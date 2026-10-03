package com.mycash.performance.report;

import com.mycash.performance.config.PerformanceConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates an offline-friendly management dashboard from the JMeter CSV/JTL.
 * It intentionally has no external CSS/JS dependencies so index.html can be
 * opened directly from an extracted ZIP using file:// in a normal browser.
 */
public final class PerformanceReportGenerator {

    private static final DecimalFormat DF = new DecimalFormat("0.##");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
            .withLocale(Locale.ENGLISH).withZone(ZoneId.systemDefault());

    private PerformanceReportGenerator() {}

    public static void generate(Path jtlFile, Path reportDir, PerformanceConfig config,
                                int apiCount, String jmeterDashboardRelativePath) throws IOException {
        if (!Files.isRegularFile(jtlFile)) {
            throw new IOException("Performance JTL was not generated: " + jtlFile);
        }
        Files.createDirectories(reportDir);

        List<Sample> samples = readJtl(jtlFile);
        if (samples.isEmpty()) {
            throw new IOException("Performance JTL contains no samples: " + jtlFile);
        }

        Metrics metrics = Metrics.from(samples);
        String html = render(metrics, config, apiCount, jmeterDashboardRelativePath);
        Files.writeString(reportDir.resolve("index.html"), html, StandardCharsets.UTF_8);
    }

