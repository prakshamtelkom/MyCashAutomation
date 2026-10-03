package com.mycash.core.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExtentManager {

    private static volatile ExtentReports extent;

    private ExtentManager() {
    }

    public static ExtentReports getInstance() {

        if (extent == null) {

            synchronized (ExtentManager.class) {

                if (extent == null) {

                    String reportPath =
                            System.getProperty("user.dir")
                                    + "/target/ExtentReport.html";

                    ExtentSparkReporter spark =
                            new ExtentSparkReporter(reportPath);

                    configureSparkReporter(spark);

                    extent = new ExtentReports();

                    extent.attachReporter(spark);

                    addSystemInformation(extent);
                }
            }
        }

        return extent;
    }

    public static void flush() {

        if (extent != null) {
            extent.flush();
        }
    }

    // =========================================================================
    // SPARK REPORT CONFIGURATION
    // =========================================================================

    private static void configureSparkReporter(
            ExtentSparkReporter spark) {

        spark.config().setTheme(Theme.STANDARD);
        spark.config().setDocumentTitle(
                "MyCash API Automation"
        );
        spark.config().setReportName(
                "MyCash API Regression Report"
        );
        spark.config().setEncoding("UTF-8");
        spark.config().setTimelineEnabled(true);

        /*
         * Presentation-only enhancement.
         *
         * No test execution logic is changed here.
         * The CSS below only improves the built-in Extent dashboard:
         *
         *  - Executive summary cards
         *  - Pass / Fail cards
         *  - Charts
         *  - Tags table
         *  - System / Environment table
         *  - Timeline
         *  - Test list
         *  - Navigation
         */
        spark.config().setCss(buildCss());

        spark.config().setJs(buildJavascript());
    }

    // =========================================================================
    // SYSTEM / ENVIRONMENT INFORMATION
    // =========================================================================

    private static void addSystemInformation(
            ExtentReports extent) {

        extent.setSystemInfo(
                "Framework",
                "RestAssured + TestNG"
        );

        extent.setSystemInfo(
                "Execution",
                "Excel + Postman"
        );

        extent.setSystemInfo(
                "Environment",
                "QA"
        );

        extent.setSystemInfo(
                "Java",
                System.getProperty("java.version")
        );

        extent.setSystemInfo(
                "OS",
                System.getProperty("os.name")
        );

        extent.setSystemInfo(
                "User",
                System.getProperty("user.name")
        );

        extent.setSystemInfo(
                "Report Type",
                "API Regression"
        );

        extent.setSystemInfo(
                "Generated On",
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "dd-MMM-yyyy HH:mm:ss"
                        )
                )
        );
    }

    // =========================================================================
    // EXECUTIVE / DASHBOARD CSS
    // =========================================================================

    private static String buildCss() {

        return
                "/* ============================================================= " +
                        "   MYCASH ENTERPRISE EXTENT REPORT THEME " +
                        "   ============================================================= */" +

                        "body{" +
                        "font-family:'Segoe UI',Arial,sans-serif!important;" +
                        "font-size:14px!important;" +
                        "background:#F6F8FC!important;" +
                        "color:#1E293B!important;" +
                        "}" +

                        "/* Top header */" +
                        ".navbar{" +
                        "background:linear-gradient(135deg,#0B1F3A,#163B70)!important;" +
                        "box-shadow:0 3px 14px rgba(15,39,71,.18)!important;" +
                        "}" +

                        ".navbar-brand," +
                        ".navbar-brand:hover{" +
                        "font-weight:800!important;" +
                        "letter-spacing:.2px!important;" +
                        "}" +

                        "/* Main dashboard area */" +
                        ".dashboard-view{" +
                        "background:#F6F8FC!important;" +
                        "}" +

                        ".dashboard-view .card{" +
                        "border:1px solid #E2E8F0!important;" +
                        "border-radius:14px!important;" +
                        "background:#FFFFFF!important;" +
                        "box-shadow:0 4px 16px rgba(15,39,71,.07)!important;" +
                        "overflow:hidden!important;" +
                        "transition:all .2s ease-in-out!important;" +
                        "}" +

                        ".dashboard-view .card:hover{" +
                        "box-shadow:0 8px 24px rgba(15,39,71,.12)!important;" +
                        "transform:translateY(-1px)!important;" +
                        "}" +

                        "/* Dashboard card headings */" +
                        ".dashboard-view .card-header{" +
                        "background:linear-gradient(135deg,#F8FAFC,#EEF4FF)!important;" +
                        "border-bottom:1px solid #E2E8F0!important;" +
                        "font-weight:700!important;" +
                        "color:#0F2747!important;" +
                        "}" +

                        ".dashboard-view .card-body{" +
                        "background:#FFFFFF!important;" +
                        "}" +

                        "/* Summary numbers */" +
                        ".dashboard-view .card .card-body h3," +
                        ".dashboard-view .card .card-body h4{" +
                        "font-weight:800!important;" +
                        "color:#0F2747!important;" +
                        "}" +

                        "/* Passed / Failed numbers */" +
                        ".dashboard-view .text-success{" +
                        "color:#15803D!important;" +
                        "font-weight:800!important;" +
                        "}" +

                        ".dashboard-view .text-danger{" +
                        "color:#DC2626!important;" +
                        "font-weight:800!important;" +
                        "}" +

                        "/* Extent badges */" +
                        ".badge{" +
                        "border-radius:999px!important;" +
                        "padding:5px 10px!important;" +
                        "font-weight:700!important;" +
                        "font-size:12px!important;" +
                        "letter-spacing:.2px!important;" +
                        "}" +

                        ".badge-success{" +
                        "background:#16A34A!important;" +
                        "color:#FFFFFF!important;" +
                        "}" +

                        ".badge-danger{" +
                        "background:#DC2626!important;" +
                        "color:#FFFFFF!important;" +
                        "}" +

                        ".badge-warning{" +
                        "background:#F59E0B!important;" +
                        "color:#FFFFFF!important;" +
                        "}" +

                        ".badge-info{" +
                        "background:#2563EB!important;" +
                        "color:#FFFFFF!important;" +
                        "}" +

                        "/* Test cards / API list */" +
                        ".test-list-item," +
                        ".test-list-item .test-detail{" +
                        "border-radius:12px!important;" +
                        "}" +

                        ".test-list-item{" +
                        "background:#FFFFFF!important;" +
                        "border:1px solid #E2E8F0!important;" +
                        "margin-bottom:8px!important;" +
                        "box-shadow:0 2px 8px rgba(15,39,71,.04)!important;" +
                        "}" +

                        ".test-list-item:hover{" +
                        "background:#F8FAFC!important;" +
                        "border-color:#BFDBFE!important;" +
                        "}" +

                        "/* Test name */" +
                        ".test-name{" +
                        "font-weight:650!important;" +
                        "color:#334155!important;" +
                        "}" +

                        "/* Tables */" +
                        "table{" +
                        "width:100%!important;" +
                        "font-size:13px!important;" +
                        "border-collapse:separate!important;" +
                        "border-spacing:0!important;" +
                        "background:#FFFFFF!important;" +
                        "border:1px solid #E2E8F0!important;" +
                        "border-radius:12px!important;" +
                        "overflow:hidden!important;" +
                        "}" +

                        "th{" +
                        "background:#F1F5F9!important;" +
                        "color:#334155!important;" +
                        "font-weight:800!important;" +
                        "text-transform:uppercase!important;" +
                        "font-size:11px!important;" +
                        "letter-spacing:.5px!important;" +
                        "padding:11px 12px!important;" +
                        "border-bottom:1px solid #CBD5E1!important;" +
                        "}" +

                        "td{" +
                        "padding:10px 12px!important;" +
                        "vertical-align:top!important;" +
                        "border-bottom:1px solid #E2E8F0!important;" +
                        "color:#334155!important;" +
                        "background:#FFFFFF!important;" +
                        "}" +

                        "tr:last-child td{" +
                        "border-bottom:none!important;" +
                        "}" +

                        "tr:hover td{" +
                        "background:#F8FAFC!important;" +
                        "}" +

                        "/* Tags section - make category names look like chips */" +
                        ".tags-view table td:first-child," +
                        ".tags-view .table td:first-child{" +
                        "font-weight:700!important;" +
                        "color:#0F2747!important;" +
                        "}" +

                        ".tags-view .table td{" +
                        "padding:12px!important;" +
                        "}" +

                        "/* System / Environment section */" +
                        ".system-info table td:first-child," +
                        ".system-info .table td:first-child{" +
                        "font-weight:800!important;" +
                        "color:#0F2747!important;" +
                        "background:#F8FAFC!important;" +
                        "width:32%!important;" +
                        "}" +

                        ".system-info table td:last-child," +
                        ".system-info .table td:last-child{" +
                        "font-weight:600!important;" +
                        "color:#334155!important;" +
                        "}" +

                        "/* Timeline */" +
                        ".timeline-container," +
                        ".timeline{" +
                        "border-radius:14px!important;" +
                        "background:#FFFFFF!important;" +
                        "border:1px solid #E2E8F0!important;" +
                        "box-shadow:0 4px 16px rgba(15,39,71,.05)!important;" +
                        "}" +

                        "/* Request/response JSON */" +
                        "pre{" +
                        "background:#F8FAFC!important;" +
                        "color:#1E293B!important;" +
                        "padding:14px!important;" +
                        "border:1px solid #E2E8F0!important;" +
                        "border-radius:9px!important;" +
                        "overflow:auto!important;" +
                        "font-size:13px!important;" +
                        "line-height:1.55!important;" +
                        "text-shadow:none!important;" +
                        "}" +

                        "/* Collapsible details */" +
                        "details{" +
                        "margin-top:10px!important;" +
                        "margin-bottom:10px!important;" +
                        "}" +

                        "summary{" +
                        "cursor:pointer!important;" +
                        "font-weight:700!important;" +
                        "font-size:14px!important;" +
                        "color:#1D4ED8!important;" +
                        "}" +

                        "/* Navigation icons */" +
                        ".side-nav{" +
                        "background:#FFFFFF!important;" +
                        "border-right:1px solid #E2E8F0!important;" +
                        "}" +

                        "/* Search */" +
                        ".search-box input{" +
                        "border-radius:8px!important;" +
                        "border:1px solid #CBD5E1!important;" +
                        "}" +

                        "/* Scrollbars */" +
                        "::-webkit-scrollbar{" +
                        "width:8px!important;" +
                        "height:8px!important;" +
                        "}" +

                        "::-webkit-scrollbar-track{" +
                        "background:#F1F5F9!important;" +
                        "}" +

                        "::-webkit-scrollbar-thumb{" +
                        "background:#94A3B8!important;" +
                        "border-radius:10px!important;" +
                        "}" +

                        "::-webkit-scrollbar-thumb:hover{" +
                        "background:#64748B!important;" +
                        "}";
    }

    // =========================================================================
    // SMALL PRESENTATION ENHANCEMENTS
    // =========================================================================

    private static String buildJavascript() {
        return
                "document.title='MyCash API Regression Report';" +
                        "window.addEventListener('load',function(){" +

                        // Keep workbook/sheet/API details collapsed when the report opens.
                        "function collapseExtentPanels(){" +
                        "var nodes=document.querySelectorAll('.test-list-item .test-detail,.test-list-item .test-content,.test-detail');" +
                        "nodes.forEach(function(el){el.classList.remove('active');el.removeAttribute('open');});" +
                        "var details=document.querySelectorAll('details');" +
                        "details.forEach(function(d){d.removeAttribute('open');});" +
                        "}" +

                        // Make the standard Extent dashboard terminology match this API report.
                        "function renameDashboardCards(){" +
                        "var root=document.querySelector('.dashboard-view');" +
                        "if(!root){return;}" +
                        "var nodes=root.querySelectorAll('*');" +
                        "nodes.forEach(function(el){" +
                        "if(el.children.length!==0){return;}" +
                        "var t=(el.textContent||'').trim();" +
                        "if(t==='Tests Passed'){el.textContent='Workbooks Passed';}" +
                        "else if(t==='Tests Failed'){el.textContent='Workbooks Failed';}" +
                        "else if(t==='Log events'){el.textContent='API Executions';}" +
                        "else if(t==='Tests'){el.textContent='Workbooks';}" +
                        "});" +
                        "}" +

                        "collapseExtentPanels();" +
                        "renameDashboardCards();" +
                        "setTimeout(collapseExtentPanels,300);" +
                        "setTimeout(renameDashboardCards,300);" +
                        "setTimeout(collapseExtentPanels,1000);" +
                        "setTimeout(renameDashboardCards,1000);" +
                        "});";
    }
}
