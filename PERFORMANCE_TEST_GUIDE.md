# MyCash Performance Testing - Ready-to-Run Guide

## Scope
Performance testing is isolated under `src/test/java/com/mycash/performance` and uses:

`src/test/resources/testng-performance.xml`

The existing functional suite remains:

`src/test/resources/testng-all.xml`

**Performance authentication is now isolated from the functional `ConfigReader/TokenManager` path.** This prevents performance configuration from changing the functional test configuration.

---

## 1. Safe baseline / smoke run

Default configuration in `src/test/resources/performance.properties`:

- Workbook: `src/main/resources/2026_09_06_1APIsGateway3088.xlsx`
- Base URL: `http://172.16.50.18:3088`
- Scenario: `baseline`
- Threads: `1`
- Duration: `60` seconds
- Authentication: enabled
- P95 threshold: `5000 ms`
- P99 threshold: `8000 ms`
- Maximum error rate: `5%`

From the `api-automation` directory:

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml
```

### Important
The performance suite does **not** load or modify the functional `ConfigReader` state.

---

## 2. Functional test remains separate

Functional suite:

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-all.xml
```

Do not change `testng-all.xml` for performance execution.

---

## 3. Performance authentication flow

For authenticated performance tests the framework now uses this flow:

```text
performance.properties
        |
        v
POST /authenticate-admin
        |
        v
session cookies
        |
        v
POST /verify-admin-otp
        |
        v
Bearer token
        |
        v
Pre-flight selected API with Bearer token
        |
        +---- 401/403 --> FAIL FAST (JMeter does not start)
        |
        v
JMeter Java DSL performance workload
```

### Authentication pre-flight

Before JMeter starts, the generated Bearer token is validated against one of the selected APIs. A GET API is preferred; if no GET API is selected, the first selected API is used.

- **2xx:** authentication pre-flight passes.
- **401/403:** the run stops immediately with an authentication error; no useless load is generated.
- **Other status:** the token was not rejected as unauthorized, so the run continues and the response is reported as an API/request-level result.

The token value is never printed; only its length and a short non-sensitive prefix are logged. Authentication request bodies are also no longer logged to avoid exposing credentials in the console.

The performance authentication reads the admin values from `performance.properties` instead of relying on the functional global `ConfigReader`.

The existing functional `TokenManager` is not modified.

### Current admin configuration

```properties
admin.id=0
admin.email=info@amtelkom.com
admin.password=QA@2026#
admin.deviceType=WEB
admin.deviceId=postman-live
admin.msisdn=718118948
admin.userType=ADMIN
admin.pinCode=9999
```

The OTP request intentionally follows the existing working functional behavior:

```json
{
  "msisdn": "<admin.email>",
  "pincode": "<admin.pincode/pinCode>",
  "deviceId": "<admin.deviceId>"
}
```

---

## 4. If authentication returns HTTP 400

The framework retries the admin authentication once by default:

```properties
perf.authRetryCount=1
perf.authRetryDelayMs=1000
```

If both attempts return HTTP 400, the failure is before the performance workload starts. The console will identify whether the failure occurred at:

- `AUTHENTICATE-ADMIN`, or
- `VERIFY-ADMIN-OTP`

This should be validated against the same request in Postman before changing the performance workload.

A pre-generated token can also be supplied without changing functional tests:

```properties
perf.bearerToken=
```

or:

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml -Dperf.bearerToken="<TOKEN>"
```

Leave it blank when normal performance authentication should be used.

---

## 5. Load test

Example: 10 concurrent threads for 5 minutes:

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml -Dperf.scenario=load -Dperf.threads=10 -Dperf.durationSeconds=300
```

## 6. Stress test

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml -Dperf.scenario=stress -Dperf.threads=25 -Dperf.maxThreads=50 -Dperf.rampUpSeconds=60 -Dperf.holdSeconds=120
```

## 7. Spike test

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml -Dperf.scenario=spike -Dperf.threads=10 -Dperf.maxThreads=50 -Dperf.rampUpSeconds=30 -Dperf.holdSeconds=60
```

## 8. Soak test

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml -Dperf.scenario=soak -Dperf.threads=10 -Dperf.durationSeconds=1800
```

---

## 9. Gateway 3088 workbook

Example:

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml -Dperf.workbook=src/main/resources/1_108APIsGateway3088.xlsx
```

---

## 10. Core 8080 workbook

