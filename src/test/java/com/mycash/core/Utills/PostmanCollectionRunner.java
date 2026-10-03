package com.mycash.core.Utills;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycash.core.config.ConfigReader;
import com.mycash.core.model.ApiResult;

import io.restassured.response.Response;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;


public class PostmanCollectionRunner {


    private final JsonNode collection;

    private final Map<String, String> env =
            new HashMap<>();


    private static final Pattern VAR_PATTERN =
            Pattern.compile("\\{\\{(.*?)}}");


    private final ObjectMapper mapper =
            new ObjectMapper();



    public PostmanCollectionRunner(
            String resourcePath) throws Exception {


        InputStream stream =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(resourcePath);


        if(stream == null){

            throw new RuntimeException(
                    "Collection not found : "
                            + resourcePath
            );

        }


        collection =
                mapper.readTree(stream);


        initializeEnvironment();

    }





    private void initializeEnvironment(){


        env.clear();


        env.put(
                "baseUrl",
                ConfigReader.get("baseUrl")
        );


        env.put(
                "token",
                ""
        );


        env.put(
                "deviceId",
                getProperty("subscriber.deviceId")
        );


        env.put(
                "deviceType",
                getProperty("subscriber.deviceType")
        );


        env.put(
                "userType",
                getProperty("subscriber.userType")
        );


        env.put(
                "msisdn",
                getProperty("subscriber.msisdn")
        );


        env.put(
                "notificationToken",
                getProperty("subscriber.notificationToken")
        );


        env.put(
                "pincode",
                getProperty("subscriber.pincode")
        );


        env.put(
                "pinCode",
                getProperty("subscriber.pinCode")
        );


        printEnvironment();

    }





    private String getProperty(String key){


        String value =
                ConfigReader.get(key);



        if(value == null){

            System.out.println(
                    "WARNING : Missing property -> "
                            + key
            );

            return "";

        }


        return value.trim();

    }





    public Map<String, ApiResult> runAll(){


        Map<String, ApiResult> results =
                new LinkedHashMap<>();


        JsonNode items =
                collection.path("item");


        if(items.isArray()){


            for(JsonNode item : items){


                executeItem(
                        item,
                        results
                );

            }

        }


        return results;

    }





    private void executeItem(
            JsonNode item,
            Map<String, ApiResult> results){



        if(item.path("disabled")
                .asBoolean(false)){


            return;

        }



        if(item.has("item")){


            for(JsonNode child :
                    item.get("item")){


                executeItem(
                        child,
                        results
                );

            }


            return;

        }



        String name =
                item.path("name")
                        .asText();



        String method = "";

        String url = "";



        try{


            JsonNode request =
                    item.path("request");



            method =
                    request.path("method")
                            .asText();



            url =
                    resolveUrl(
                            request.path("url")
                    );



            Map<String,String> headers =
                    extractHeaders(
                            request.path("header")
                    );



            String body =
                    extractBody(
                            request.path("body")
                    );



            validateVariables(
                    url,
                    body
            );



            System.out.println(
                    "Executing Postman API : "
                            + name
            );



            long start =
                    System.currentTimeMillis();



            Response response =
                    executeApi(
                            method,
                            url,
                            headers,
                            body
                    );



            long elapsed =
                    System.currentTimeMillis()
                            - start;



            captureToken(response);



            ApiResult result =
                    new ApiResult(
                            name,
                            "Postman",
                            url,
                            method,
                            response,
                            elapsed,
                            null
                    );



            results.put(
                    name,
                    result
            );



        }
        catch(Exception e){



            ApiResult result =
                    new ApiResult(
                            name,
                            "Postman",
                            url,
                            method,
                            null,
                            0,
                            e.getMessage()
                    );



            results.put(
                    name,
                    result
            );

        }


    }






    private String resolveUrl(JsonNode urlNode){


        if(urlNode == null ||
                urlNode.isMissingNode()){


            return "";

        }



        String raw =
                urlNode.isTextual()
                        ?
                        urlNode.asText()
                        :
                        urlNode.path("raw")
                                .asText();



        return replaceVariables(raw);

    }





