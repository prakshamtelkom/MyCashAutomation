package com.mycash.performance.model;

import com.mycash.core.Utills.APIData;

public record PerformanceApi(APIData api, long weight) {
    public String name() { return api.getApiName(); }
}
