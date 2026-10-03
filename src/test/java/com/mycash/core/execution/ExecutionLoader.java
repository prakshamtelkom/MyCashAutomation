package com.mycash.core.execution;

import com.mycash.core.Utills.APIData;
import com.mycash.core.Utills.ExcelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecutionLoader {

    private final ExecutionFilter filter;

    public ExecutionLoader() {
        this.filter = new ExecutionFilter();
    }

    /**
     * Reads one or more Excel workbooks and prepares execution contexts.
     */
    public List<ExecutionContext> loadExcelTests(String... workbooks) {

        List<ExecutionContext> executionList = new ArrayList<>();

        System.out.println(filter);

        for (String workbook : workbooks) {

            Map<String, Map<String, APIData>> workbookData =
                    ExcelUtils.getExcelDataBySheet(workbook);

            for (Map.Entry<String, Map<String, APIData>> sheet :
                    workbookData.entrySet()) {

                String sheetName = sheet.getKey();

                // Skip unwanted sheets
                if (!filter.shouldExecuteSheet(sheetName)) {
                    continue;
                }

                for (APIData api : sheet.getValue().values()) {

                    executionList.add(

                            new ExecutionContext(

                                    "Excel",

                                    workbook,

                                    sheetName,

                                    api

                            )
                    );

                }

            }

        }

        return executionList;
    }

}