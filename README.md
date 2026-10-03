MyCash API Automation & Performance Testing Framework
A reusable Java-based automation framework for REST API functional testing and performance testing of the MyCash application.
The framework supports Excel-driven API execution for functional testing and a reusable, parameterized performance-testing layer using JMeter Java DSL.
1. Overview
The MyCash API Automation Framework provides two independent testing capabilities:
Functional API Automation
Functional API testing is implemented using:
Java
Maven
REST Assured
TestNG
Apache POI
Postman Collection
Allure
Extent Reports
The functional framework supports Excel-driven API execution, authentication, filtering, validation, and module-wise execution.
Performance Testing
Performance testing is implemented using:
Java
TestNG
JMeter Java DSL
Apache JMeter
Excel-driven API selection
Configurable performance scenarios
JTL result generation
JMeter HTML Dashboard
Performance thresholds
Supported scenarios:
Load
Stress
Spike
Soak
2. Key Capabilities
Functional Testing
REST API automation
Excel-driven API execution
Postman collection integration
REST Assured-based requests
TestNG execution
Authentication and token management
API filtering
Workbook filtering
HTTP method filtering
Request and response handling
API response validation
Allure reporting
Extent reporting
Module-wise execution
Dedicated functional TestNG suites
Performance Testing
Reusable performance framework
Excel-driven API selection
API filtering
Workbook and sheet filtering
Configurable execution scenarios
Load testing
Stress testing
Spike testing
Soak testing
Sequence-based execution
Weighted API execution
Configurable concurrency
Ramp-up configuration
Hold duration
Iteration control
Authentication pre-flight validation
JMeter Java DSL execution
JTL generation
JMeter HTML Dashboard
Performance thresholds
Offline management-friendly report
Performance result packaging
3. Architecture
The framework separates functional API automation from performance execution.
flowchart TD
    A[Excel API Inventory] --> B{Execution Type}

    B --> C[Functional API Testing]
    B --> D[Performance Testing]

    C --> C1[REST Assured]
    C1 --> C2[TestNG]
    C2 --> C3[API Validation]
    C3 --> C4[Allure / Extent Reports]

    D --> D1[Performance Configuration]
    D1 --> D2[API Selection]
    D2 --> D3[Authentication Pre-flight]
    D3 --> D4{Authentication Valid?}

    D4 -->|No| D5[Stop Performance Execution]
    D4 -->|Yes| D6[JMeter Java DSL]

    D6 --> D7[Load / Stress / Spike / Soak]
    D7 --> D8[JTL Result]
    D8 --> D9[JMeter HTML Dashboard]
    D8 --> D10[Management Performance Report]
4. Functional API Automation
The functional framework uses REST Assured and TestNG to execute REST APIs.
API information and test data can be maintained in Excel, while the Postman collection provides the API definitions and request structure where applicable.
Functional Execution Flow
Excel / Postman API Definition
            |
            v
     API Selection
            |
            v
 Authentication
            |
            v
     API Execution
            |
            v
 Request / Response Handling
            |
            v
      API Validation
            |
            v
 Allure / Extent Reporting
5. Excel-Driven Execution
The framework supports Excel-driven API execution.
Excel can be used to define and control:
API name
Endpoint
HTTP method
Request data
Expected response
Module
Environment
Performance selection
Execution sequence
Weight
This allows APIs to be added or modified without changing the core execution engine.
6. Postman Collection
The framework supports API definitions from the Postman collection.
Example:
src/main/resources/environments/
└── Mycash_final.postman_collection1.json
The Postman collection can be used as the source/reference for API request information.
7. Authentication
Authentication is supported for both functional and performance execution.
The framework manages authentication and token handling before protected APIs are executed.
Performance Authentication
Performance execution includes a dedicated authentication pre-flight.
The flow is:
Performance Start
       |
       v
Load Authentication Configuration
       |
       v
Generate Authentication Token
       |
       v
Validate Authentication
       |
       +------ Failed ------> Stop Execution
       |
       v
