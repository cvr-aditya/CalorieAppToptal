package com.toptal.calorie.util;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * Created by asirna on 03/07/2017.
 */
public class HttpUtil {

    private HttpClient client;

    public HttpUtil() {
        client = HttpClientBuilder.create().build();
    }

    public JSONObject get(String url, Map<String,String> headers) {
        HttpGet httpGet = new HttpGet(url);
        for (Map.Entry<String,String> header : headers.entrySet()) {
            httpGet.setHeader(header.getKey(), header.getValue());
        }
        try {
            HttpResponse response = client.execute(httpGet);
            String resp = EntityUtils.toString(response.getEntity());
            System.out.println(" get response is : " + resp);
            return new JSONObject(resp);
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONObject();
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }


    }

    public JSONObject post(String url, Map<String,String> headers, JSONObject body) {
        HttpPost httpPost = new HttpPost(url);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpPost.setHeader(header.getKey(), header.getValue());
            }
        }
        try {
            httpPost.setEntity(new StringEntity(body.toString()));
            HttpResponse response = client.execute(httpPost);
            String resp = EntityUtils.toString(response.getEntity());
            System.out.println("post response is : " + resp);
            return new JSONObject(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public JSONObject put(String url, Map<String,String> headers, JSONObject body) {
        HttpPut httpPut = new HttpPut(url);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpPut.setHeader(header.getKey(), header.getValue());
            }
        }
        try {
            httpPut.setEntity(new StringEntity(body.toString()));
            HttpResponse response = client.execute(httpPut);
            String resp = EntityUtils.toString(response.getEntity());
            System.out.println("put response is : " + resp);
            return new JSONObject(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public JSONObject delete(String url, Map<String,String> headers) {
        HttpDelete httpDelete = new HttpDelete(url);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpDelete.setHeader(header.getKey(), header.getValue());
            }
        }
        try {
            HttpResponse response = client.execute(httpDelete);
            String resp = EntityUtils.toString(response.getEntity());
            System.out.println("delete response is : " + resp);
            return new JSONObject(resp);
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
