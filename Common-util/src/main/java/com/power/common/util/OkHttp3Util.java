package com.power.common.util;

import okhttp3.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Http请求工具类
 *
 * @author yu 2018/06/19.
 */
public class OkHttp3Util {

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static final MediaType FORM_DATA = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");


    /**
     * Simple sync get request.
     *
     * @param url request url
     * @return response body
     */
    public static String syncGet(String url) {
        return doSyncGet(url, null, null);
    }

    /**
     * Simple async get request.
     *
     * @param url      request
     * @param callback call back
     */
    public static void asyncGet(String url, Callback callback) {
        doAsyncGet(url, null, null, callback);
    }

    /**
     * Synchronous get request with parameters
     *
     * @param baseUrl request base url
     * @param params  request params
     * @return response body
     */
    public static String syncGet(String baseUrl, Map<String, String> params) {
        return doSyncGet(baseUrl, params, null);
    }

    /**
     * Asynchronous get request with parameters
     *
     * @param baseUrl  request base url
     * @param params   request params
     * @param callback callback
     */
    public static void asyncGet(String baseUrl, Map<String, String> params, Callback callback) {
        doAsyncGet(baseUrl, params, null, callback);
    }

    /**
     * Synchronous get request with parameters and headers
     *
     * @param baseUrl request base url
     * @param params  request params
     * @param headers request headers
     * @return response body
     */
    public static String syncGet(String baseUrl, Map<String, String> params, Map<String, String> headers) {
        return doSyncGet(baseUrl, params, headers);
    }

    /**
     * Asynchronous get request with parameters and headers
     *
     * @param baseUrl  request base url
     * @param params   request params
     * @param headers  request headers
     * @param callback call back
     */
    public static void asyncGet(String baseUrl, Map<String, String> params, Map<String, String> headers,
                                Callback callback) {
        doAsyncGet(baseUrl, params, headers, callback);
    }

    /**
     * Synchronous post request with parameters
     *
     * @param url    request url
     * @param params request params
     * @return response body
     */
    public static String syncPost(String url, Map<String, String> params) {
        return doSyncPost(url, params, null);
    }

    /**
     * Asynchronous post request with parameters
     *
     * @param url      request url
     * @param params   request params
     * @param callback call back
     */
    public static void asyncPost(String url, Map<String, String> params, Callback callback) {
        doAsyncPost(url, params, null, callback);
    }

    /**
     * Synchronous post request with parameters and headers
     *
     * @param url     url
     * @param params  request params
     * @param headers request headers
     * @return response body
     */
    public static String syncPost(String url, Map<String, String> params, Map<String, String> headers) {
        return doSyncPost(url, params, headers);
    }


    /**
     * Asynchronous post request with parameters and headers
     *
     * @param url      request url
     * @param params   request params
     * @param headers  request headers
     * @param callback call back
     */
    public static void asyncPost(String url, Map<String, String> params, Map<String, String> headers, Callback callback) {
        doAsyncPost(url, params, headers, callback);
    }


    /**
     * Synchronous post json request
     *
     * @param url  request url
     * @param json json data
     * @return response body
     */
    public static String syncPostJson(String url, String json) {
        RequestBody body = RequestBody.create(JSON_TYPE, json);
        return doSyncPost(url, body, null);
    }

    /**
     * Asynchronous post json request
     *
     * @param url      request url
     * @param json     json data
     * @param callback call back
     */
    public static void asyncPostJson(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(JSON_TYPE, json);
        doAsyncPost(url, body, null, callback);
    }

    /**
     * Synchronous post json request with headers
     *
     * @param url        request url
     * @param json       json data
     * @param headersMap request headers
     * @return response body
     */
    public static String syncPostJson(String url, String json, Map<String, String> headersMap) {
        RequestBody body = RequestBody.create(JSON_TYPE, json);
        return doSyncPost(url, body, headersMap);
    }

    /**
     * Asynchronous post json request with headers
     *
     * @param url        request url
     * @param json       json data
     * @param headersMap request headers
     * @param callback   call back
     */
    public static void asyncPostJson(String url, String json, Map<String, String> headersMap, Callback callback) {
        RequestBody body = RequestBody.create(JSON_TYPE, json);
        doAsyncPost(url, body, headersMap, callback);
    }

    private static void doAsyncGet(String baseUrl, Map<String, String> params, Map<String, String> headersMap,
                                   Callback callback) {
        OkHttpClient client = OkHttp3Util.getInstance();
        String url = urlJoin(baseUrl, params);
        Request request;
        if (null == headersMap || headersMap.size() == 0) {
            request = new Request.Builder().url(url).build();
        } else {
            Headers headers = setHeaders(headersMap);
            request = new Request.Builder().url(url).headers(headers).build();
        }
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    private static String doSyncGet(String baseUrl, Map<String, String> params, Map<String, String> headersMap) {
        OkHttpClient client = OkHttp3Util.getInstance();
        String url = urlJoin(baseUrl, params);
        Request request;
        if (null == headersMap || headersMap.size() == 0) {
            request = new Request.Builder().url(url).build();
        } else {
            Headers headers = setHeaders(headersMap);
            request = new Request.Builder().url(url).headers(headers).build();
        }
        Call call = client.newCall(request);
        try {
            Response response = call.execute();

            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void doAsyncPost(String url, Map<String, String> params, Map<String, String> headersMap, Callback callback) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        FormBody body = builder.build();
        doAsyncPost(url, body, headersMap, callback);
    }

    private static String doSyncPost(String url, Map<String, String> params, Map<String, String> headersMap) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        FormBody body = builder.build();
        return doSyncPost(url, body, headersMap);
    }

    private static void doAsyncPost(String url, RequestBody body, Map<String, String> headersMap, Callback callback) {
        OkHttpClient client = OkHttp3Util.getInstance();
        Request request;
        if (null == headersMap || headersMap.size() == 0) {
            request = new Request.Builder().post(body).url(url).build();
        } else {
            Headers headers = setHeaders(headersMap);
            request = new Request.Builder().post(body).url(url).headers(headers).build();
        }
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    private static String doSyncPost(String url, RequestBody body, Map<String, String> headersMap) {
        OkHttpClient client = OkHttp3Util.getInstance();
        Request request;
        if (null == headersMap || headersMap.size() == 0) {
            request = new Request.Builder().post(body).url(url).build();
        } else {
            Headers headers = setHeaders(headersMap);
            request = new Request.Builder().post(body).url(url).headers(headers).build();
        }
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Headers setHeaders(Map<String, String> headersParams) {
        Headers headers;
        okhttp3.Headers.Builder headersBuilder = new okhttp3.Headers.Builder();
        if (headersParams != null) {
            Iterator<String> iterator = headersParams.keySet().iterator();
            String key;
            while (iterator.hasNext()) {
                key = iterator.next().toString();
                headersBuilder.add(key, headersParams.get(key));
            }
        }
        headers = headersBuilder.build();
        return headers;
    }

    private static String urlJoin(String url, Map<String, String> params) {
        StringBuilder endUrl = new StringBuilder(url);
        if (null == params) {
            return url;
        }
        boolean isFirst = true;
        Set<Map.Entry<String, String>> entrySet = params.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            if (isFirst && !url.contains("?")) {
                isFirst = false;
                endUrl.append("?");
            } else {
                endUrl.append("&");
            }
            endUrl.append(entry.getKey());
            endUrl.append("=");
            endUrl.append(entry.getValue());
        }
        return endUrl.toString();
    }

    public static OkHttpClient getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final OkHttpClient INSTANCE = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
    }
}