Start Performance Workload
This prevents a performance test from producing misleading results when the authentication token is invalid.
For example, if all requests return 401 Unauthorized, the framework should identify the authentication problem before starting the actual workload.
8. API Filtering
The framework supports filtering APIs instead of always executing the complete API inventory.
Supported filtering includes:
API filtering
Workbook filtering
Sheet filtering
HTTP method filtering
Scenario-based selection
Example:
API = Third Party My Payroll Requests
or:
API = ALL
When ALL is selected, the configured API inventory can be used for execution.
9. Performance Testing
The performance framework is designed to be reusable and parameterized.
The same performance engine can be used for different APIs and different performance scenarios without creating a separate performance implementation for every API.
Supported Scenarios


Scenario
Purpose
Load
Validate application behaviour under expected workload
Stress
Identify behaviour beyond expected workload
Spike
Validate sudden increases or decreases in traffic
Soak
Validate stability during prolonged execution
10. Load Testing
Load testing executes APIs with a configured number of concurrent users/threads for a defined duration.
Typical configurable parameters include:
Threads
Ramp-up
Duration
Hold time
Iterations
Example:
Scenario       = load
Threads        = 10
Ramp-up        = 10 seconds
Duration       = 300 seconds
11. Stress Testing
Stress testing increases the workload to evaluate system behaviour under higher-than-normal concurrency.
Example configuration:
Scenario       = stress
Threads        = 10
Max Threads    = 50
Ramp-up        = 10 seconds
Duration       = 300 seconds
The exact workload should be determined from the agreed performance test requirements and system capacity.
12. Spike Testing
Spike testing introduces a sudden change in traffic.
The objective is to observe:
Response time behaviour
Error rate
Recovery behaviour
Throughput
Application stability
The workload profile is configurable through the performance configuration.
13. Soak Testing
Soak testing runs the selected APIs for an extended period.
It can be used to identify:
Performance degradation over time
Resource exhaustion
Connection issues
Memory-related problems
Increasing response times
Increasing error rates
The duration should be configured according to the agreed performance test plan.
14. JMeter Java DSL
The performance framework uses JMeter Java DSL for performance-test execution.
The Java DSL allows the framework to create and execute JMeter test plans programmatically.
The performance layer handles:
API Selection
     |
     v
Performance Configuration
     |
     v
JMeter Test Plan
     |
     v
HTTP Requests
     |
     v
Load Generation
     |
     v
JTL Results
15. Performance Configuration
Performance execution can be controlled through:
performance.properties
Typical parameters include:
scenario=load
executionMode=sequence
threads=10
maxThreads=50
durationSeconds=300
rampUpSeconds=10
holdSeconds=300
API selection can also be controlled through workbook and sheet configuration.
Example:
perf.workbook=src/main/resources/2026_09_06_1APIsGateway3088.xlsx
sheet=ALL
api=ALL
Performance thresholds can be configured using parameters such as:
p95Ms=<threshold>
p99Ms=<threshold>
The exact threshold values should be agreed with the application/business team before execution.
16. Performance API Selection
The framework supports selecting:
Single API
api=Third Party My Payroll Requests
Multiple APIs
Multiple APIs can be configured for a performance run.
Complete API Set
api=ALL
This allows the same performance engine to execute different API combinations without code changes.
17. Sequence and Weighted Execution
The framework supports configurable API execution order.
Example:
API A -> API B -> API C -> API D
API weights can also be used to represent different traffic distributions.
Example:
API A   Weight = 100
API B   Weight = 50
API C   Weight = 25
This allows performance workloads to represent different traffic patterns when required.
18. Performance Authentication Pre-flight
Authentication validation is performed before starting the performance workload.
Objective
The purpose is to avoid executing a complete load test when authentication itself is failing.
Validation Flow
Read Performance Configuration
             |
             v
Generate Authentication Token
             |
             v
Validate Token
             |
             v
Execute Authentication Check
             |
       +-----+-----+
       |           |
     FAIL        SUCCESS
       |           |
       v           v
 Stop Test    Start JMeter
                 Workload
If authentication fails, the performance workload should not continue.
19. JTL Generation
Performance execution generates a JTL result file.
Typical output:
target/performance-results.jtl
The JTL contains the raw performance execution results required for further analysis and dashboard generation.
The reporting pipeline is:
Performance Test
       |
       v
