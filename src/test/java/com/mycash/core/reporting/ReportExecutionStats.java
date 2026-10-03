package com.mycash.core.reporting;

import com.mycash.core.model.ApiResult;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ReportExecutionStats {

    private static final Map<String, Counter> WORKBOOK_STATS = new ConcurrentHashMap<>();
    private static final Map<String, Counter> SHEET_STATS = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> RECORDED_APIS = new ConcurrentHashMap<>();

    private static final Counter OVERALL = new Counter();

    private ReportExecutionStats() {}

    public static void record(String workbook, String sheet, ApiResult result) {
        if (result == null) return;

        String wb = workbookName(workbook);
        String sh = safe(sheet, "Unknown Sheet");
        String api = safe(result.getApiName(), "Unknown API");

        // Keep statistics consistent with ReportNodeManager's API de-duplication.
        String executionKey = wb + "::" + sh + "::" + api;
        if (RECORDED_APIS.putIfAbsent(executionKey, Boolean.TRUE) != null) {
            return;
        }

        Counter workbookCounter =
                WORKBOOK_STATS.computeIfAbsent(wb, k -> new Counter());
        Counter sheetCounter =
                SHEET_STATS.computeIfAbsent(wb + "::" + sh, k -> new Counter());

        boolean passed = result.isSuccess();

        OVERALL.increment(passed);
        workbookCounter.increment(passed);
        sheetCounter.increment(passed);
    }

    public static CounterSnapshot overall() {
        return OVERALL.snapshot();
    }

    public static CounterSnapshot workbook(String workbook) {
        return snapshot(WORKBOOK_STATS.get(workbookName(workbook)));
    }

    public static CounterSnapshot sheet(String workbook, String sheet) {
        return snapshot(SHEET_STATS.get(
                workbookName(workbook) + "::" + safe(sheet, "Unknown Sheet")));
    }

    public static Map<String, CounterSnapshot> workbookSnapshots() {
        Map<String, CounterSnapshot> result = new LinkedHashMap<>();
        WORKBOOK_STATS.forEach((k, v) -> result.put(k, v.snapshot()));
        return result;
    }

    public static Map<String, CounterSnapshot> sheetSnapshotsForWorkbook(String workbook) {
        String wb = workbookName(workbook);
        Map<String, CounterSnapshot> result = new LinkedHashMap<>();

        SHEET_STATS.forEach((key, counter) -> {
            String prefix = wb + "::";
            if (key.startsWith(prefix)) {
                result.put(key.substring(prefix.length()), counter.snapshot());
            }
        });

        return result;
    }

    public static String buildOverallHtml() {
        return buildCard("OVERALL EXECUTION", overall());
    }

    public static String buildWorkbookHtml(String workbook) {
        return buildCard(workbookName(workbook), workbook(workbook));
    }

    public static String buildSheetHtml(String workbook, String sheet) {
        return buildCard(sheet, sheet(workbook, sheet));
    }

    public static void clear() {
        WORKBOOK_STATS.clear();
        SHEET_STATS.clear();
        RECORDED_APIS.clear();
        OVERALL.reset();
    }

    private static String buildCard(String title, CounterSnapshot s) {
        double passRate = s.total == 0 ? 0.0 : (s.passed * 100.0 / s.total);

        return "<div style='font-family:Arial,sans-serif;margin:8px 0;padding:14px;"
                + "border:1px solid #dbe4f0;border-radius:10px;background:#f8fbff;'>"
                + "<div style='font-size:15px;font-weight:700;color:#102a43;margin-bottom:10px;'>"
                + escape(title)
                + "</div>"
                + stat("TOTAL", s.total, "#1565C0")
                + stat("PASSED", s.passed, "#1B8A3D")
                + stat("FAILED", s.failed, "#C62828")
                + "<span style='display:inline-block;margin-right:14px;'>"
                + "<b>PASS RATE</b> " + String.format("%.2f", passRate) + "%</span>"
                + "</div>";
    }

    private static String stat(String label, int value, String color) {
        return "<span style='display:inline-block;margin-right:14px;'>"
                + "<b style='color:" + color + ";'>" + label + "</b> " + value
                + "</span>";
    }

    private static CounterSnapshot snapshot(Counter counter) {
        return counter == null ? new CounterSnapshot(0, 0, 0) : counter.snapshot();
    }

    private static String workbookName(String workbook) {
        if (workbook == null || workbook.isBlank()) return "Unknown Workbook";
        return new File(workbook).getName();
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static final class CounterSnapshot {
        private final int total;
        private final int passed;
        private final int failed;

        private CounterSnapshot(int total, int passed, int failed) {
            this.total = total;
            this.passed = passed;
            this.failed = failed;
        }

        public int getTotal() { return total; }
        public int getPassed() { return passed; }
        public int getFailed() { return failed; }
    }

    private static final class Counter {
        private final AtomicInteger total = new AtomicInteger();
        private final AtomicInteger passed = new AtomicInteger();
        private final AtomicInteger failed = new AtomicInteger();

        private void increment(boolean success) {
            total.incrementAndGet();
            if (success) passed.incrementAndGet();
            else failed.incrementAndGet();
        }

        private CounterSnapshot snapshot() {
            return new CounterSnapshot(
                    total.get(),
                    passed.get(),
                    failed.get()
            );
        }

        private void reset() {
            total.set(0);
            passed.set(0);
            failed.set(0);
        }
    }
}
