package com.mycash.performance.engine;

import com.mycash.performance.config.PerformanceConfig;
import com.mycash.performance.model.PerformanceApi;
import org.apache.http.entity.ContentType;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslDefaultThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;
import us.abstracta.jmeter.javadsl.core.threadgroups.RpsThreadGroup;

import java.time.Duration;
import java.util.List;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public final class PerformancePlanFactory {

    private PerformancePlanFactory() {}

    public static DslThreadGroup createThreadGroup(PerformanceConfig config,
                                                            List<PerformanceApi> apis,
                                                            String token) {
        ThreadGroupChild[] samplers = apis.stream()
                .map(api -> createSampler(api, config, token))
                .toArray(ThreadGroupChild[]::new);

        if (config.getTargetRps() > 0 && !config.getScenario().equals("stress") && !config.getScenario().equals("spike")) {
            RpsThreadGroup rps = rpsThreadGroup("API Performance - " + config.getScenario())
                    .maxThreads(config.getMaxThreads())
                    .rampTo(config.getTargetRps(), Duration.ofSeconds(config.getRampUpSeconds()))
                    .holdFor(Duration.ofSeconds(config.getDurationSeconds()))
                    .rampTo(0, Duration.ofSeconds(1));
            if (config.getExecutionMode().equals("weighted")) {
                var weighted = weightedSwitchController().randomChoice(true);
                for (PerformanceApi api : apis) {
                    weighted.child(api.weight(), createSampler(api, config, token));
                }
                return rps.children(weighted);
            }
            return rps.children(samplers);
        }

        if (config.getExecutionMode().equals("weighted")) {
            var weighted = weightedSwitchController().randomChoice(true);
            for (PerformanceApi api : apis) {
                weighted.child(api.weight(), createSampler(api, config, token));
            }
            return profile(config, weighted);
        }

        return profile(config, samplers);
    }

    private static DslDefaultThreadGroup profile(PerformanceConfig config, ThreadGroupChild... children) {
        String name = "API Performance - " + config.getScenario();

        return switch (config.getScenario()) {
            case "baseline" -> threadGroup(name, 1, Duration.ofSeconds(config.getDurationSeconds()), children);
            case "load" -> threadGroup(name, config.getThreads(), Duration.ofSeconds(config.getDurationSeconds()), children);
            case "soak" -> threadGroup(name, config.getThreads(), Duration.ofSeconds(config.getDurationSeconds()), children);
            case "stress" -> threadGroup(name)
                    .rampTo(Math.max(1, config.getThreads() / 2), Duration.ofSeconds(config.getRampUpSeconds()))
                    .rampToAndHold(config.getThreads(), Duration.ofSeconds(config.getRampUpSeconds()), Duration.ofSeconds(config.getHoldSeconds()))
                    .rampTo(config.getMaxThreads(), Duration.ofSeconds(config.getRampUpSeconds()))
                    .rampTo(0, Duration.ofSeconds(config.getRampUpSeconds()))
                    .children(children);
            case "spike" -> threadGroup(name)
                    .rampTo(config.getThreads(), Duration.ofSeconds(Math.max(1, config.getRampUpSeconds())))
                    .rampToAndHold(config.getMaxThreads(), Duration.ofSeconds(1), Duration.ofSeconds(config.getHoldSeconds()))
                    .rampTo(0, Duration.ofSeconds(Math.max(1, config.getRampUpSeconds())))
                    .children(children);
            default -> throw new IllegalArgumentException("Unsupported scenario: " + config.getScenario());
        };
    }

    private static DslHttpSampler createSampler(PerformanceApi performanceApi,
                                                    PerformanceConfig config,
                                                    String token) {
        var api = performanceApi.api();
        String url = join(config, api.getEndPoint());
        var sampler = httpSampler(api.getApiName(), url)
                .useKeepAlive(true)
                .header("Accept", "application/json");

        if (config.isAuthRequired()) {
            if (token == null || token.isBlank()) {
                throw new IllegalStateException(
                        "Authentication is enabled but no token was generated.");
            }
            sampler.header("Authorization", "Bearer " + token);
        }

        String method = api.getMethod() == null ? "GET" : api.getMethod().trim().toUpperCase();
        String body = api.getRequestBody() == null ? "{}" : api.getRequestBody();

        switch (method) {
            case "GET", "DELETE", "PATCH", "HEAD", "OPTIONS" -> sampler.method(method);
            case "POST" -> sampler.post(body, ContentType.APPLICATION_JSON);
            case "PUT" -> sampler.method("PUT").contentType(ContentType.APPLICATION_JSON).body(body);
            default -> throw new IllegalArgumentException("Unsupported HTTP method for performance test: " + method);
        }
        return sampler;
    }

    private static String join(PerformanceConfig config, String endpoint) {
        String base = config.getBaseUrl();
        if (base == null || base.isBlank()) {
            throw new IllegalStateException(
                    "baseUrl is missing for performance test. Set perf.baseUrl in performance.properties or with -Dperf.baseUrl=<url>.");
        }

        if (endpoint == null || endpoint.isBlank()) return base;
        String trimmedEndpoint = endpoint.trim();

        // Excel may contain either a relative endpoint or a complete URL.
        // Do not prepend the performance base URL to an already absolute URL.
        if (trimmedEndpoint.matches("(?i)^https?://.*")) {
            return trimmedEndpoint;
        }

        if (base.endsWith("/") && trimmedEndpoint.startsWith("/")) {
            return base.substring(0, base.length() - 1) + trimmedEndpoint;
        }
        if (!base.endsWith("/") && !trimmedEndpoint.startsWith("/")) {
            return base + "/" + trimmedEndpoint;
        }
        return base + trimmedEndpoint;
    }
}