JTL
       |
       +------------------+
       |                  |
       v                  v
JMeter Dashboard     Management Report
20. JMeter HTML Dashboard
The JTL result is used to generate the JMeter HTML Dashboard.
The dashboard can provide information such as:
Response time statistics
Throughput
Error percentage
Response-code distribution
Response-time distribution
Percentiles
Request statistics
Performance trends
The generated dashboard is retained as part of the performance result package.
Typical location:
jmeter-dashboard/
21. Management Performance Report
In addition to the standard JMeter dashboard, the framework can generate a simplified HTML report for easier management consumption.
The report can include:
Total Requests
Successful Requests
Failed Requests
Error Rate
Throughput
Average Response Time
Minimum Response Time
Maximum Response Time
P90
P95
P99
Peak Threads
Response Code Distribution
Response Time Distribution
API Summary
Threshold Results
The purpose of this report is to provide a concise view of the performance execution without requiring the reader to navigate the complete JMeter dashboard.
22. Performance Thresholds
Performance thresholds can be used to determine whether a performance run meets predefined criteria.
Examples include:
P95 response time
P99 response time
Error rate
Example configuration:
p95Ms=<configured-value>
p99Ms=<configured-value>
Threshold values should be defined based on the application's agreed SLA/SLO and performance requirements.
A threshold failure should be clearly visible in the generated performance report.
23. Performance Result Structure
A typical performance execution produces:
target/
│
├── performance-results.jtl
│
├── performance-report/
│   └── index.html
│
└── jmeter-dashboard/
    ├── index.html
    ├── content/
    ├── sbadmin2-1.0.7/
    └── ...
The exact generated files can vary depending on the reporting configuration.
24. Performance Execution Flow
The complete performance execution is:
1. Read performance.properties
            |
            v
2. Select environment
            |
            v
3. Select workbook / sheet / API
            |
            v
4. Load API definitions
            |
            v
5. Authentication pre-flight
            |
            v
6. Validate authentication
            |
            v
7. Create JMeter test plan
            |
            v
8. Execute selected scenario
            |
            v
9. Generate JTL
            |
            v
10. Generate JMeter HTML Dashboard
            |
            v
11. Generate management report
            |
            v
12. Evaluate performance thresholds
25. Capability Matrix

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
Extent Reports
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
JMeter HTML Dashboard
—
✓
Performance thresholds
—
✓
26. Test Suite Separation
Functional and performance executions are maintained as separate TestNG suites.
Functional Suites
src/test/resources/testng-all.xml
src/test/resources/testng-excel.xml
Performance Suite
src/test/resources/testng-performance.xml
This separation prevents normal functional API execution from accidentally starting performance workloads.
Performance execution is explicitly triggered through the dedicated performance TestNG suite.
27. Project Structure
MyCash API Automation
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── mycash
│   │   │           └── api
│   │   │               ├── core
│   │   │               ├── auth
│   │   │               ├── clients
│   │   │               ├── config
│   │   │               ├── execution
│   │   │               ├── model
│   │   │               ├── reporting
│   │   │               ├── specs
│   │   │               ├── services
│   │   │               ├── smoke
│   │   │               └── performance
│   │   │                   ├── auth
│   │   │                   ├── config
│   │   │                   ├── engine
│   │   │                   ├── loader
│   │   │                   ├── model
│   │   │                   └── report
│   │   │
│   │   └── resources
│   │       ├── *.xlsx
│   │       ├── environments
│   │       │   └── Mycash_final.postman_collection1.json
│   │       ├── payloads
│   │       └── environment properties
│   │
│   └── test
│       ├── java
│       │   └── com
│       │       └── mycash
│       │           └── api
│       │               └── tests
│       │
│       └── resources
│           ├── testng-all.xml
│           ├── testng-excel.xml
│           ├── testng-performance.xml
│           ├── performance.properties
│           └── allure.properties
│
├── PERFORMANCE_TEST_GUIDE.md
├── pom.xml
└── README.md
The structure above represents the logical framework organization. Actual package/file names may vary slightly depending on the current implementation.
28. Technology Stack
Functional Automation


