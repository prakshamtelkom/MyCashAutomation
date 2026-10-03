package com.mycash.performance;

import com.mycash.performance.auth.PerformanceAuthManager;
import com.mycash.performance.config.PerformanceConfig;
import com.mycash.performance.engine.PerformancePlanFactory;
import com.mycash.performance.loader.PerformanceApiLoader;
import com.mycash.performance.model.PerformanceApi;
import com.mycash.performance.report.PerformanceReportGenerator;
import org.testng.annotations.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class PerformanceTest {

    private static final Path PERFORMANCE_REPORT_DIR = Path.of("target", "performance-report");
    private static final Path MANAGEMENT_REPORT_ZIP = Path.of("target", "MyCash_Performance_Report_.zip");

    @Test
    public void runPerformanceTest() throws IOException {
        PerformanceConfig config = PerformanceConfig.fromSystemProperties();
        List<PerformanceApi> apis = PerformanceApiLoader.load(config);
        String token = config.isAuthRequired() ? PerformanceAuthManager.generateToken(config) : "";

        // Fail fast on authentication before starting JMeter. This prevents a
        // long run of meaningless 401/403 samples and tells us whether the
        // problem is the token itself or JMeter request construction.
        if (config.isAuthRequired()) {
            PerformanceApi preflightApi = apis.stream()
                    .filter(api -> "GET".equalsIgnoreCase(api.api().getMethod()))
                    .findFirst()
                    .orElse(apis.get(0));
            PerformanceAuthManager.validateTokenAgainstApi(config, preflightApi, token);
        }

        prepareReporting(config);

        System.out.println("\n================ PERFORMANCE TEST ================");
        System.out.println(config);
        System.out.println("Selected APIs : " + apis.size());
        apis.forEach(a -> System.out.println("  - " + a.name() + " [weight=" + a.weight() + "]"));
        System.out.println("===================================================\n");

        TestPlanStats stats = testPlan(
                PerformancePlanFactory.createThreadGroup(config, apis, token),
                jtlWriter(
                        resultsPath(config).getParent().toString(),
                        resultsPath(config).getFileName().toString()
                ),
                htmlReporter(PERFORMANCE_REPORT_DIR.toString(), "jmeter-dashboard")
        ).run();

        // Keep the official JMeter dashboard plus a self-contained management dashboard.
        // The management index has no external JS/CSS/data dependencies, so it works when
        // opened directly from an extracted ZIP (file://) without a local web server.
        PerformanceReportGenerator.generate(
                resultsPath(config),
                PERFORMANCE_REPORT_DIR,
                config,
                apis.size(),
                "jmeter-dashboard/index.html"
        );

        // Generate the management package before threshold assertions so the
        // complete dashboard is available even when a performance gate fails.
        zipDirectory(PERFORMANCE_REPORT_DIR, MANAGEMENT_REPORT_ZIP);

        printSummary(stats, config, apis.size());
        printReportLocations();

        assertThat(stats.overall().errorsCount())
                .as("Performance test errors")
                .isLessThanOrEqualTo(Math.max(0, Math.round(stats.overall().samplesCount() * config.getErrorRatePercent() / 100.0)));

        if (config.getP95Ms() > 0) {
            assertThat(stats.overall().sampleTime().perc95())
                    .as("P95 response time")
                    .isLessThanOrEqualTo(Duration.ofMillis(config.getP95Ms()));
        }
        if (config.getP99Ms() > 0) {
            assertThat(stats.overall().sampleTimePercentile99())
                    .as("P99 response time")
                    .isLessThanOrEqualTo(Duration.ofMillis(config.getP99Ms()));
        }
    }

    private void prepareReporting(PerformanceConfig config) throws IOException {
        deleteDirectory(PERFORMANCE_REPORT_DIR);
        Files.deleteIfExists(MANAGEMENT_REPORT_ZIP);
        Files.deleteIfExists(resultsPath(config));
        Path parent = resultsPath(config).getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private Path resultsPath(PerformanceConfig config) {
        return Path.of(config.getResultsFile()).toAbsolutePath().normalize();
    }

    private void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (var paths = Files.walk(directory)) {
            paths.sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(
                                    "Unable to clean performance report path: " + path, e);
                        }
                    });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private void zipDirectory(Path sourceDirectory, Path zipFile) throws IOException {
        if (!Files.isDirectory(sourceDirectory)) {
            throw new IOException("Performance HTML report directory was not generated: " + sourceDirectory);
        }

        Path parent = zipFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ZipOutputStream zip = new ZipOutputStream(
                Files.newOutputStream(
                        zipFile,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE))) {

            try (var paths = Files.walk(sourceDirectory)) {
                paths.filter(Files::isRegularFile)
                        .forEach(file -> {
                            String entryName = sourceDirectory.relativize(file)
                                    .toString()
                                    .replace('\\', '/');
                            try {
                                zip.putNextEntry(new ZipEntry(entryName));
                                Files.copy(file, zip);
                                zip.closeEntry();
                            } catch (IOException e) {
                                throw new UncheckedIOException(
                                        "Unable to add file to performance report ZIP: " + file, e);
                            }
                        });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }
    }

    private void printReportLocations() {
        System.out.println("\n================ PERFORMANCE REPORT ================");
        System.out.println("HTML Dashboard : " + PERFORMANCE_REPORT_DIR.resolve("index.html"));
        System.out.println("Management ZIP  : " + MANAGEMENT_REPORT_ZIP);
        System.out.println("=====================================================\n");
    }

    private void printSummary(TestPlanStats stats, PerformanceConfig config, int apiCount) {
        System.out.println("\n================ PERFORMANCE SUMMARY ================");
        System.out.println("Scenario       : " + config.getScenario());
        System.out.println("APIs           : " + apiCount);
        System.out.println("Samples        : " + stats.overall().samplesCount());
        System.out.println("Errors         : " + stats.overall().errorsCount());
        System.out.println("Error %        : " + stats.overall().errorsCount() * 100.0 / Math.max(1, stats.overall().samplesCount()));
        System.out.println("P95            : " + stats.overall().sampleTime().perc95());
        System.out.println("P99            : " + stats.overall().sampleTimePercentile99());
        System.out.println("======================================================\n");
    }
}
