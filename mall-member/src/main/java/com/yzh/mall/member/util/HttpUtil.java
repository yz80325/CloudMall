package com.yzh.mall.member.util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HttpUtil {

    static CloseableHttpClient httpClient = HttpClients.createDefault();


    // HTTP GET请求
    public static HttpResponse sendGet(String url, HashMap<String,String>querys) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        List<NameValuePair> list = new LinkedList<>();
        for (Map.Entry<String, String> stringStringEntry : querys.entrySet()) {
            list.add(new BasicNameValuePair(stringStringEntry.getKey(),stringStringEntry.getValue()));
        }
        uriBuilder.setParameters(list);

        HttpGet httpGet = new HttpGet(uriBuilder.build());

        return httpClient.execute(httpGet);
    }

}
