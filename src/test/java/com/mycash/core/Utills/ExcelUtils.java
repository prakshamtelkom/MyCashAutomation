package com.mycash.core.Utills;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

public class ExcelUtils {

    public static Map<String, Map<String, APIData>> getExcelDataBySheet(
            String filePath) {

        return getExcelDataBySheet(filePath, null);
    }

    public static Map<String, Map<String, APIData>> getExcelDataBySheet(
            String filePath,
            String sheetFilter) {

        DataFormatter formatter = new DataFormatter();
        Map<String, Map<String, APIData>> workbookData = new LinkedHashMap<>();

        boolean runAllSheets =
                sheetFilter == null
                        || sheetFilter.trim().isEmpty()
                        || sheetFilter.equalsIgnoreCase("ALL");

        boolean sheetFound = false;

        try (InputStream inputStream = new FileInputStream(resolvePath(filePath));
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

                Sheet sheet = workbook.getSheetAt(i);

                if (!runAllSheets &&
                        !sheet.getSheetName().equalsIgnoreCase(sheetFilter)) {
                    continue;
                }

                sheetFound = true;

                Map<String, APIData> sheetApis = new LinkedHashMap<>();

                for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {

                    Row row = sheet.getRow(rowNum);

                    if (row == null) {
                        continue;
                    }

                    String apiName = formatter.formatCellValue(row.getCell(0));
                    String endpoint = formatter.formatCellValue(row.getCell(1));
                    String method = formatter.formatCellValue(row.getCell(2));
                    String requestBody = formatter.formatCellValue(row.getCell(3));
                    String responseBody = formatter.formatCellValue(row.getCell(4));

                    if (apiName == null || apiName.trim().isEmpty()) {
                        continue;
                    }

                    APIData apiData =
                            new APIData(
                                    apiName,
                                    endpoint,
                                    method,
                                    requestBody,
                                    responseBody);

                    sheetApis.put(apiName, apiData);
                }

                workbookData.put(sheet.getSheetName(), sheetApis);
            }

            if (!runAllSheets && !sheetFound) {
                throw new RuntimeException(
                        "Sheet '" + sheetFilter +
                                "' not found in workbook : " + filePath);
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to read excel : " + filePath, e);
        }

        return workbookData;
    }
    public static Map<String, APIData> getExcelData(String... filePaths) {

        DataFormatter formatter = new DataFormatter();
        Map<String, APIData> apiCache = new HashMap<>();

        for (String filePath : filePaths) {

            try (InputStream inputStream = new FileInputStream(resolvePath(filePath));
                 Workbook workbook = new XSSFWorkbook(inputStream)) {

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                        Row row = sheet.getRow(rowNum);
                        if (row == null) continue;

                        String apiName = formatter.formatCellValue(row.getCell(0));
                        String endPoint = formatter.formatCellValue(row.getCell(1));
                        String method = formatter.formatCellValue(row.getCell(2));
                        String requestBody = formatter.formatCellValue(row.getCell(3));
                        String responseBody = formatter.formatCellValue(row.getCell(4));

                        if (apiName == null || apiName.trim().isEmpty()) continue;

                        APIData data = new APIData(apiName, endPoint, method, requestBody, responseBody);
                        apiCache.put(apiName, data);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException("Failed to read Excel file: " + filePath, e);
            }
        }

        return apiCache;
    }

    private static String resolvePath(String filePath) {

        File file = new File(filePath);
        if (file.exists()) {
            return file.getAbsolutePath();
        }

        File rootFile = new File(System.getProperty("user.dir"), filePath);
        if (rootFile.exists()) {
            return rootFile.getAbsolutePath();
        }

        File resourcesFile = new File(System.getProperty("user.dir") + "/src/main/resources/" + new File(filePath).getName());
        if (resourcesFile.exists()) {
            return resourcesFile.getAbsolutePath();
        }

        System.out.println("FILE NOT FOUND");
        System.out.println("Path1: " + file.getAbsolutePath());
        System.out.println("Path2: " + rootFile.getAbsolutePath());
        System.out.println("Path3: " + resourcesFile.getAbsolutePath());

        throw new RuntimeException("Excel file not found: " + filePath);
    }
}
