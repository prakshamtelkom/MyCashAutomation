MyCash API Automation Framework
A reusable, data-driven API automation framework for functional API testing and performance testing of MyCash services.
The framework supports:
Functional API automation using Java + REST Assured + TestNG
Excel-driven API execution
Postman collection execution
API/module/sheet/method/workbook level filtering
Authentication and token management
Request/response validation
Allure and Extent reporting
Configurable QA environment execution
Performance testing using JMeter Java DSL
Load, Stress, Spike and Soak testing
Automated JTL generation
Automated HTML performance reporting
JMeter HTML dashboard generation
1. Framework Overview
The framework is divided into two independent execution areas:
                         MyCash API Automation
                                  |
                  +---------------+---------------+
                  |                               |
          Functional API Testing           Performance Testing
                  |                               |
        REST Assured + TestNG             JMeter Java DSL
                  |                               |
        +---------+---------+             +-------+--------+
        |                   |             |                |
      Excel              Postman        Load             Stress
        |                   |             |                |
        +---------+---------+             Spike            Soak
                  |                               |
          API Validation                  JTL / Metrics
                  |                               |
          Allure + Extent                 HTML Reports
The functional and performance suites are intentionally kept separate so that functional execution can be performed without triggering performance workloads.
2. Functional API Automation
2.1 Functional Testing Capabilities
The functional automation framework provides a reusable execution layer for validating MyCash APIs.
Key capabilities
REST API execution using REST Assured
GET, POST, PUT and other supported HTTP methods
Excel-driven API definitions
Postman collection execution
Environment-based configuration
Authentication/token handling
Request payload execution
Response status validation
Response body capture
API-level pass/fail validation
Dynamic API test naming
API execution filtering
Parallel API execution through TestNG DataProvider
Allure reporting
Extent reporting
Request/response attachments
Failed API details in reports
2.2 Excel-Driven API Automation
Functional APIs can be maintained in Excel workbooks instead of hard-coding individual test methods.
The framework reads API definitions from Excel using:
Excel Workbook
      |
      v
ExcelUtils
      |
      v
Sheet
      |
      v
APIData
      |
      v
ApiTestRunner
      |
      v
REST Assured
      |
      v
API Response
      |
      v
Validation + Reporting
This allows new APIs to be added or existing API details to be updated primarily through test data/configuration rather than creating a new Java test method for every API.
Typical API information maintained through the Excel-driven model includes:
API name
HTTP method
Endpoint
Request payload
Sheet/module
Environment-specific information
API-specific test data
3. Functional API Execution
The main functional execution class is:
src/test/java/com/mycash/api/tests/RunFullCollectionTest.java
The execution flow is:
TestNG
   |
   v
RunFullCollectionTest
   |
   +---- Load Excel APIs
   |
   +---- Load Postman collection
   |
   v
ExecutionFilter
   |
   v
ApiTestRunner
   |
   v
REST Assured
   |
   v
API Response
   |
   +---- Status validation
   +---- Response validation
   +---- Allure attachment
   +---- Extent reporting
   |
   v
TestNG Result
Each API is treated as an individual TestNG execution and receives its own execution result.
4. API Execution Sources
The functional framework supports two API sources.
Excel APIs
Excel-based APIs are loaded through:
ExcelUtils
and executed using:
ApiTestRunner
Postman APIs
The framework also supports execution of the configured Postman collection:
src/main/resources/environments/Mycash_final.postman_collection1.json
Postman execution is handled through:
PostmanCollectionRunner
This allows the framework to execute APIs maintained in both Excel and Postman formats.
5. Functional Execution Filtering
The framework provides runtime filtering so that users do not need to modify Java code to execute a smaller subset of APIs.
Supported filters include:
-Dworkbook
-Dsheet
-Dapi
-Dmethod
-Dsource
-Dtags
-DfailedOnly
-Denv
-Dsuite
-Dmode
-Dthreads
Execute a specific workbook
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml \
-Dworkbook=20260917_20APIsGateway3088.xlsx
Execute a specific sheet
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml \
-Dsheet=ALL
Execute a specific API
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml \
-Dapi="API_NAME"
Execute a specific HTTP method
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml \
-Dmethod=GET
Execute multiple APIs
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml \
-Dapi="API_1,API_2,API_3"
The filters can be combined when a more targeted functional execution is required.
6. Authentication & Token Management
Authentication is handled through the reusable authentication layer:
src/test/java/com/mycash/core/auth/
Important components include:
TokenManager
AuthCache
The framework supports an authentication flow where required:
Authentication Request
        |
        v
