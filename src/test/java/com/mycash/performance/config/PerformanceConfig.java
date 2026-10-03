package com.mycash.performance.config;

import java.util.*;
import java.io.InputStream;
import java.util.stream.Collectors;

/**
 * Runtime configuration for reusable API performance tests.
 * All values can be overridden with -D system properties.
 */
public final class PerformanceConfig {

    private final String env;
    private final String workbook;
    private final String baseUrl;
    private final String sheet;
    private final Set<String> apis;
    private final Set<String> methods;
    private final String scenario;
    private final String executionMode;
    private final int threads;
    private final int maxThreads;
    private final int durationSeconds;
    private final int rampUpSeconds;
    private final int holdSeconds;
    private final int iterations;
    private final double targetRps;
    private final double errorRatePercent;
    private final long p95Ms;
    private final long p99Ms;
    private final String user;
    private final boolean authRequired;
    private final Map<String, Long> apiWeights;
    private final String bearerToken;
    private final int authRetryCount;
    private final long authRetryDelayMs;
    private final String resultsFile;

    private static final Properties FILE_PROPERTIES = loadFileProperties();

    private PerformanceConfig() {
        env = value("env", "qa");
        workbook = required("perf.workbook");
        baseUrl = value("perf.baseUrl", "");
        sheet = value("perf.sheet", "ALL");
        apis = csv("perf.api");
        methods = csv("perf.method");
        scenario = value("perf.scenario", "load").toLowerCase(Locale.ROOT);
        executionMode = value("perf.executionMode", "sequence").toLowerCase(Locale.ROOT);
        threads = positiveInt("perf.threads", 10);
        maxThreads = positiveInt("perf.maxThreads", Math.max(threads, 50));
        durationSeconds = positiveInt("perf.durationSeconds", 300);
        rampUpSeconds = nonNegativeInt("perf.rampUpSeconds", 60);
        holdSeconds = positiveInt("perf.holdSeconds", durationSeconds);
        iterations = positiveInt("perf.iterations", 1);
        targetRps = nonNegativeDouble("perf.targetRps", 0);
        errorRatePercent = nonNegativeDouble("perf.maxErrorRatePercent", 1.0);
        p95Ms = nonNegativeLong("perf.p95Ms", 2000);
        p99Ms = nonNegativeLong("perf.p99Ms", 3000);
        user = value("perf.user", "admin");
        authRequired = Boolean.parseBoolean(value("perf.authRequired", "true"));
        apiWeights = parseWeights(value("perf.apiWeights", ""));
        bearerToken = value("perf.bearerToken", "");
        authRetryCount = nonNegativeInt("perf.authRetryCount", 1);
        authRetryDelayMs = nonNegativeLong("perf.authRetryDelayMs", 1000);
        resultsFile = value("perf.resultsFile", "target/performance-results.jtl");
        validate();
    }

    public static PerformanceConfig fromSystemProperties() {
        return new PerformanceConfig();
    }

