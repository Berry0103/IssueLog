package com.yuanzhang.econexus.controller;

import com.yuanzhang.econexus.util.BaiduTranslateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/translate")
@CrossOrigin(origins = "*")  // 允许跨域（生产环境建议指定具体域名）
public class TranslateController {

    @Autowired
    private BaiduTranslateUtil baiduTranslateUtil;

    /**
     * 批量翻译接口
     * @param requestMap 包含待翻译文本列表和目标语言
     *                   格式：{ "texts": ["文本1", "文本2"], "to": "en" }
     * @return 翻译结果映射 { "文本1": "翻译结果1", "文本2": "翻译结果2" }
     */
    @PostMapping("/batch")
    public Mono<Map<String, String>> batchTranslate(@RequestBody Map<String, Object> requestMap) {
        // 解析请求参数
        String toLang = (String) requestMap.get("to");
        Object textsObj = requestMap.get("texts");

        // 校验参数
        if (textsObj == null || !(textsObj instanceof java.util.List) || toLang == null) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "参数错误");
            return Mono.just(errorMap);
        }

        // 批量翻译
        java.util.List<String> texts = (java.util.List<String>) textsObj;
        Map<String, String> resultMap = new HashMap<>();
        for (String text : texts) {
            if (text == null || text.trim().isEmpty()) {
                resultMap.put(text, text);
                continue;
            }
            String translated = baiduTranslateUtil.translate(text, "auto", toLang);
            resultMap.put(text, translated);
        }

        return Mono.just(resultMap);
    }
}