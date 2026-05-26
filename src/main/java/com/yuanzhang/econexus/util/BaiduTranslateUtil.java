package com.yuanzhang.econexus.util;

import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class BaiduTranslateUtil {

    @Value("${baidu.translate.appid}")
    private String appid;

    @Value("${baidu.translate.secretKey}")
    private String secretKey;

    @Value("${baidu.translate.apiUrl}")
    private String apiUrl;

    @Value("${baidu.translate.apiUrl.professional}")
    private String apiUrl_pro;

    // 翻译缓存：key=文本_源语言_目标语言，value=翻译结果，缓存1小时
    private final Cache<String, String> translateCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000) // 最大缓存1000条
            .build();

    /**
     * 调用百度翻译API（带缓存）
     * @param text 待翻译文本
     * @param from 源语言（auto=自动检测）
     * @param to 目标语言（zh=中文，en=英文）
     * @return 翻译结果
     */
    public String translate(String text, String from, String to) {
        // 构建缓存key
        String cacheKey = text + "_" + from + "_" + to;
        // 先从缓存获取
        String cachedResult = translateCache.getIfPresent(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 缓存未命中，调用百度API
        String salt = UUID.randomUUID().toString().replace("-", "");
        String sign = DigestUtils.md5Hex(appid + text + salt + secretKey);

        Map<String, String> params = new HashMap<>();
        params.put("q", text);
        params.put("from", from);
        params.put("to", to);
        params.put("appid", appid);
        params.put("salt", salt);
        params.put("sign", sign);
        params.put("action", "1");

        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        String url = urlBuilder.substring(0, urlBuilder.length() - 1);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String result = text; // 默认返回原文本
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResult = response.body().string();
                JSONObject jsonObject = JSONObject.parseObject(jsonResult);
                if (jsonObject.containsKey("trans_result")) {
                    result = jsonObject.getJSONArray("trans_result").getJSONObject(0).getString("dst");
                    // 存入缓存
                    translateCache.put(cacheKey, result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 调用百度专业翻译API（带缓存）
     * @param text 待翻译文本
     * @param from 源语言（auto=自动检测）
     * @param to 目标语言（zh=中文，en=英文）
     * @return 翻译结果
     */
    public String translate_pro(String text, String from, String to, String domain) {
        // 构建缓存key
        String cacheKey = text + "_" + from + "_" + to + "_pro";
        // 先从缓存获取
        String cachedResult = translateCache.getIfPresent(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 缓存未命中，调用百度API
        String salt = UUID.randomUUID().toString().replace("-", "");
        String sign = DigestUtils.md5Hex(appid + text + salt + domain + secretKey);

        Map<String, String> params = new HashMap<>();
        params.put("q", text);
        params.put("from", from);
        params.put("to", to);
        params.put("appid", appid);
        params.put("salt", salt);
        params.put("sign", sign);
        params.put("domain", domain); // 指定专业领域

        StringBuilder urlBuilder = new StringBuilder(apiUrl_pro);
        urlBuilder.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        String url = urlBuilder.substring(0, urlBuilder.length() - 1);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String result = text; // 默认返回原文本
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResult = response.body().string();
                JSONObject jsonObject = JSONObject.parseObject(jsonResult);
                if (jsonObject.containsKey("trans_result")) {
                    result = jsonObject.getJSONArray("trans_result").getJSONObject(0).getString("dst");
                    // 存入缓存
                    translateCache.put(cacheKey, result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}