    private String replaceVariables(
            String input){


        if(input == null){

            return null;

        }



        Matcher matcher =
                VAR_PATTERN.matcher(input);



        StringBuffer sb =
                new StringBuffer();



        while(matcher.find()){


            String variable =
                    matcher.group(1)
                            .trim();



            String value =
                    env.getOrDefault(
                            variable,
                            ""
                    );



            matcher.appendReplacement(
                    sb,
                    Matcher.quoteReplacement(value)
            );

        }



        matcher.appendTail(sb);



        return sb.toString();

    }





    private Map<String,String> extractHeaders(
            JsonNode headerNode){


        Map<String,String> headers =
                new LinkedHashMap<>();



        if(headerNode != null &&
                headerNode.isArray()){


            for(JsonNode h : headerNode){


                if(h.path("disabled")
                        .asBoolean(false)){


                    continue;

                }



                String key =
                        h.path("key")
                                .asText();



                String value =
                        replaceVariables(
                                h.path("value")
                                        .asText()
                        );



                if(!key.isBlank()){


                    headers.put(
                            key,
                            value
                    );

                }


            }

        }



        headers.putIfAbsent(
                "Content-Type",
                "application/json"
        );



        if(!env.get("token")
                .isBlank()){


            headers.putIfAbsent(
                    "Authorization",
                    "Bearer "
                            +
                            env.get("token")
            );

        }



        return headers;

    }





    private String extractBody(JsonNode bodyNode){


        if(bodyNode == null ||
                bodyNode.isMissingNode()){


            return "";

        }



        if("raw".equalsIgnoreCase(
                bodyNode.path("mode")
                        .asText())){


            return replaceVariables(
                    bodyNode.path("raw")
                            .asText()
            );

        }



        return "";

    }





    private void validateVariables(
            String url,
            String body){


        if(url.contains("{{")){


            throw new RuntimeException(
                    "Unresolved URL variable : "
                            + url
            );

        }



        if(body != null &&
                body.contains("{{")){


            throw new RuntimeException(
                    "Unresolved Body variable : "
                            + body
            );

        }


    }





    private Response executeApi(
            String method,
            String url,
            Map<String,String> headers,
            String body){


        var request =
                given()
                        .relaxedHTTPSValidation()
                        .headers(headers);



        if(body != null &&
                !body.isBlank()){


            request.body(body);

        }



        switch(method.toUpperCase()){


            case "GET":

                return request.get(url);


            case "POST":

                return request.post(url);


            case "PUT":

                return request.put(url);


            case "PATCH":

                return request.patch(url);


            case "DELETE":

                return request.delete(url);


            default:

                throw new RuntimeException(
                        "Unsupported method : "
                                + method
                );

        }

    }





    private void captureToken(
            Response response){


        if(response == null){

            return;

        }



        String[] paths = {


                "token",
                "accessToken",
                "access_token",
                "jwt",
                "jwtToken",

                "data.token",
                "data.accessToken",
                "data.access_token"


        };



        for(String path : paths){


            try{


                String token =
                        response.jsonPath()
                                .getString(path);



                if(token != null &&
                        !token.isBlank()){


                    env.put(
                            "token",
                            token
                    );


                    System.out.println(
                            "TOKEN CAPTURED"
                    );


                    break;

                }


            }
            catch(Exception ignored){}


        }


    }





    public void setVariable(
            String key,
            String value){


        env.put(
                key,
                value
        );

    }





    public String getVariable(
            String key){


        return env.get(key);

    }





    public void printEnvironment(){


        System.out.println(
                "========== POSTMAN ENV =========="
        );


        env.forEach(
                (k,v)-> {


                    if(k.equals("token")){

                        System.out.println(
                                k+" = ********"
                        );

                    }
                    else{

                        System.out.println(
                                k+" = "+v
                        );

                    }


                }
        );


    }


}