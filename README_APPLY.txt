MyCash Performance v5 patch

Apply from repository root.

Recommended:
  git apply --directory=. /path/to/combined.patch

If applying individual patches, use:
  git apply /path/to/pom.patch
  git apply /path/to/PerformanceAuthManager.patch
  git apply /path/to/PerformanceConfig.patch
  git apply /path/to/PerformanceTest.patch
  git apply /path/to/performance.properties.patch

Then verify:
  git diff --check
  git status

Run baseline:
  mvn clean test -Dtestng.suite.file=src/test/resources/testng-performance.xml -Dperf.scenario=baseline -Dperf.threads=1 -Dperf.durationSeconds=60 -Dperf.rampUpSeconds=10 -Dperf.iterations=1
