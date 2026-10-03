MyCash API Automation Framework
A reusable, data-driven API automation framework for functional API testing and performance testing of MyCash services.
The framework provides a common automation platform for:
Functional API automation
Excel-driven API execution
Postman collection execution
API authentication and token management
API request/response validation
Allure and Extent reporting
Smoke testing
Load testing
Stress testing
Spike testing
Soak testing
Automated JTL generation
Automated HTML performance reporting
JMeter HTML dashboard generation
1. Framework Overview
The framework is designed to support both functional API validation and performance testing while keeping their execution flows independent.
High-Level Architecture
flowchart TB

    A[MyCash API Automation Framework]

    A --> B[Functional API Automation]
    A --> C[Performance Testing]

    B --> B1[Excel API Inventory]
    B --> B2[Postman Collection]

    B1 --> B3[API Execution]
    B2 --> B3

    B3 --> B4[REST Assured]
    B4 --> B5[TestNG]
    B5 --> B6[API Validation]

    B6 --> B7[Allure Report]
    B6 --> B8[Extent Report]

    C --> C1[Excel API Inventory]
    C1 --> C2[Performance Configuration]
    C2 --> C3[Authentication & Pre-flight]

    C3 --> C4[Load]
    C3 --> C5[Stress]
    C3 --> C6[Spike]
    C3 --> C7[Soak]

    C4 --> C8[JMeter Java DSL]
    C5 --> C8
    C6 --> C8
    C7 --> C8

    C8 --> C9[JTL Results]
    C9 --> C10[Custom HTML Report]
    C9 --> C11[JMeter HTML Dashboard]
2. Key Features


Area
Capability
API Automation
REST Assured + TestNG
API Data
Excel-driven
API Source
Excel + Postman
Authentication
Token/session based
Validation
Status, response and execution validation
Functional Reporting
Allure + Extent
Smoke Testing
Supported
Performance Engine
JMeter Java DSL
Performance Execution
TestNG
Performance Scenarios
Load / Stress / Spike / Soak
Performance Data
Excel
Performance Results
JTL
Performance Reporting
Custom HTML + JMeter Dashboard
Configuration
Externalized properties
Filtering
Workbook / Sheet / API / Method
CI Execution
Maven/TestNG compatible
3. Functional API Automation
3.1 Functional Testing
The functional automation layer provides reusable API execution and validation capabilities using:
Java
REST Assured
TestNG
Excel
Postman collections
Allure
Extent Reports
The framework is designed to avoid creating a separate Java test method for every API. API definitions and execution data are maintained externally wherever possible.
Functional capabilities
GET, POST, PUT and other HTTP methods
Excel-driven API execution
Postman collection execution
Environment-based configuration
Authentication/token handling
Request payload handling
Response validation
Status-code validation
API-level pass/fail reporting
API filtering
Workbook/sheet filtering
HTTP method filtering
Parallel execution support
Request/response reporting
Allure attachments
Extent reporting
Smoke-test execution
4. Functional API Architecture
flowchart LR

    A[Excel API Inventory] --> C[API Data Loader]
    B[Postman Collection] --> C

    C --> D[Execution Filter]

    D --> E[Authentication / Token Manager]

    E --> F[API Test Runner]

    F --> G[REST Assured]

    G --> H[API Response]

    H --> I[Validation]

    I --> J[TestNG Result]

    J --> K[Allure Report]
    J --> L[Extent Report]
The functional execution flow is:
API Source
    ↓
Excel / Postman
    ↓
API Data Loading
    ↓
Execution Filtering
    ↓
Authentication
    ↓
REST Assured Request
    ↓
API Response
    ↓
Validation
    ↓
TestNG Result
    ↓
Allure / Extent Report
5. Excel-Driven API Automation
The framework supports data-driven API execution through Excel workbooks.
API inventory can be maintained externally and loaded at runtime.
This provides the following benefits:
No hard-coded API execution for every endpoint
Easy addition of new APIs
Easy modification of test data
Module/sheet-based organization
API-level filtering
Method-level filtering
Reusable execution logic
Typical information maintained in the Excel-driven model includes:
API name
HTTP method
Endpoint
Request payload
Module/sheet
Test data
Environment-specific information
Execution Model
Excel Workbook
      ↓
ExcelUtils
      ↓
API Data
      ↓
Execution Filter
      ↓
API Runner
      ↓
REST Assured
      ↓
Response
      ↓
