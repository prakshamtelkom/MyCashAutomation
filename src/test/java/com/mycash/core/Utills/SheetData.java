package com.mycash.core.Utills;

import java.util.Map;

public class SheetData {

    private final String sheetName;
    private final Map<String, APIData> apiMap;

    public SheetData(String sheetName,
                     Map<String, APIData> apiMap) {

        this.sheetName = sheetName;
        this.apiMap = apiMap;
    }

    public String getSheetName() {
        return sheetName;
    }

    public Map<String, APIData> getApiMap() {
        return apiMap;
    }
}