Technology
Purpose
Java
Programming language
Maven
Build and dependency management
REST Assured
REST API automation
TestNG
Test execution and suite management
Apache POI
Excel processing
Jackson
JSON processing
Postman Collection
API definition/reference
Allure
Test reporting
Extent Reports
Test reporting
Performance Testing


Technology
Purpose
Java
Performance framework implementation
TestNG
Test suite orchestration
JMeter Java DSL
Programmatic JMeter test-plan creation
Apache JMeter
Performance execution and dashboard generation
Excel
API inventory and workload configuration
JTL
Raw performance result storage
HTML
Performance result visualization
29. Maven
The project uses Maven for:
Dependency management
Compilation
Test execution
Build lifecycle management
Performance execution integration
Typical command:
mvn clean test
Performance execution can be triggered through the dedicated TestNG performance suite.
Example:
mvn clean test -DtestngXmlSuiteFiles=src/test/resources/testng-performance.xml
If the project provides a dedicated Maven profile for performance execution, the corresponding profile can be used.
30. Functional Test Execution
Execute All Functional Tests
mvn clean test
Execute Specific TestNG Suite
mvn test -DtestngXmlSuiteFiles=src/test/resources/testng-all.xml
Execute Excel-Based Suite
mvn test -DtestngXmlSuiteFiles=src/test/resources/testng-excel.xml
31. Performance Test Execution
Performance execution should use the dedicated performance TestNG suite.
mvn clean test -DtestngXmlSuiteFiles=src/test/resources/testng-performance.xml
The performance configuration can be supplied through:
performance.properties
or the supported Maven/system-property overrides.
32. Example Performance Configuration
env=qa

perf.workbook=src/main/resources/2026_09_06_1APIsGateway3088.xlsx

sheet=ALL

api=ALL

scenario=load

executionMode=sequence

threads=10

maxThreads=50

durationSeconds=300

rampUpSeconds=10

holdSeconds=300

iterations=1

p95Ms=<configured-value>

p99Ms=<configured-value>
Values should be changed according to the performance test requirement.
33. Example Performance Workload
Example workload:
Environment       : QA
Gateway           : 3088
Scenario          : Load
Execution Mode    : Sequence
Threads           : 10
Maximum Threads   : 50
Ramp-up           : 10 seconds
Duration          : 300 seconds
Hold Time         : 300 seconds
Iterations        : 1
The selected APIs are driven from the configured Excel workbook.
34. Performance Inputs Required
Before executing a formal performance test, the following inputs should be available.
API Scope
API inventory
API endpoint
HTTP method
Module
Business criticality
Expected request volume
Expected traffic distribution
Authentication
Performance test user
Required privileges
Authentication flow
Token requirements
OTP requirements, if applicable
Workload
Expected concurrency
Peak concurrency
Requests per second / throughput target
Test duration
Ramp-up
Ramp-down, if applicable
Load profile
Performance Criteria
Average response-time expectation
P95 target
P99 target
Maximum acceptable error rate
Throughput expectation
Environment Monitoring
The performance test should ideally be accompanied by monitoring of:
Application CPU
Application memory
Database CPU
Database memory
Database connections
Connection pool usage
Slow queries
Network utilization
Application error logs
Infrastructure metrics
35. Performance Result Analysis
The generated results can be analyzed using:
JTL
The JTL provides raw execution-level performance data.
JMeter Dashboard
The dashboard provides detailed performance visualizations.
Management Report
The simplified report provides key execution metrics such as:
Total Requests
Success Count
Error Count
Error Rate
Throughput
Average Response Time
P90
P95
P99
Maximum Response Time
Peak Concurrency
36. Reporting Pipeline
                    PERFORMANCE TEST
                           |
                           v
                    JMeter Execution
                           |
                           v
                    JTL Generation
                           |
             +-------------+-------------+
             |                           |
             v                           v
     JMeter HTML Dashboard       Management HTML Report
             |                           |
             +-------------+-------------+
                           |
                           v
                   Performance Package
