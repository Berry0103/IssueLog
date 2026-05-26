package com.yuanzhang.econexus.util;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClientUtil {

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    // GET方法（原有逻辑，无需修改）
    public static String sendGet(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败，状态码：" + response.code());
            }
            return response.body() != null ? response.body().string() : "";
        }
    }

    // 新增POST方法（支持JSON请求体，传递mobile等必填参数）
    public static String sendPost(String url, String jsonBody) throws IOException {
        // 定义JSON媒体类型
        MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
        // 构建POST请求体
        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, jsonBody);
        // 构建POST请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        // 执行请求并返回结果
        try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("POST请求失败，状态码：" + response.code());
            }
            return response.body() != null ? response.body().string() : "";
        }
    }
}