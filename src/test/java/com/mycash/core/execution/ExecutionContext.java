package com.mycash.core.execution;

import com.mycash.core.Utills.APIData;

public class ExecutionContext {

    private final String source;
    private final String workbook;
    private final String sheetName;
    private final APIData apiData;

    public ExecutionContext(
            String source,
            String workbook,
            String sheetName,
            APIData apiData) {

        this.source = source;
        this.workbook = workbook;
        this.sheetName = sheetName;
        this.apiData = apiData;
    }

    public String getSource() {
        return source;
    }

    public String getWorkbook() {
        return workbook;
    }

    public String getSheetName() {
        return sheetName;
    }

    public APIData getApiData() {
        return apiData;
    }

    public String getApiName() {
        return apiData.getApiName();
    }

    @Override
    public String toString() {

        return workbook
                + " -> "
                + sheetName
                + " -> "
                + apiData.getApiName();
    }

}