Validation
6. Postman Collection Automation
The framework also supports API execution from the configured Postman collection.
Example collection:
src/main/resources/environments/
└── Mycash_final.postman_collection1.json
This allows existing API definitions maintained in Postman to be incorporated into the automation framework without recreating every API manually.
7. Functional API Execution
The primary functional execution layer is located under:
src/test/java/com/mycash/api/
The framework provides reusable execution components for:
API loading
API execution
Authentication
Request construction
Response handling
Validation
Reporting
A typical functional execution is initiated using TestNG/Maven.
Functional Suite
src/test/resources/testng-excel.xml
Additional suites are available depending on the execution requirement.
8. API Filtering
The framework supports runtime filtering so that users can execute only the required APIs without modifying Java code.
Typical filters include:
Workbook
Sheet
API
HTTP method
Environment
Execution source
Execute Functional Suite
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml
Execute a Specific API
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml \
-Dapi="API_NAME"
Execute Multiple APIs
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml \
-Dapi="API_1,API_2,API_3"
Execute a Specific HTTP Method
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml \
-Dmethod=GET
9. Authentication & Token Management
Authentication is implemented through reusable authentication components.
The framework supports authenticated API execution through token/session management.
Typical flow:
sequenceDiagram
    participant T as TestNG
    participant A as Auth Manager
    participant S as Authentication Service
    participant API as MyCash API

    T->>A: Request Token
    A->>S: Authenticate
    S-->>A: Session / Token
    A-->>T: Authentication Context
    T->>API: API Request + Token
    API-->>T: API Response
    T->>T: Validate Response
Authentication-related components are maintained under the core authentication layer.
Authentication configuration is externalized from individual API test methods.
10. Request & Response Handling
Reusable request and response specifications are provided through the framework's core components.
The framework centralizes common API handling such as:
Request specification
Base URI
Headers
Authentication
Request payload
Response specification
Status-code validation
Response capture
Error handling
This reduces duplication across individual API tests.
11. API Validation
Each API execution produces an execution result that can be evaluated by the functional test layer.
Validation can include:
HTTP status
Response content
API execution status
Error information
Expected/actual result
Simplified flow:
API Request
    ↓
API Response
    ↓
Status / Response Validation
    ↓
PASS / FAIL
Failed API executions provide relevant execution information for troubleshooting.
12. Functional Reporting
The functional automation layer supports:
Allure Reporting
Allure provides detailed execution visibility including:
API name
HTTP method
Endpoint
Request information
Response information
Execution status
Failure details
Request/response attachments
Extent Reporting
Extent Reports provide an additional execution summary including:
API execution status
API name
Result
Failure information
Execution details
Functional reporting is intentionally kept independent from the performance reporting pipeline.
13. Smoke Testing
The framework contains smoke-test support for quick validation of critical APIs/business functionality.
Smoke tests can be executed independently before broader functional or performance execution.
Example location:
src/test/java/com/mycash/smoke/
14. Performance Testing
The performance framework is implemented as a separate execution layer using:
Java
TestNG
JMeter Java DSL
Excel
Maven
The framework supports configurable performance testing without requiring changes to the Java execution code for normal workload changes.
15. Performance Architecture
flowchart LR

    A[Excel API Inventory]
    B[performance.properties]

    A --> C[Performance API Loader]
    B --> D[Performance Configuration]

    C --> E[Performance Plan Factory]
    D --> E

    E --> F[Authentication]

    F --> G[Pre-flight API Validation]

    G --> H{Validation Result}

    H -->|2xx| I[Start Performance Test]
    H -->|401 / 403 / Failure| J[Fail Fast]

    I --> K[Load]
    I --> L[Stress]
    I --> M[Spike]
    I --> N[Soak]

    K --> O[JMeter Java DSL]
    L --> O
    M --> O
    N --> O

    O --> P[JTL Result]

    P --> Q[Custom HTML Dashboard]
    P --> R[JMeter HTML Dashboard]
