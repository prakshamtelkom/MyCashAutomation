package com.mycash.core.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.mycash.core.model.ApiResult;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public final class ExtentReportLogger {

    private static final Set<String> LOGGED_APIS =
            ConcurrentHashMap.newKeySet();

    private ExtentReportLogger() {
    }

    public static void logApiExecution(
            String workbook,
            String sheet,
            ApiResult result) {

        if (result == null) {
            return;
        }

        String safeWorkbook = safe(workbook);
        String safeSheet = safe(sheet);
        String safeApi = safe(result.getApiName());

        String executionKey =
                safeWorkbook + "::" + safeSheet + "::" + safeApi;

        // Protect the report from duplicate calls for the same API.
        if (!LOGGED_APIS.add(executionKey)) {
            return;
        }

        ExtentTest apiNode = ReportNodeManager.createApiNode(
                safeWorkbook,
                safeSheet,
                safeApi
        );

        // Update workbook + sheet counters once, after duplicate protection.
        ReportNodeManager.recordApiResult(
                safeWorkbook,
                safeSheet,
                result.isSuccess()
        );

        String html = buildApiDetails(
                safeWorkbook,
                safeSheet,
                result
        );

        // Exactly ONE Extent event per API.
        // Using pass()/fail() keeps Extent's status counters meaningful.
        if (result.isSuccess()) {
            apiNode.pass(html);
        } else {
            apiNode.fail(html);
        }
    }

    /**
     * Call this only when a completely new report lifecycle starts in the
     * same JVM. Normally not required for a single Maven/TestNG execution.
     */
    public static void clearLoggedApis() {
        LOGGED_APIS.clear();
    }

    private static String buildApiDetails(
            String workbook,
            String sheet,
            ApiResult result) {

        StringBuilder html = new StringBuilder(8000);

        html.append("<div style='")
                .append("font-family:Segoe UI,Arial,sans-serif;")
                .append("width:100%;")
                .append("color:#0F172A !important;")
                .append("background:#FFFFFF !important;'>");

        // Extent already renders the PASS/FAIL badge for the single event.
        // Do not add another PASS/FAIL event or banner here.
        html.append(buildExecutionSummary(workbook, sheet, result));

        // One outer collapsed section for request/response/error.
        html.append("<details style='")
                .append("margin-top:12px;")
                .append("border:1px solid #CBD5E1;")
                .append("border-radius:10px;")
                .append("background:#FFFFFF !important;")
                .append("overflow:hidden;'>");

        html.append("<summary style='")
                .append("cursor:pointer;")
                .append("padding:12px 14px;")
                .append("background:#F8FAFC !important;")
                .append("color:#0F2747 !important;")
                .append("font-size:13px;")
                .append("font-weight:700;")
                .append("outline:none;'>")
                .append("▶ &nbsp;View Request / Response Details")
                .append("<span style='float:right;")
                .append("font-size:11px;")
                .append("font-weight:600;")
                .append("color:#64748B !important;'>")
                .append("Click to expand")
                .append("</span>")
                .append("</summary>");

        html.append("<div style='")
                .append("padding:12px;")
                .append("background:#FFFFFF !important;'>");

        html.append(buildRequestBlock(result));

        html.append(buildJsonBlock(
                "📥 Response Body",
                result.getBody(),
                "No response body was returned."
        ));

        if (!result.isSuccess() && hasValue(result.getError())) {
            html.append(buildErrorBlock(result.getError()));
        }

        html.append("</div></details>");
        html.append("</div>");

        return html.toString();
    }

    private static String buildExecutionSummary(
            String workbook,
            String sheet,
            ApiResult result) {

        StringBuilder html = new StringBuilder(3000);

        html.append("<div style='")
                .append("border:1px solid #E2E8F0;")
                .append("border-radius:9px;")
                .append("overflow:hidden;")
                .append("background:#FFFFFF !important;'>");

        html.append("<div style='")
                .append("padding:10px 13px;")
                .append("background:#EEF4FF !important;")
                .append("border-bottom:1px solid #E2E8F0;")
                .append("color:#163B70 !important;")
                .append("font-size:13px;")
                .append("font-weight:800;'>")
                .append("API Execution Summary")
                .append("</div>");

        html.append("<table style='")
                .append("width:100%;")
                .append("border-collapse:collapse;")
                .append("font-size:12px;")
                .append("background:#FFFFFF !important;")
                .append("color:#0F172A !important;'>");

        addRow(html, "Workbook", workbook);
        addRow(html, "Sheet", sheet);
        addRow(html, "API", safe(result.getApiName()));
        addRow(html, "Method", methodBadge(result.getMethod()));
        addRow(html, "Endpoint", safe(result.getEndpoint()));
        addRow(html, "Status Code", statusBadge(result.getStatusCode()));
        addRow(html, "Response Time",
                responseTimeBadge(result.getResponseTimeMs()));

        html.append("</table></div>");

        return html.toString();
    }

    private static void addRow(
            StringBuilder html,
            String key,
            String value) {

        html.append("<tr>")
                .append("<td style='")
                .append("width:150px;")
                .append("padding:8px 10px;")
                .append("font-weight:700;")
                .append("color:#475569 !important;")
                .append("background:#F8FAFC !important;")
                .append("border-bottom:1px solid #E2E8F0;'>")
                .append(escape(key))
                .append("</td>")
                .append("<td style='")
                .append("padding:8px 10px;")
                .append("color:#0F172A !important;")
                .append("background:#FFFFFF !important;")
                .append("border-bottom:1px solid #E2E8F0;")
                .append("word-break:break-word;'>")
                .append(value == null ? "" : value)
                .append("</td>")
                .append("</tr>");
    }

    private static String buildRequestBlock(ApiResult result) {

        String endpoint = hasValue(result.getEndpoint())
                ? result.getEndpoint()
                : "Endpoint was not captured.";

        String method = hasValue(result.getMethod())
                ? result.getMethod().toUpperCase()
                : "UNKNOWN";

        String requestBody = hasValue(result.getRequestBody())
                ? prettyJson(result.getRequestBody())
                : "No request body was supplied for this API.";

        return "<div style='"
                + "margin:10px 0;"
                + "border:1px solid #BFDBFE;"
                + "border-radius:8px;"
                + "overflow:hidden;"
                + "background:#FFFFFF !important;'>"
                + "<div style='"
                + "padding:9px 12px;"
                + "background:#EAF4FF !important;"
                + "border-bottom:1px solid #BFDBFE;"
                + "color:#0F4C81 !important;"
                + "font-size:12px;"
                + "font-weight:800;'>"
                + "📤 Request"
                + "</div>"
                + "<table style='width:100%;border-collapse:collapse;font-size:12px;background:#FFFFFF !important;'>"
                + "<tr>"
                + "<td style='width:150px;padding:8px 10px;font-weight:700;color:#475569 !important;background:#F8FAFC !important;border-bottom:1px solid #E2E8F0;'>Method</td>"
                + "<td style='padding:8px 10px;color:#0F172A !important;background:#FFFFFF !important;border-bottom:1px solid #E2E8F0;'>"
                + methodBadge(method)
                + "</td></tr>"
                + "<tr>"
                + "<td style='width:150px;padding:8px 10px;font-weight:700;color:#475569 !important;background:#F8FAFC !important;border-bottom:1px solid #E2E8F0;'>Endpoint</td>"
                + "<td style='padding:8px 10px;color:#0F172A !important;background:#FFFFFF !important;border-bottom:1px solid #E2E8F0;word-break:break-all;'>"
                + "<code style='font-family:Consolas,Monaco,monospace;font-size:12px;color:#0F172A !important;background:#F8FAFC;padding:4px 6px;border-radius:5px;'>"
                + escape(endpoint)
                + "</code></td></tr>"
                + "</table>"
                + "<div style='padding:9px 12px;background:#F8FAFC !important;border-bottom:1px solid #D7E0EA;color:#0F2747 !important;font-size:12px;font-weight:800;'>Request Body</div>"
                + "<pre style='margin:0;padding:14px;background:#F8FAFC !important;color:#0F172A !important;font-family:Consolas,Monaco,monospace;font-size:12px;line-height:1.55;white-space:pre-wrap;overflow:auto;max-height:420px;text-shadow:none !important;'>"
                + escape(requestBody)
                + "</pre>"
                + "</div>";
    }

    private static String buildJsonBlock(
            String title,
            String content,
            String emptyMessage) {

        boolean hasContent = hasValue(content);
        String body = hasContent ? prettyJson(content) : emptyMessage;

        return "<div style='"
                + "margin:10px 0;"
                + "border:1px solid #D7E0EA;"
                + "border-radius:8px;"
                + "overflow:hidden;"
                + "background:#FFFFFF !important;'>"
                + "<div style='"
                + "padding:9px 12px;"
                + "background:#F1F5F9 !important;"
                + "border-bottom:1px solid #D7E0EA;"
                + "color:#0F2747 !important;"
                + "font-size:12px;"
                + "font-weight:800;'>"
                + escape(title)
                + "</div>"
                + "<pre style='"
                + "display:block !important;box-sizing:border-box !important;width:100% !important;"
                + "max-height:360px !important;overflow:auto !important;margin:0 !important;"
                + "padding:14px !important;background:#F8FAFC !important;color:#0F172A !important;"
                + "font-family:Consolas,Monaco,\"Courier New\",monospace !important;"
                + "font-size:12px !important;line-height:1.55 !important;font-weight:500 !important;"
                + "white-space:pre-wrap !important;word-break:break-word !important;"
                + "text-shadow:none !important;'>"
                + escape(body)
                + "</pre>"
                + "</div>";
    }

    private static String buildErrorBlock(String error) {
        return "<div style='margin-top:10px;border:1px solid #FECACA;border-radius:8px;overflow:hidden;background:#FFF7F7 !important;'>"
                + "<div style='padding:9px 12px;background:#FEF2F2 !important;color:#991B1B !important;font-size:12px;font-weight:800;'>❌ Error Details</div>"
                + "<pre style='margin:0;padding:12px;background:#FFF7F7 !important;color:#7F1D1D !important;white-space:pre-wrap !important;word-break:break-word !important;font-family:Consolas,Monaco,\"Courier New\",monospace;font-size:12px;text-shadow:none !important;'>"
                + escape(error)
                + "</pre></div>";
    }

    private static String methodBadge(String method) {
        String m = safe(method).toUpperCase();
        if (m.isEmpty()) {
            return createBadge("N/A", "#64748B");
        }

        String bg;
        switch (m) {
            case "GET": bg = "#2563EB"; break;
            case "POST": bg = "#16A34A"; break;
            case "PUT": bg = "#7C3AED"; break;
            case "PATCH": bg = "#9333EA"; break;
            case "DELETE": bg = "#DC2626"; break;
            default: bg = "#64748B";
        }
        return createBadge(m, bg);
    }

    private static String statusBadge(int statusCode) {
        String bg;
        if (statusCode >= 200 && statusCode < 300) {
            bg = "#16A34A";
        } else if (statusCode >= 400 && statusCode < 500) {
            bg = "#EA580C";
        } else if (statusCode >= 500) {
            bg = "#DC2626";
        } else {
            bg = "#64748B";
        }
        return createBadge(String.valueOf(statusCode), bg);
    }

    private static String responseTimeBadge(long ms) {
        String bg;
        if (ms <= 500) {
            bg = "#16A34A";
        } else if (ms <= 1000) {
            bg = "#D97706";
        } else {
            bg = "#DC2626";
        }
        return createBadge(ms + " ms", bg);
    }

    private static String createBadge(String text, String background) {
        return "<span style='display:inline-block;padding:4px 9px;border-radius:999px;"
                + "background:" + background + " !important;color:#FFFFFF !important;"
                + "font-size:11px;font-weight:800;white-space:nowrap;'>"
                + escape(text)
                + "</span>";
    }

    private static String prettyJson(String json) {
        if (!hasValue(json)) {
            return "";
        }

        String value = json.trim();
        if (value.startsWith("{") || value.startsWith("[")) {
            try {
                return simplePrettyJson(value);
            } catch (Exception ignored) {
                // Keep original body if formatting fails.
            }
        }
        return value;
    }

    private static String simplePrettyJson(String json) {
        StringBuilder out = new StringBuilder(json.length() + 200);
        int indent = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                out.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\' && inString) {
                out.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                out.append(c);
                continue;
            }

            if (!inString) {
                if (c == '{' || c == '[') {
                    out.append(c).append('\n');
                    indent++;
                    appendIndent(out, indent);
                    continue;
                }
                if (c == '}' || c == ']') {
                    out.append('\n');
                    indent = Math.max(0, indent - 1);
                    appendIndent(out, indent);
                    out.append(c);
                    continue;
                }
                if (c == ',') {
                    out.append(c).append('\n');
                    appendIndent(out, indent);
                    continue;
                }
                if (c == ':') {
                    out.append(": ");
                    continue;
                }
            }

            if (!Character.isWhitespace(c) || inString) {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static void appendIndent(StringBuilder out, int indent) {
        out.append("    ".repeat(indent));
    }

    private static boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