    private static List<Sample> readJtl(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) return List.of();
            List<String> headers = parseCsv(headerLine);
            Map<String, Integer> index = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) index.put(headers.get(i).trim(), i);

            Integer ts = first(index, "timeStamp", "timestamp");
            Integer elapsed = first(index, "elapsed");
            Integer label = first(index, "label");
            Integer code = first(index, "responseCode");
            Integer success = first(index, "success");
            Integer latency = first(index, "Latency", "latency");
            Integer threads = first(index, "allThreads");
            if (ts == null || elapsed == null || label == null || success == null) {
                throw new IOException("Unsupported JTL format. Required columns: timeStamp, elapsed, label, success");
            }

            List<Sample> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> row = parseCsv(line);
                long timestamp = parseLong(value(row, ts), 0);
                long duration = parseLong(value(row, elapsed), 0);
                String sampler = value(row, label);
                String responseCode = code == null ? "-" : value(row, code);
                boolean ok = Boolean.parseBoolean(value(row, success));
                long lat = latency == null ? duration : parseLong(value(row, latency), duration);
                int activeThreads = threads == null ? 0 : (int) parseLong(value(row, threads), 0);
                result.add(new Sample(timestamp, duration, sampler, responseCode, ok, lat, activeThreads));
            }
            return result;
        }
    }

    private static Integer first(Map<String, Integer> index, String... names) {
        for (String name : names) if (index.containsKey(name)) return index.get(name);
        return null;
    }

    private static String value(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }

    private static long parseLong(String value, long fallback) {
        try { return Long.parseLong(value.trim()); } catch (Exception e) { return fallback; }
    }

    private static List<String> parseCsv(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"'); i++;
                } else quoted = !quoted;
            } else if (c == ',' && !quoted) {
                values.add(current.toString()); current.setLength(0);
            } else current.append(c);
        }
        values.add(current.toString());
        return values;
    }

    private static String render(Metrics m, PerformanceConfig config, int apiCount, String jmeterDashboardPath) {
        String generated = TIME.format(Instant.now());
        String scenario = esc(config.getScenario());
        String baseUrl = esc(config.getBaseUrl());
        String endpointRows = m.byLabel.values().stream()
                .sorted(Comparator.comparingLong((EndpointMetrics e) -> e.count).reversed())
                .map(PerformanceReportGenerator::endpointRow).collect(Collectors.joining());
        String codeRows = m.byCode.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).map(e -> "<tr><td><b>" + esc(e.getKey()) + "</b></td><td>" + e.getValue() + "</td><td>" + pct(e.getValue(), m.total) + "%</td></tr>")
                .collect(Collectors.joining());

        return "<!doctype html><html><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<title>MyCash Performance Report</title><style>"
                + "body{font-family:Arial,Helvetica,sans-serif;margin:0;background:#f5f7fa;color:#17202a}"
                + ".wrap{max-width:1400px;margin:auto;padding:28px}.hero{background:#075e54;color:white;padding:28px;border-radius:12px;margin-bottom:18px}"
                + "h1{margin:0 0 8px;font-size:30px}.muted{opacity:.82;font-size:13px}.grid{display:grid;grid-template-columns:repeat(6,1fr);gap:12px;margin:18px 0}"
                + ".card{background:white;border-radius:10px;padding:18px;box-shadow:0 1px 4px #0001}.label{font-size:12px;color:#667085;text-transform:uppercase}.value{font-size:25px;font-weight:700;margin-top:7px}"
                + ".section{background:white;border-radius:10px;padding:20px;margin:16px 0;box-shadow:0 1px 4px #0001}h2{font-size:18px;margin:0 0 14px}"
                + ".cols{display:grid;grid-template-columns:1fr 1fr;gap:16px}.chart{width:100%;overflow:auto}.chart svg{width:100%;min-width:600px;height:280px}"
                + "table{width:100%;border-collapse:collapse;font-size:13px}th,td{padding:9px;border-bottom:1px solid #eaecf0;text-align:right}th:first-child,td:first-child{text-align:left}th{background:#f8fafc;color:#475467}"
                + ".ok{color:#067647}.bad{color:#b42318}.meta{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.pill{background:#eef4ff;padding:10px;border-radius:8px;font-size:13px}"
                + "a{color:#075e54;font-weight:600}.foot{font-size:12px;color:#667085;margin-top:18px}@media(max-width:900px){.grid{grid-template-columns:repeat(2,1fr)}.cols{grid-template-columns:1fr}.meta{grid-template-columns:1fr 1fr}}"
                + "</style></head><body><div class='wrap'>"
                + "<div class='hero'><h1>MyCash API Performance Report</h1><div class='muted'>Management-ready offline dashboard • Generated " + generated + "</div></div>"
                + "<div class='meta'><div class='pill'><b>Scenario</b><br>" + scenario + "</div><div class='pill'><b>Target</b><br>" + baseUrl + "</div><div class='pill'><b>APIs configured</b><br>" + apiCount + "</div><div class='pill'><b>Samples analyzed</b><br>" + m.total + "</div></div>"
                + "<div class='grid'>"
                + card("Total Requests", fmt(m.total), "") + card("Successful", fmt(m.success), "ok") + card("Errors", fmt(m.errors), m.errors == 0 ? "ok" : "bad")
                + card("Error Rate", pct(m.errors, m.total) + "%", m.errors == 0 ? "ok" : "bad") + card("Throughput", DF.format(m.throughput) + " req/s", "") + card("Avg Response", DF.format(m.avg) + " ms", "")
                + card("Min", m.min + " ms", "") + card("Max", m.max + " ms", "") + card("P90", DF.format(m.p90) + " ms", "") + card("P95", DF.format(m.p95) + " ms", "") + card("P99", DF.format(m.p99) + " ms", "") + card("Peak Threads", fmt(m.peakThreads), "")
                + "</div>"
                + "<div class='section'><h2>Response Time Distribution</h2><div class='chart'>" + histogram(m) + "</div></div>"
                + "<div class='cols'><div class='section'><h2>Throughput Over Time</h2><div class='chart'>" + timeChart(m, true) + "</div></div><div class='section'><h2>Average Response Time Over Time</h2><div class='chart'>" + timeChart(m, false) + "</div></div></div>"
                + "<div class='cols'><div class='section'><h2>HTTP Response Distribution</h2><table><thead><tr><th>Response Code</th><th>Requests</th><th>Share</th></tr></thead><tbody>" + codeRows + "</tbody></table></div>"
                + "<div class='section'><h2>Configured Thresholds</h2><table><tbody><tr><td>P95 threshold</td><td>" + threshold(config.getP95Ms()) + "</td></tr><tr><td>P99 threshold</td><td>" + threshold(config.getP99Ms()) + "</td></tr><tr><td>Max error rate</td><td>" + DF.format(config.getErrorRatePercent()) + "%</td></tr></tbody></table><p class='foot'>Threshold pass/fail is enforced by the performance test. This page summarizes the measured JTL results.</p></div></div>"
                + "<div class='section'><h2>API / Sampler Summary</h2><div style='overflow:auto'><table><thead><tr><th>API / Sampler</th><th>Requests</th><th>Errors</th><th>Error %</th><th>Avg ms</th><th>Min</th><th>Max</th><th>P90</th><th>P95</th><th>P99</th></tr></thead><tbody>" + endpointRows + "</tbody></table></div></div>"
                + "<div class='section'><h2>Detailed JMeter Dashboard</h2><p>The official JMeter dashboard is included separately in the report package.</p><p><a href='" + escAttr(jmeterDashboardPath) + "'>Open official JMeter dashboard</a></p><p class='foot'>The management dashboard above is deliberately self-contained and can be opened directly from an extracted ZIP without a local web server.</p></div>"
                + "<div class='foot'>Source: " + esc(jtlFileName(config)) + " • Generated from JMeter JTL samples.</div>"
                + "</div></body></html>";
    }

    private static String jtlFileName(PerformanceConfig config) { return "performance-results.jtl"; }
    private static String threshold(long ms) { return ms > 0 ? ms + " ms" : "Not configured"; }
    private static String card(String label, String value, String cls) { return "<div class='card'><div class='label'>" + label + "</div><div class='value " + cls + "'>" + value + "</div></div>"; }
    private static String endpointRow(EndpointMetrics e) {
        return "<tr><td>" + esc(e.label) + "</td><td>" + e.count + "</td><td>" + e.errors + "</td><td>" + pct(e.errors,e.count) + "%</td><td>" + DF.format(e.avg()) + "</td><td>" + e.min + "</td><td>" + e.max + "</td><td>" + DF.format(percentile(e.times, .90)) + "</td><td>" + DF.format(percentile(e.times, .95)) + "</td><td>" + DF.format(percentile(e.times, .99)) + "</td></tr>";
    }

    private static String histogram(Metrics m) {
        int[] buckets = {0,100,250,500,1000,2000,5000,10000,Integer.MAX_VALUE};
        String[] labels = {"<100","100-250","250-500","500-1s","1-2s","2-5s","5-10s","10s+"};
        long[] counts = new long[labels.length];
        for (long t : m.times) { int b=0; while(b<8 && t>=buckets[b+1]) b++; counts[b]++; }
        return barChart(labels, counts, "Response time (ms)");
    }

    private static String barChart(String[] labels, long[] values, String axis) {
        int w=900,h=280,left=55,bottom=65,top=25,plotW=w-left-20,plotH=h-top-bottom; long max=1; for(long v:values) max=Math.max(max,v);
        StringBuilder s=new StringBuilder("<svg viewBox='0 0 "+w+" "+h+"' xmlns='http://www.w3.org/2000/svg'><line x1='"+left+"' y1='"+(h-bottom)+"' x2='"+(w-20)+"' y2='"+(h-bottom)+"' stroke='#98a2b3'/>");
        double bw=plotW/(double)values.length*0.68;
        for(int i=0;i<values.length;i++){double x=left+i*plotW/values.length+(plotW/values.length-bw)/2;double bh=values[i]*plotH/(double)max;double y=h-bottom-bh;s.append("<rect x='"+x+"' y='"+y+"' width='"+bw+"' height='"+bh+"' fill='#075e54'/><text x='"+(x+bw/2)+"' y='"+(y-5)+"' text-anchor='middle' font-size='11'>"+values[i]+"</text><text x='"+(x+bw/2)+"' y='"+(h-bottom+20)+"' text-anchor='middle' font-size='11'>"+esc(labels[i])+"</text>");}
        s.append("<text x='"+left+"' y='15' font-size='11' fill='#667085'>"+esc(axis)+"</text></svg>"); return s.toString();
    }

    private static String timeChart(Metrics m, boolean throughput) {
        List<TimePoint> points=m.timePoints;
        int w=900,h=280,left=55,bottom=40,top=25,right=20;double pw=w-left-right,ph=h-top-bottom;double max=1;for(TimePoint p:points)max=Math.max(max,throughput?p.rps:p.avg);
        StringBuilder s=new StringBuilder("<svg viewBox='0 0 "+w+" "+h+"' xmlns='http://www.w3.org/2000/svg'><line x1='"+left+"' y1='"+(h-bottom)+"' x2='"+(w-right)+"' y2='"+(h-bottom)+"' stroke='#98a2b3'/><line x1='"+left+"' y1='"+top+"' x2='"+left+"' y2='"+(h-bottom)+"' stroke='#98a2b3'/>");
        if(points.size()>0){StringBuilder poly=new StringBuilder();for(int i=0;i<points.size();i++){TimePoint p=points.get(i);double x=left+(points.size()==1?0:i*pw/(points.size()-1));double v=throughput?p.rps:p.avg;double y=h-bottom-(v/max)*ph;poly.append(i==0?"":" ").append(x).append(',').append(y);s.append("<circle cx='"+x+"' cy='"+y+"' r='2.5' fill='#075e54'/>");}s.append("<polyline points='"+poly+"' fill='none' stroke='#075e54' stroke-width='2'/>");}
        s.append("<text x='"+left+"' y='15' font-size='11' fill='#667085'>"+(throughput?"Requests/sec":"Average response time (ms)")+"</text></svg>");return s.toString();
    }

    private static double percentile(List<Long> values, double p) { if(values.isEmpty())return 0;List<Long> copy=new ArrayList<>(values);copy.sort(Long::compare);int idx=(int)Math.ceil(p*copy.size())-1;return copy.get(Math.max(0,Math.min(idx,copy.size()-1))); }
    private static String pct(long n,long total){return DF.format(total==0?0:n*100.0/total);}
    private static String fmt(long n){return String.format(Locale.ENGLISH,"%,d",n);}
    private static String esc(String s){if(s==null)return "";return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");}
    private static String escAttr(String s){return esc(s).replace("'","&#39;");}

    private record Sample(long timestamp,long duration,String label,String responseCode,boolean success,long latency,int activeThreads) {}

    private static final class EndpointMetrics {
        final String label; final List<Long> times=new ArrayList<>(); long count,errors,min=Long.MAX_VALUE,max=Long.MIN_VALUE,total;
        EndpointMetrics(String label){this.label=label;}
        void add(Sample s){count++;if(!s.success)errors++;times.add(s.duration);total+=s.duration;min=Math.min(min,s.duration);max=Math.max(max,s.duration);}
        double avg(){return count==0?0:total/(double)count;}
    }

    private static final class TimePoint { long bucket; long count; double rps,avg; TimePoint(long bucket,long count,double avg){this.bucket=bucket;this.count=count;this.rps=count;this.avg=avg;} }

    private static final class Metrics {
        long total,success,errors,min=Long.MAX_VALUE,max=Long.MIN_VALUE,peakThreads;double avg,throughput,p90,p95,p99;List<Long> times=new ArrayList<>();Map<String,EndpointMetrics> byLabel=new LinkedHashMap<>();Map<String,Long> byCode=new LinkedHashMap<>();List<TimePoint> timePoints=new ArrayList<>();
        static Metrics from(List<Sample> samples){Metrics m=new Metrics();long totalTime=0;long first=Long.MAX_VALUE,last=Long.MIN_VALUE;Map<Long,List<Sample>> buckets=new LinkedHashMap<>();for(Sample s:samples){m.total++;if(s.success)m.success++;else m.errors++;m.min=Math.min(m.min,s.duration);m.max=Math.max(m.max,s.duration);totalTime+=s.duration;m.times.add(s.duration);m.peakThreads=Math.max(m.peakThreads,s.activeThreads);first=Math.min(first,s.timestamp);last=Math.max(last,s.timestamp);m.byCode.merge(s.responseCode,1L,Long::sum);m.byLabel.computeIfAbsent(s.label,EndpointMetrics::new).add(s);long bucket=s.timestamp/1000; buckets.computeIfAbsent(bucket,k->new ArrayList<>()).add(s);}m.avg=m.total==0?0:totalTime/(double)m.total;m.p90=percentile(m.times,.90);m.p95=percentile(m.times,.95);m.p99=percentile(m.times,.99);double seconds=Math.max(.001,(last-first+1000)/1000.0);m.throughput=m.total/seconds;for(var e:buckets.entrySet()){long sum=0;for(Sample s:e.getValue())sum+=s.duration;m.timePoints.add(new TimePoint(e.getKey(),e.getValue().size(),sum/(double)e.getValue().size()));}return m;}
    }
}