16. Performance API Selection
Performance APIs are maintained through Excel workbooks.
The framework loads the configured workbook and applies runtime filters.
Default configuration is maintained in:
src/test/resources/performance.properties
Example:
perf.env=qa
perf.sheet=ALL
perf.api=ALL
perf.method=ALL
perf.scenario=baseline
perf.executionMode=sequence
perf.threads=1
perf.durationSeconds=60
perf.iterations=1
The workbook can be overridden at runtime.
Example:
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.workbook=src/main/resources/1_108APIsGateway3088.xlsx
17. Performance Scenarios
Load Testing
Used to evaluate API behavior under expected/conventional concurrent load.
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=load \
-Dperf.threads=10 \
-Dperf.durationSeconds=300
Stress Testing
Used to gradually increase workload beyond the expected operating level and observe system behavior.
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=stress \
-Dperf.threads=25 \
-Dperf.maxThreads=50 \
-Dperf.rampUpSeconds=60 \
-Dperf.holdSeconds=120
Spike Testing
Used to evaluate system behavior when concurrency increases sharply.
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=spike \
-Dperf.threads=10 \
-Dperf.maxThreads=50 \
-Dperf.rampUpSeconds=30 \
-Dperf.holdSeconds=60
Soak Testing
Used to evaluate API/system behavior over an extended duration.
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=soak \
-Dperf.threads=10 \
-Dperf.durationSeconds=1800
18. Performance Authentication & Pre-flight
Authenticated performance execution performs authentication and API pre-flight validation before starting the actual performance workload.
Authenticate
     ↓
OTP / Token Validation
     ↓
Bearer Token
     ↓
Pre-flight API
     ↓
2xx Response
     ↓
Start Performance Test
If authentication or pre-flight validation returns an authentication/authorization failure such as 401 or 403, the framework fails fast rather than generating misleading performance results.
This helps distinguish:
Authentication / Environment Issue
              from
Actual Performance Behaviour
19. Gateway and Core Performance Testing
The framework supports different MyCash API environments/workbooks.
Gateway
Example:
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.workbook=src/main/resources/1_108APIsGateway3088.xlsx
Core
For Core APIs where authentication is not required:
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.workbook=src/main/resources/1_72APIsCore8080.xlsx \
-Dperf.baseUrl=http://172.16.50.18:8080 \
-Dperf.authRequired=false
20. Performance API Filtering
A specific API can be executed using:
-Dperf.api="API_NAME"
Multiple APIs can be selected:
-Dperf.api="API_1,API_2,API_3"
HTTP method filtering:
-Dperf.method=GET
This allows targeted performance validation without changing the performance execution code.
21. Performance Results
The performance execution generates a JTL result file:
target/performance-results.jtl
The JTL contains request-level performance information that is used to generate the reporting dashboards.
22. Automated Performance Reporting
The framework automatically generates two levels of HTML reporting.
Management-Friendly HTML Report
target/performance-report/index.html
The custom dashboard provides:
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
API-level summary
Configured thresholds
JMeter HTML Dashboard
target/performance-report/jmeter-dashboard/index.html
This provides the standard JMeter dashboard generated from the JTL result.
23. Performance Report Package
A complete performance report package is generated as:
target/MyCash_Performance_Report_*.zip
The package can be shared with stakeholders for reviewing the execution results.
The custom management dashboard is designed to be easier to consume for offline review.
24. Performance Thresholds
Performance thresholds can be configured in:
src/test/resources/performance.properties
Example:
perf.p95Ms=5000
perf.p99Ms=8000
perf.maxErrorRatePercent=5
The framework can use these thresholds to determine whether configured performance criteria have been exceeded.
The generated report still remains available for investigation even when a threshold causes the TestNG performance execution to fail.
25. Functional vs Performance Execution


Capability
Functional
Performance
REST API execution
✓
✓
Excel-driven APIs
✓
✓
Postman collection
✓
—
REST Assured
✓
—
TestNG
✓
✓
Authentication
✓
✓
API filtering
✓
✓
Workbook filtering
✓
✓
Method filtering
✓
✓
API validation
✓
✓
Allure
✓
—
Extent
✓
—
Load testing
—
✓
Stress testing
—
✓
Spike testing
—
✓
Soak testing
—
✓
JMeter Java DSL
—
✓
JTL generation
—
✓
JMeter Dashboard
—
✓
Performance thresholds
—
✓
26. Test Suite Separation
Functional and performance executions are maintained as separate TestNG suites.
Functional
src/test/resources/testng-all.xml
src/test/resources/testng-excel.xml
Performance
src/test/resources/testng-performance.xml
This prevents normal functional API execution from accidentally starting performance workloads.
27. Project Structure
MyCash API Automation
│
├── src/
│   │
│   ├── main/
│   │   └── resources/
│   │       ├── *.xlsx
│   │       ├── environments/
│   │       │   └── Mycash_final.postman_collection1.json
│   │       ├── payloads/
│   │       └── environment properties
│   │
│   └── test/
│       │
│       ├── java/
│       │   └── com/mycash/
│       │       │
│       │       ├── api/
│       │       │   └── tests/
│       │       │
│       │       ├── core/
│       │       │   ├── auth/
│       │       │   ├── clients/
│       │       │   ├── config/
│       │       │   ├── execution/
│       │       │   ├── model/
│       │       │   ├── reporting/
│       │       │   ├── specs/
│       │       │   └── Utills/
│       │       │
│       │       ├── services/
│       │       │
│       │       ├── smoke/
│       │       │
│       │       └── performance/
│       │           ├── auth/
│       │           ├── config/
│       │           ├── engine/
│       │           ├── loader/
│       │           ├── model/
│       │           └── report/
│       │
│       └── resources/
│           ├── testng-all.xml
│           ├── testng-excel.xml
│           ├── testng-performance.xml
│           ├── performance.properties
│           └── allure.properties
│
├── PERFORMANCE_TEST_GUIDE.md
├── pom.xml
└── README.md
28. Technology Stack