Session/Cookies
        |
        v
OTP Verification
        |
        v
Bearer Token
        |
        v
Authenticated API Execution
Authentication configuration is externalized through environment properties rather than being embedded inside individual API tests.
7. Environment Configuration
Environment-specific configuration is maintained under:
src/main/resources/
Examples include:
qa.properties
stage.properties
The framework loads environment configuration through:
ConfigReader
ConfigManager
TestConfigInitializer
This separates environment-specific values from API execution logic.
Typical configuration includes:
Base URL
Authentication endpoints
User configuration
API environment settings
Authentication-related properties
Credentials and secrets should not be committed to source control. Use environment variables or an approved secret-management mechanism for shared repositories.
8. Request & Response Handling
Reusable request/response components are provided through:
core/specs/
including:
RequestSpecFactory
ResponseSpecFactory
The execution layer provides common handling for API requests and responses.
The framework captures useful execution information such as:
HTTP method
Request URI
Request payload
HTTP status code
Response body
Execution result
Error information
Response timing
9. API Validation
Every API execution produces an ApiResult.
The result contains information required to determine whether the API execution was successful.
The functional execution validates the API result and fails the corresponding TestNG test when the API execution is unsuccessful.
Example validation flow:
API Request
    |
    v
Response
    |
    v
ApiResult
    |
    +---- Status
    +---- Response
    +---- Error
    |
    v
Validation
    |
    +---- PASS
    |
    +---- FAIL
Failed API executions include relevant status/error information to simplify troubleshooting.
10. Reporting — Functional Testing
The framework supports both Allure and Extent Reports for functional execution.
Allure
Allure provides detailed API-level execution information including:
API name
Source
Workbook
Sheet/module
HTTP method
Endpoint
Request body
Response body
Execution status
API execution hierarchy
The framework also attaches request and response information to the Allure report.
Extent
Extent reporting provides an additional execution view containing:
API execution status
Source
Workbook
Sheet/module
API result
Failure information
Reporting components are maintained under:
src/test/java/com/mycash/core/reporting/
11. Functional Test Suites
Functional execution is separated from performance execution.
Functional suite
src/test/resources/testng-all.xml
Excel API suite
src/test/resources/testng-excel.xml
Postman suite
src/test/resources/testng-postman.xml
Performance suite
src/test/resources/testng-performance.xml
This separation prevents functional API execution from accidentally triggering performance workloads.
12. Smoke Testing
The framework also contains smoke-test support under:
src/test/java/com/mycash/smoke/
Example:
UserSmokeTest
Smoke tests can be used for quick validation of critical functionality before executing broader API suites.
13. Performance Testing
The performance framework is implemented separately from the functional execution layer.
Technology:
Java
TestNG
JMeter Java DSL
Excel
Performance execution supports:
Load testing
Stress testing
Spike testing
Soak testing
Configurable concurrency
Ramp-up
Duration
Iterations
API selection
Workbook selection
Gateway/Core environment selection
Authentication pre-flight
Bearer token handling
JTL generation
Automated HTML reporting
JMeter HTML dashboard
14. Performance API Selection
Performance APIs are also Excel-driven.
Default configuration:
src/test/resources/performance.properties
The workbook can be changed without modifying the performance Java code.
Example:
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.workbook=src/main/resources/1_108APIsGateway3088.xlsx
API and HTTP method filtering are also supported:
-Dperf.api="API_NAME"
-Dperf.method=GET
15. Performance Scenarios
Load
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=load \
-Dperf.threads=10 \
-Dperf.durationSeconds=300
Stress
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=stress \
-Dperf.threads=25 \
-Dperf.maxThreads=50 \
-Dperf.rampUpSeconds=60 \
-Dperf.holdSeconds=120
Spike
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=spike \
-Dperf.threads=10 \
-Dperf.maxThreads=50 \
-Dperf.rampUpSeconds=30 \
-Dperf.holdSeconds=60
Soak
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=soak \
-Dperf.threads=10 \
-Dperf.durationSeconds=1800
16. Performance Authentication & Pre-flight
For authenticated performance APIs, the framework performs authentication before starting the load.
Authenticate
     |
     v
OTP Verification
     |
     v
Bearer Token
     |
     v
Pre-flight API Validation
     |
     +---- 2xx --> Start Performance Test
     |
     +---- 401/403 --> Fail Fast
