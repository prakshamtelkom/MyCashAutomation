package com.mycash.core.execution;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ExecutionFilter {

    private final Set<String> workbooks;
    private final Set<String> sheets;
    private final Set<String> apis;
    private final Set<String> methods;
    private final Set<String> sources;
    private final Set<String> tags;

    private final boolean failedOnly;

    private final String environment;
    private final String suite;
    private final String mode;

    private final int threads;

    public ExecutionFilter() {

        workbooks = parse("workbook");
        sheets = parse("sheet");
        apis = parse("api");
        methods = parse("method");
        sources = parse("source");
        tags = parse("tags");

        failedOnly =
                Boolean.parseBoolean(
                        System.getProperty("failedOnly","false"));

        environment =
                System.getProperty("env","qa");

        suite =
                System.getProperty("suite","ALL");

        mode =
                System.getProperty("mode","functional");

        threads =
                Integer.parseInt(
                        System.getProperty("threads","1"));
    }

    private Set<String> parse(String key){

        String value =
                System.getProperty(key);

        if(value==null ||
                value.trim().isEmpty() ||
                value.equalsIgnoreCase("ALL")){

            return Collections.emptySet();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s->!s.isEmpty())
                .collect(Collectors.toSet());
    }

    public boolean shouldExecuteWorkbook(String workbook){

        if(workbooks.isEmpty())
            return true;

        String file =
                new File(workbook).getName();

        return workbooks.stream()
                .anyMatch(w->
                        file.equalsIgnoreCase(w)
                                ||
                                file.toLowerCase().contains(w.toLowerCase()));
    }

    public boolean shouldExecuteSheet(String sheet){

        return sheets.isEmpty()
                || sheets.contains(sheet);
    }

    public boolean shouldExecuteApi(String api){

        return apis.isEmpty()
                || apis.contains(api);
    }

    public boolean shouldExecuteMethod(String method){

        return methods.isEmpty()
                || methods.contains(method.toUpperCase());
    }

    public boolean shouldExecuteSource(String source){

        return sources.isEmpty()
                || sources.contains(source);
    }

    public boolean shouldExecuteTag(String tag){

        return tags.isEmpty()
                || tags.contains(tag);
    }

    public boolean failedOnly(){

        return failedOnly;
    }

    public String getEnvironment(){

        return environment;
    }

    public String getSuite(){

        return suite;
    }

    public String getMode(){

        return mode;
    }

    public int getThreads(){

        return threads;
    }

    @Override
    public String toString(){

        return
                "\n================ Execution Filter ================\n"+
                        "Workbook     : "+(workbooks.isEmpty()?"ALL":workbooks)+"\n"+
                        "Sheet        : "+(sheets.isEmpty()?"ALL":sheets)+"\n"+
                        "API          : "+(apis.isEmpty()?"ALL":apis)+"\n"+
                        "Method       : "+(methods.isEmpty()?"ALL":methods)+"\n"+
                        "Source       : "+(sources.isEmpty()?"ALL":sources)+"\n"+
                        "Tags         : "+(tags.isEmpty()?"ALL":tags)+"\n"+
                        "Suite        : "+suite+"\n"+
                        "Environment  : "+environment+"\n"+
                        "Mode         : "+mode+"\n"+
                        "Threads      : "+threads+"\n"+
                        "Failed Only  : "+failedOnly+"\n"+
                        "==================================================";
    }
}