    private static Properties loadFileProperties() {
        Properties properties = new Properties();
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("performance.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load performance.properties", e);
        }
        return properties;
    }

    private void validate() {
        Set<String> allowed = Set.of("baseline", "load", "stress", "spike", "soak");
        if (!allowed.contains(scenario)) {
            throw new IllegalArgumentException("Unsupported perf.scenario: " + scenario +
                    ". Supported: " + allowed);
        }
        if (!Set.of("sequence", "weighted").contains(executionMode)) {
            throw new IllegalArgumentException("Unsupported perf.executionMode: " + executionMode);
        }
        if (executionMode.equals("weighted") && apiWeights.isEmpty()) {
            throw new IllegalArgumentException("perf.apiWeights is required when perf.executionMode=weighted");
        }
    }

    private static String value(String key, String defaultValue) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) {
            v = FILE_PROPERTIES.getProperty(key);
        }
        return v == null || v.isBlank() ? defaultValue : v.trim();
    }

    private static String required(String key) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) {
            v = FILE_PROPERTIES.getProperty(key);
        }
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing performance configuration: " + key +
                    ". Set it in performance.properties or with -D" + key + "=<value>");
        }
        return v.trim();
    }

    private static Set<String> csv(String key) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) {
            v = FILE_PROPERTIES.getProperty(key);
        }
        if (v == null || v.isBlank() || v.equalsIgnoreCase("ALL")) return Collections.emptySet();
        return Arrays.stream(v.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static int positiveInt(String key, int defaultValue) {
        int value = Integer.parseInt(value(key, String.valueOf(defaultValue)));
        if (value <= 0) throw new IllegalArgumentException(key + " must be > 0");
        return value;
    }

    private static int nonNegativeInt(String key, int defaultValue) {
        int value = Integer.parseInt(value(key, String.valueOf(defaultValue)));
        if (value < 0) throw new IllegalArgumentException(key + " must be >= 0");
        return value;
    }

    private static long nonNegativeLong(String key, long defaultValue) {
        long value = Long.parseLong(value(key, String.valueOf(defaultValue)));
        if (value < 0) throw new IllegalArgumentException(key + " must be >= 0");
        return value;
    }

    private static double nonNegativeDouble(String key, double defaultValue) {
        double value = Double.parseDouble(value(key, String.valueOf(defaultValue)));
        if (value < 0) throw new IllegalArgumentException(key + " must be >= 0");
        return value;
    }

    private static Map<String, Long> parseWeights(String raw) {
        Map<String, Long> result = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) return result;
        for (String item : raw.split(",")) {
            String[] pair = item.split(":", 2);
            if (pair.length != 2) throw new IllegalArgumentException(
                    "Invalid perf.apiWeights entry: " + item + ". Expected API_NAME:WEIGHT");
            long weight = Long.parseLong(pair[1].trim());
            if (weight <= 0) throw new IllegalArgumentException("Weight must be > 0: " + item);
            result.put(pair[0].trim(), weight);
        }
        return result;
    }

    public String getEnv() { return env; }
    public String getWorkbook() { return workbook; }
    public String getBaseUrl() { return baseUrl; }
    public String getSheet() { return sheet; }
    public Set<String> getApis() { return apis; }
    public Set<String> getMethods() { return methods; }
    public String getScenario() { return scenario; }
    public String getExecutionMode() { return executionMode; }
    public int getThreads() { return threads; }
    public int getMaxThreads() { return maxThreads; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getRampUpSeconds() { return rampUpSeconds; }
    public int getHoldSeconds() { return holdSeconds; }
    public int getIterations() { return iterations; }
    public double getTargetRps() { return targetRps; }
    public double getErrorRatePercent() { return errorRatePercent; }
    public long getP95Ms() { return p95Ms; }
    public long getP99Ms() { return p99Ms; }
    public String getUser() { return user; }
    public boolean isAuthRequired() { return authRequired; }
    public Map<String, Long> getApiWeights() { return apiWeights; }
    public String getBearerToken() { return bearerToken; }
    public int getAuthRetryCount() { return authRetryCount; }
    public long getAuthRetryDelayMs() { return authRetryDelayMs; }
    public String getResultsFile() { return resultsFile; }

    @Override
    public String toString() {
        return "PerformanceConfig{" +
                "env='" + env + '\'' +
                ", workbook='" + workbook + '\'' +
                ", sheet='" + sheet + '\'' +
                ", apis=" + (apis.isEmpty() ? "ALL" : apis) +
                ", scenario='" + scenario + '\'' +
                ", executionMode='" + executionMode + '\'' +
                ", threads=" + threads +
                ", maxThreads=" + maxThreads +
                ", durationSeconds=" + durationSeconds +
                ", rampUpSeconds=" + rampUpSeconds +
                ", holdSeconds=" + holdSeconds +
                ", iterations=" + iterations +
                ", targetRps=" + targetRps +
                ", user='" + user + '\'' +
                '}';
    }
}
