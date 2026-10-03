package com.mycash.core.base;

import com.mycash.core.Utills.APIData;
import com.mycash.core.Utills.ExcelUtils;
import com.mycash.core.auth.AuthCache;
import com.mycash.core.config.ConfigReader;
//import com.mycash.core.Utills.EmailUtil;
import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class BaseAPITest {

    private static final Logger log = LoggerFactory.getLogger(BaseAPITest.class);

    protected static String baseuri;
    protected static Map<String, APIData> apiCache;

    private static final String ALLURE_RESULTS = "allure-results";
    private static final String ALLURE_REPORT = "allure-report";

    public static Map<String, APIData> getApiCache() {
        return apiCache;
    }

    @BeforeSuite(alwaysRun = true)
    public void init() {
        String env = resolveEnv();
        log.info("Environment = {}", env);

        loadConfig(env);

        baseuri = ConfigReader.get("baseuri");
        String authUrl = ConfigReader.get("auth_url");
        String otpUrl = ConfigReader.get("otp_url");

        validateConfig(baseuri, "baseuri", env);
        validateConfig(authUrl, "auth_url", env);
        validateConfig(otpUrl, "otp_url", env);

        RestAssured.baseURI = baseuri;
        log.info("RestAssured.baseURI set to {}", baseuri);

        loadTestData();

        List<String> users = ConfigReader.getUserList(env);
        warmUpAuth(users);

        log.info("Framework initialized successfully.");
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        try {
            File resultsDir = new File(ALLURE_RESULTS);
            if (resultsDir.exists() && resultsDir.isDirectory() && resultsDir.list().length > 0) {
                generateAllureReport();
                String reportPath = System.getProperty("user.dir") + File.separator + ALLURE_REPORT;
                try {
                    //  EmailUtil.sendEmailWithReport(reportPath);
                    log.warn("No allure-results found; skipping report generation.");
                } catch (Exception e) {
                    log.warn("Failed to send email with report: {}", e.getMessage());
                }
            } else {
                log.warn("No allure-results found; skipping report generation.");
            }
        } catch (Exception e) {
            log.error("Error during tearDownSuite: {}", e.getMessage(), e);
        } finally {
            AuthCache.clearAll();
        }
    }

    private void generateAllureReport() throws IOException, InterruptedException {

        log.info("Generating Allure report...");

        deleteDirectory(Paths.get(ALLURE_REPORT));

        ProcessBuilder checkPb = new ProcessBuilder("allure", "--version");
        checkPb.redirectErrorStream(true);
        try {
            Process check = checkPb.start();
            boolean ok = check.waitFor(5, TimeUnit.SECONDS);
            if (!ok || check.exitValue() != 0) {
                log.warn("Allure CLI not available on PATH. Skipping HTML report generation. Use 'allure serve' locally to preview results.");
                return;
            }
        } catch (IOException ex) {
            log.warn("Allure CLI check failed: {}. Skipping report generation.", ex.getMessage());
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("allure", "generate", ALLURE_RESULTS, "-o", ALLURE_REPORT, "--clean");
        pb.inheritIO();
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Allure generation failed. Exit code: " + exitCode);
        }

        log.info("Allure report generated successfully at {}", ALLURE_REPORT);
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private String resolveEnv() {
        String env = System.getProperty("env");
        if (isBlank(env)) env = System.getenv("ENV");
        if (isBlank(env)) env = "qa";
        return env;
    }

    private void loadConfig(String env) {
        String path = "environments/" + env + ".properties";
        ConfigReader.load(path);
        log.info("Loaded config for env: {}", env);
    }

    private void validateConfig(String value, String key, String env) {
        if (isBlank(value)) {
            throw new RuntimeException(key + " missing in env: " + env);
        }
    }

    private void loadTestData() {
        try {
            apiCache = ExcelUtils.getExcelData(
                    "src/main/resources/fintech_banking_.xlsx",
                    "src/main/resources/Fintech_Banking_Wallet_API_Details.xlsx"
            );
            log.info("Loaded API definitions: {}", apiCache == null ? 0 : apiCache.size());
        } catch (Exception e) {
            log.warn("Failed to load Excel test data: {}. Proceeding with empty apiCache.", e.getMessage());
            apiCache = null;
        }
    }

    private void warmUpAuth(List<String> users) {
        if (users == null || users.isEmpty()) return;
        for (String user : users) {
            if (isBlank(user)) continue;
            try {
                AuthCache.getCachedToken(user.trim());
            } catch (Exception e) {
                log.warn("Warm-up auth failed for user {}: {}", user, e.getMessage());
            }
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}