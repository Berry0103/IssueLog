package com.yuanzhang.econexus.config;

import com.yuanzhang.econexus.model.MesIssue;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class IssuesKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        String methodName = method.getName();
        StringBuilder keyBuilder = new StringBuilder();

        // 1. 处理分页查询（findIssuesByPage）
        if ("findIssuesByPage".equals(methodName)) {
            Map<String, Object> paramsMap = (Map<String, Object>) params[0];
            Pageable pageable = (Pageable) params[1];
            // Key：分页+排序+查询参数
            keyBuilder.append("issuePage:")
                    .append("page=").append(pageable.getPageNumber())
                    .append(";size=").append(pageable.getPageSize())
                    .append(";sort=").append(pageable.getSort())
                    .append(";params=").append(new TreeMap<>(paramsMap));
            return keyBuilder.toString();
        }

        // 2. 处理convertToDTO/convertToDTOWithUpdates（保持原有逻辑）
        if ("convertToDTO".equals(methodName) || "convertToDTOWithUpdates".equals(methodName)) {
            if (params.length > 0 && params[0] instanceof MesIssue) {
                MesIssue issue = (MesIssue) params[0];
                keyBuilder.append("issueDTO:").append(methodName).append(":").append(issue.getIssueIndex());
                return keyBuilder.toString();
            }
        }

        // 3. 兜底逻辑（更新记录的Key已硬编码为'issueUpdates:' + issueIndex）
        List<String> paramStrs = new ArrayList<>();
        for (Object param : params) {
            paramStrs.add(param != null ? param.toString() : "null");
        }
        return "issueCache:default:" + methodName + ":" + String.join("_", paramStrs);
    }
}