This prevents an invalid authentication state from producing misleading performance results.
17. Performance Reporting
Performance execution produces:
target/performance-results.jtl
The framework then generates:
target/performance-report/index.html
and:
target/performance-report/jmeter-dashboard/index.html
The reporting layer provides metrics including:
Total requests
Successful requests
Failed requests
Error rate
Throughput
Average response time
Minimum response time
Maximum response time
P90
P95
P99
Peak active threads
Response-code distribution
Response-time distribution
Throughput over time
Average response time over time
API-level performance summary
Configured performance thresholds
A packaged report is also generated:
target/MyCash_Performance_Report_*.zip
18. Functional vs Performance Execution


Capability
Functional API Automation
Performance Testing
REST API validation
Yes
Yes
Excel-driven APIs
Yes
Yes
Postman collection
Yes
No
TestNG
Yes
Yes
REST Assured
Yes
Supporting/API layer
JMeter Java DSL
No
Yes
Load testing
No
Yes
Stress testing
No
Yes
Spike testing
No
Yes
Soak testing
No
Yes
Authentication
Yes
Yes
API filtering
Yes
Yes
Allure reporting
Yes
No
Extent reporting
Yes
No
JTL generation
No
Yes
JMeter HTML dashboard
No
Yes
Performance thresholds
No
Yes
19. Project Structure
src/
├── main/
│   └── resources/
│       ├── *.xlsx
│       ├── environments/
│       │   ├── Mycash_final.postman_collection1.json
│       │   └── MyCash_TestPlan.jmx
│       ├── payloads/
│       ├── qa.properties
│       └── stage.properties
│
└── test/
    ├── java/
    │   └── com/mycash/
    │       ├── api/tests/
    │       │   ├── RunFullCollectionTest.java
    │       │   └── AuthenticationAndUserFlowTests.java
    │       │
    │       ├── core/
    │       │   ├── auth/
    │       │   ├── clients/
    │       │   ├── config/
    │       │   ├── execution/
    │       │   ├── model/
    │       │   ├── reporting/
    │       │   ├── specs/
    │       │   └── Utills/
    │       │
    │       ├── services/
    │       ├── smoke/
    │       │
    │       └── performance/
    │           ├── auth/
    │           ├── config/
    │           ├── engine/
    │           ├── loader/
    │           ├── model/
    │           └── report/
    │
    └── resources/
        ├── testng-all.xml
        ├── testng-excel.xml
        ├── testng-postman.xml
        ├── testng-performance.xml
        ├── performance.properties
        └── allure.properties
20. Recommended Execution Model
For normal API validation:
Excel / Postman
       |
       v
Functional API Suite
       |
       v
API Validation
       |
       v
Allure / Extent
For performance validation:
Excel API Inventory
       |
       v
Performance Configuration
       |
       v
Authentication + Pre-flight
       |
       v
Load / Stress / Spike / Soak
       |
       v
JTL
       |
       v
HTML + JMeter Dashboard
This architecture allows the same API inventory and environment information to support both functional validation and performance testing while keeping the two execution models isolated.
21. Technology Stack


Area
Technology
Language
Java
Build
Maven
Functional API
REST Assured
Functional Execution
TestNG
API Data
Excel
API Source
Excel + Postman
Functional Reporting
Allure + Extent
Performance Engine
JMeter Java DSL
Performance Execution
TestNG
Performance Data
Excel
Performance Result
JTL
Performance Dashboard
JMeter HTML Dashboard
Custom Reporting
HTML
Logging
Log4j / SLF4J
22. Key Design Principles
The framework is designed around the following principles:
Data-driven — API definitions are maintained externally through Excel/Postman.
Configuration-driven — environment and execution parameters are externalized.
Reusable — common authentication, request, execution and reporting components are centralized.
Filterable — users can execute specific workbooks, sheets, APIs or methods.
Reportable — functional and performance executions generate dedicated reports.
Scalable — the framework can handle increasing API inventory without creating a separate Java test method for every API.
Isolated — functional and performance suites can be executed independently.
CI-friendly — Maven/TestNG based execution can be integrated into CI pipelines.
23. Quick Start
Functional API execution
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml
Performance execution
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=load \
-Dperf.threads=10 \
-Dperf.durationSeconds=300
The framework therefore provides a single automation repository covering the complete API quality lifecycle:
Functional API Validation
          +
Authentication
          +
API Data Management
          +
Reporting
          +
Performance Testing
          +
Performance Analytics
