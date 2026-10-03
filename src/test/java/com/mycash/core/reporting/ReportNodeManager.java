package com.mycash.core.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public final class ReportNodeManager {

    private static final ExtentReports EXTENT = ExtentManager.getInstance();

    private static final Map<String, ExtentTest> WORKBOOK_NODES =
            new ConcurrentHashMap<>();
    private static final Map<String, ExtentTest> SHEET_NODES =
            new ConcurrentHashMap<>();
    private static final Map<String, ExtentTest> API_NODES =
            new ConcurrentHashMap<>();

    private static final Map<String, ExecutionStats> WORKBOOK_STATS =
            new ConcurrentHashMap<>();
    private static final Map<String, ExecutionStats> SHEET_STATS =
            new ConcurrentHashMap<>();

    private ReportNodeManager() {
    }

    public static ExtentTest getWorkbookNode(String workbook) {
        String workbookName = getWorkbookName(workbook);

        return WORKBOOK_NODES.computeIfAbsent(workbookName, key -> {
            ExtentTest node = EXTENT.createTest("📘 " + workbookName);
            node.assignCategory("Workbook");
            return node;
        });
    }

    public static ExtentTest getSheetNode(String workbook, String sheet) {
        String workbookName = getWorkbookName(workbook);
        String safeSheet = safe(sheet);
        String key = workbookName + "::" + safeSheet;

        return SHEET_NODES.computeIfAbsent(key, k -> {
            ExtentTest node = getWorkbookNode(workbookName)
                    .createNode("📂 " + safeSheet);
            node.assignCategory(safeSheet);
            return node;
        });
    }

    public static ExtentTest createApiNode(
            String workbook,
            String sheet,
            String apiName) {

        String workbookName = getWorkbookName(workbook);
        String safeSheet = safe(sheet);
        String safeApi = safe(apiName);
        String key = workbookName + "::" + safeSheet + "::" + safeApi;

        return API_NODES.computeIfAbsent(key,
                k -> getSheetNode(workbookName, safeSheet)
                        .createNode("🌐 " + safeApi));
    }

    /**
     * Records one API result and refreshes workbook/sheet display names.
     * Call only after duplicate protection has succeeded.
     */
    public static void recordApiResult(
            String workbook,
            String sheet,
            boolean passed) {

        String workbookName = getWorkbookName(workbook);
        String safeSheet = safe(sheet);
        String sheetKey = workbookName + "::" + safeSheet;

        ExecutionStats workbookStats = WORKBOOK_STATS.computeIfAbsent(
                workbookName,
                k -> new ExecutionStats()
        );
        ExecutionStats sheetStats = SHEET_STATS.computeIfAbsent(
                sheetKey,
                k -> new ExecutionStats()
        );

        if (passed) {
            workbookStats.passed.incrementAndGet();
            sheetStats.passed.incrementAndGet();
        } else {
            workbookStats.failed.incrementAndGet();
            sheetStats.failed.incrementAndGet();
        }

        refreshWorkbookName(workbookName, workbookStats);
        refreshSheetName(workbookName, safeSheet, sheetStats);
    }

    private static void refreshWorkbookName(
            String workbookName,
            ExecutionStats stats) {

        ExtentTest node = WORKBOOK_NODES.get(workbookName);
        if (node == null) {
            return;
        }

        node.getModel().setName(
                "📘 " + workbookName
                        + " | " + stats.total() + " APIs"
                        + " | " + stats.passed.get() + " Passed"
                        + " | " + stats.failed.get() + " Failed"
        );
    }

    private static void refreshSheetName(
            String workbookName,
            String sheet,
            ExecutionStats stats) {

        String key = workbookName + "::" + sheet;
        ExtentTest node = SHEET_NODES.get(key);
        if (node == null) {
            return;
        }

        node.getModel().setName(
                "📂 " + sheet
                        + " | " + stats.total() + " APIs"
                        + " | " + stats.passed.get() + " Passed"
                        + " | " + stats.failed.get() + " Failed"
        );
    }

    public static void clear() {
        API_NODES.clear();
        SHEET_NODES.clear();
        WORKBOOK_NODES.clear();
        SHEET_STATS.clear();
        WORKBOOK_STATS.clear();
    }

    private static String getWorkbookName(String workbook) {
        if (workbook == null || workbook.isBlank()) {
            return "Unknown Workbook";
        }
        return new File(workbook).getName();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class ExecutionStats {
        private final AtomicInteger passed = new AtomicInteger();
        private final AtomicInteger failed = new AtomicInteger();

        private int total() {
            return passed.get() + failed.get();
        }
    }
}