Category
Technology
Language
Java
Build Tool
Maven
Functional API Automation
REST Assured
Test Framework
TestNG
API Data
Excel
API Source
Excel + Postman
Functional Reporting
Allure + Extent
Performance Engine
JMeter Java DSL
Performance Data
Excel
Performance Result
JTL
Performance Dashboard
JMeter HTML Dashboard
Custom Reporting
HTML
Configuration
Properties
Execution
Maven + TestNG
29. Configuration-Driven Design
The framework follows a configuration-driven approach.
The following can generally be changed without modifying the core execution code:
Functional
Environment
Workbook
Sheet
API
HTTP method
Test data
Execution source
Performance
Environment
Workbook
Sheet
API
HTTP method
Scenario
Threads
Maximum threads
Ramp-up
Duration
Iterations
Authentication requirement
Performance thresholds
This allows the framework to scale as the API inventory grows.
30. Recommended Execution Flow
Functional API Testing
Excel / Postman
      ↓
API Loader
      ↓
Filter APIs
      ↓
Authentication
      ↓
REST Assured
      ↓
API Validation
      ↓
TestNG
      ↓
Allure / Extent
Performance Testing
Excel API Inventory
      ↓
Performance Configuration
      ↓
Authentication
      ↓
Pre-flight Validation
      ↓
Load / Stress / Spike / Soak
      ↓
JMeter Java DSL
      ↓
JTL
      ↓
HTML Reports
      ↓
JMeter Dashboard
31. Quick Start
Functional API Automation
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-excel.xml
Performance Load Test
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=load \
-Dperf.threads=10 \
-Dperf.durationSeconds=300
Specific Performance API
mvn clean test \
-Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
-Dperf.scenario=load \
-Dperf.api="API_NAME" \
-Dperf.threads=10 \
-Dperf.durationSeconds=300
32. Repository Outputs
Functional execution:
Allure Results
Extent Report
TestNG Results
Logs
Performance execution:
target/
├── performance-results.jtl
├── performance-report/
│   ├── index.html
│   └── jmeter-dashboard/
│       └── index.html
└── MyCash_Performance_Report_*.zip
33. Design Principles
The framework follows these principles:
Data-driven — API definitions are maintained externally.
Configuration-driven — execution parameters are externalized.
Reusable — common API, authentication and reporting components are centralized.
Scalable — new APIs can be added without creating a new execution framework.
Filterable — specific APIs, sheets, methods and workbooks can be executed.
Reportable — functional and performance executions provide dedicated reporting.
Isolated — functional and performance suites are independently executable.
CI-friendly — Maven and TestNG based execution can be integrated with CI pipelines.
34. Security & Repository Hygiene
Do not commit real credentials, tokens, passwords or other secrets into source control.
Use:
Environment variables
CI/CD secret variables
Approved secret-management solutions
External configuration
Generated execution artifacts should also generally remain outside source control.
Recommended .gitignore entries:
target/
*.jtl
performance-report/
MyCash_Performance_Report_*.zip
35. Summary
The MyCash API Automation Framework provides a unified automation platform for both functional API quality validation and performance engineering.
                    MyCash API Automation
                             |
             +---------------+---------------+
             |                               |
       Functional Testing             Performance Testing
             |                               |
     REST Assured + TestNG          JMeter Java DSL
             |                               |
     Excel + Postman                Excel-driven APIs
             |                               |
     API Validation                 Load / Stress / Spike / Soak
             |                               |
     Allure + Extent                JTL + HTML + JMeter Dashboard
The framework is designed to provide a reusable, scalable and configuration-driven approach to API quality automation across functional and performance testing.
