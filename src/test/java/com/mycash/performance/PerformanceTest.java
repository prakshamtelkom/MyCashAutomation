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
            String preflightName = config.getPreflightApi();
            if (preflightName != null && !preflightName.isBlank()) {
                PerformanceApi preflightApi = apis.stream()
                        .filter(api -> api.name().equalsIgnoreCase(preflightName))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Configured perf.preflightApi was not found in the selected workbook/API set: " + preflightName));
                PerformanceAuthManager.validateTokenAgainstApi(config, preflightApi, token);
            } else {
                System.out.println("PERFORMANCE AUTH PREFLIGHT: skipped (perf.preflightApi is blank).");
            }
        }

        prepareReporting(config);

        System.out.println("\n================ PERFORMANCE TEST ================");
        System.out.println(config);
        System.out.println("Selected APIs : " + apis.size());
        apis.forEach(a -> System.out.println("  - " + a.name() + " [weight=" + a.weight() + "]"));
        System.out.println("===================================================\n");

        TestPlanStats stats;
        try {
            stats = testPlan(
                    PerformancePlanFactory.createThreadGroup(config, apis, token),
                    jtlWriter(
                            resultsPath(config).getParent().toString(),
                            resultsPath(config).getFileName().toString()
                    ),
                    htmlReporter(PERFORMANCE_REPORT_DIR.toString(), "jmeter-dashboard")
            ).run();
        } catch (RuntimeException executionFailure) {
            // JMeter can fail after the JTL writer has already persisted useful samples.
            // Never lose the management report just because the Apache dashboard/listener
            // failed or the execution aborted late.
            finalizeReports(config, apis.size());
            throw executionFailure;
        }

        // Keep the official Apache JMeter dashboard plus the self-contained management
        // dashboard. Generate the management report from the JTL before assertions so it
        // remains available even when a performance threshold later fails.
        finalizeReports(config, apis.size());

        printSummary(stats, config, apis.size());

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

    private void finalizeReports(PerformanceConfig config, int apiCount) {
        try {
            Path jtl = resultsPath(config);
            if (!Files.isRegularFile(jtl) || Files.size(jtl) <= 0) {
                System.err.println("PERFORMANCE REPORT: JTL not available; skipping HTML/ZIP generation: " + jtl);
                return;
            }

            PerformanceReportGenerator.generate(
                    jtl,
                    PERFORMANCE_REPORT_DIR,
                    config,
                    apiCount,
                    "jmeter-dashboard/index.html"
            );

            if (Files.isDirectory(PERFORMANCE_REPORT_DIR)) {
                zipDirectory(PERFORMANCE_REPORT_DIR, MANAGEMENT_REPORT_ZIP);
            }
            printReportLocations();
        } catch (Exception reportFailure) {
            // Reporting must never hide the real JMeter execution/assertion failure.
            System.err.println("PERFORMANCE REPORT GENERATION FAILED: " + reportFailure.getMessage());
            reportFailure.printStackTrace(System.err);
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
