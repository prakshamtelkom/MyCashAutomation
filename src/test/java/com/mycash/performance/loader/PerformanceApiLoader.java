package com.mycash.performance.loader;

import com.mycash.core.Utills.APIData;
import com.mycash.core.Utills.ExcelUtils;
import com.mycash.performance.config.PerformanceConfig;
import com.mycash.performance.model.PerformanceApi;

import java.util.*;
import java.util.stream.Collectors;

public final class PerformanceApiLoader {

    private PerformanceApiLoader() {}

    public static List<PerformanceApi> load(PerformanceConfig config) {
        Map<String, Map<String, APIData>> workbookData =
                ExcelUtils.getExcelDataBySheet(config.getWorkbook(), config.getSheet());

        List<PerformanceApi> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, APIData>> sheet : workbookData.entrySet()) {
            for (APIData api : sheet.getValue().values()) {
                if (!config.getApis().isEmpty() && !containsIgnoreCase(config.getApis(), api.getApiName())) continue;
                if (!config.getMethods().isEmpty() && !containsIgnoreCase(config.getMethods(), api.getMethod())) continue;
                long weight = config.getApiWeights().getOrDefault(api.getApiName(), 100L);
                result.add(new PerformanceApi(api, weight));
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("No APIs matched the performance selection. " + config);
        }
        return result;
    }

    private static boolean containsIgnoreCase(Set<String> values, String actual) {
        return values.stream().anyMatch(v -> v.equalsIgnoreCase(actual));
    }
}