The reporting flow ensures that the raw JTL remains available while providing both detailed JMeter analysis and a simplified management view.
37. Functional vs Performance Execution
Functional and performance execution are intentionally isolated.


Area
Functional
Performance
Test Suite
testng-all.xml / testng-excel.xml
testng-performance.xml
Main Purpose
Functional validation
Workload validation
REST Assured
Yes
Framework dependency/reference
JMeter
No
Yes
JTL
No
Yes
JMeter Dashboard
No
Yes
Allure
Yes
—
Extent
Yes
—
Load/Stress/Spike/Soak
No
Yes
This separation reduces the risk of unintentionally running performance workloads during normal functional execution.
38. Design Principles
The framework follows these principles:
Reusability
The performance engine is designed to execute different APIs without creating separate performance code for each API.
Configuration Driven
Performance parameters are externalized into configuration and Excel inputs wherever possible.
Separation of Concerns
Functional and performance execution are maintained independently.
Fail-Fast Authentication
Authentication failures are detected before starting the workload.
Reporting
Raw JTL results, JMeter dashboards, and simplified performance reports are maintained as separate outputs.
Parameterization
Concurrency, duration, ramp-up, scenario, API selection, and thresholds can be changed without modifying the core execution engine.
39. Recommended Performance Test Lifecycle
1. Identify API scope
        |
        v
2. Identify critical APIs / flows
        |
        v
3. Define workload
        |
        v
4. Define performance thresholds
        |
        v
5. Prepare performance users
        |
        v
6. Validate authentication
        |
        v
7. Execute baseline
        |
        v
8. Execute load test
        |
        v
9. Execute stress / spike / soak as required
        |
        v
10. Monitor application and infrastructure
        |
        v
11. Analyze JTL and dashboard
        |
        v
12. Review threshold results
        |
        v
13. Document observations
40. Troubleshooting
Authentication Failure
If the authentication pre-flight fails:
Do not continue with the performance workload.
Verify:
Base URL
Authentication endpoint
Credentials
Request payload
OTP flow
User privileges
Environment availability
Large Number of 401 Responses
A high percentage of 401 Unauthorized responses may indicate:
Invalid token
Expired token
Incorrect authentication flow
Incorrect environment
Missing authorization
Performance user privilege issue
Authentication should be validated before starting the workload.
JTL Not Generated
Check:
Performance test execution
JMeter execution logs
Target directory
Performance configuration
Authentication status
The JTL should normally be available under:
target/performance-results.jtl
Dashboard Not Generated
Verify:
JTL exists
JMeter report generation completed
JMeter installation/dependency configuration
Output directory permissions
41. Output Summary
A successful performance execution should produce the following key artifacts:
Performance Execution
        |
        +-- JTL
        |
        +-- JMeter HTML Dashboard
        |
        +-- Management HTML Report
        |
        +-- Threshold Results
        |
        +-- Execution Logs
42. Quick Reference
Functional
mvn clean test
Functional TestNG Suite
mvn test -DtestngXmlSuiteFiles=src/test/resources/testng-all.xml
Excel Functional Suite
mvn test -DtestngXmlSuiteFiles=src/test/resources/testng-excel.xml
Performance
mvn clean test -DtestngXmlSuiteFiles=src/test/resources/testng-performance.xml
Main Performance Result
target/performance-results.jtl
JMeter Dashboard
jmeter-dashboard/index.html
Management Report
target/performance-report/index.html
43. Summary
The MyCash API Automation Framework provides a single reusable framework for both functional API automation and performance testing while maintaining clear separation between the two execution types.
Functional Testing Provides
Excel-driven API automation
REST Assured execution
TestNG execution
Authentication
API filtering
Request/response validation
Allure reporting
Extent reporting
Performance Testing Provides
Reusable API performance execution
Excel-driven API selection
Load testing
Stress testing
Spike testing
Soak testing
JMeter Java DSL
Authentication pre-flight
JTL generation
JMeter HTML Dashboard
Performance thresholds
Management-friendly reporting
The framework is designed so that the same performance engine can be reused for different APIs, workloads, environments and performance scenarios through configuration and Excel inputs rather than creating separate performance implementations for each API.