For the currently defined no-auth Core scenario:

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml -Dperf.workbook=src/main/resources/1_72APIsCore8080.xlsx -Dperf.baseUrl=http://172.16.50.18:8080 -Dperf.authRequired=false
```

This does not change the functional environment or functional authentication.

---

## 11. API selection

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml -Dperf.api="API_NAME_1,API_NAME_2"
```

## 12. HTTP method selection

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml -Dperf.method=GET
```

---

## 13. URL handling

Performance execution supports both:

- relative endpoint: `/user/details`
- complete URL: `http://172.16.50.18:3088/user/details`

A complete URL from the Excel input is no longer incorrectly prefixed with `perf.baseUrl`.

For a relative endpoint, `perf.baseUrl` is used.

---

## 14. Reporting

After a performance execution, the framework generates **two reporting views** from the same JTL:

```text
target/
├── performance-results.jtl
├── performance-report/
│   ├── index.html                 # Management-ready, offline-friendly dashboard
│   └── jmeter-dashboard/          # Official JMeter HTML dashboard
│       └── index.html
└── MyCash_Performance_Report_.zip
```

### Management dashboard

Open: `target/performance-report/index.html`

This dashboard is intentionally self-contained: CSS, charts and measured values are embedded in the HTML, so it can be opened directly from an extracted ZIP without starting a web server. It includes:

- Total requests
- Successful requests / errors
- Error rate
- Throughput
- Average / minimum / maximum response time
- P90 / P95 / P99
- Peak active threads
- HTTP response-code distribution
- Response-time distribution
- Throughput over time
- Average response time over time
- Per-API/sampler request, error and percentile summary
- Configured P95/P99/error-rate thresholds

### Official JMeter dashboard

Open: `target/performance-report/jmeter-dashboard/index.html`

This is the standard JMeter dashboard generated from the same performance results. JMeter's dashboard provides statistics and charts such as response-time percentiles, response times over time, active threads, throughput/hits, response codes, latency and response-time distribution.

### Management ZIP

The framework creates:

`target/MyCash_Performance_Report_.zip`

Extract the ZIP and open **`index.html`** at the root of the extracted report folder. No Maven project, Java source, Excel input or functional test reports are required for management.

> If the official JMeter dashboard is opened directly with `file://` and some interactive charts do not render in a particular browser, use the root management `index.html` for offline sharing. The official dashboard remains included for detailed JMeter analysis.

## 14. Results

The result file is configurable through:

```properties
perf.resultsFile=target/performance-results.jtl
```

Console summary includes:

- total samples
- errors
- error percentage
- P95
- P99

Configured thresholds cause the performance TestNG test to fail when exceeded.

---


## 14A. Automatic HTML dashboard and management ZIP

Every performance execution now generates the performance artifacts automatically:

```text
target/
├── performance-results.jtl
├── performance-report/
│   ├── index.html
│   └── supporting dashboard files
└── MyCash_Performance_Report_.zip
```

The HTML dashboard is generated by the existing JMeter Java DSL `htmlReporter` and is kept separate from the functional Extent/Allure reports.

The management ZIP contains the complete contents of `performance-report`, with `index.html` at the ZIP root. Management does not need the Maven project to view it.

### Recommended single command

From the `api-automation` directory:

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml
```

For a 10-thread, 5-minute load test:

```bat
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml -Dperf.scenario=load -Dperf.threads=10 -Dperf.durationSeconds=300
```

After the run:

1. Open `target/performance-report/index.html` locally to review the complete dashboard.
2. Send `target/MyCash_Performance_Report_.zip` to management.
3. Management extracts the ZIP and opens `index.html`.

The dashboard includes standard JMeter performance views such as request counts, response-time statistics, percentiles, throughput, error information, response-code distribution and time-series graphs.

If a configured performance threshold fails, the HTML dashboard and management ZIP are still created before the TestNG threshold assertion is evaluated, so the run remains reviewable.

## 15. Functional safety

The following functional authentication classes were intentionally left unchanged:

- `TokenManager.java`
- `AuthCache.java`
- `RequestSpecFactory.java`
- `RunFullCollectionTest.java`
- `testng-all.xml`
- `qa.properties`

Performance-specific changes are isolated under:

- `com.mycash.performance.auth.PerformanceAuthManager`
- `PerformanceConfig`
- `PerformanceTest`
- `PerformancePlanFactory`
- `performance.properties`



### Reporting implementation detail

The performance JTL is written as a single deterministic file at `perf.resultsFile` (default: `target/performance-results.jtl`). The report directory is cleaned before each run. The official JMeter dashboard is generated at `target/performance-report/jmeter-dashboard/`, while `target/performance-report/index.html` is the offline-friendly management dashboard.
