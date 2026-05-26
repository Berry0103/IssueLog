package com.yuanzhang.econexus.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局统一响应体
 */
@Data
@AllArgsConstructor // 生成包含所有字段的构造方法（code, message, data, traceId）
@NoArgsConstructor  // 生成无参构造（必须加，否则会覆盖默认无参构造）
public class Result<T> {
    /** 响应码：200成功，400参数错误，401未登录，403权限不足，500系统异常 */
    private int code;
    /** 响应消息（用户友好提示） */
    private String message;
    /** 响应数据（成功时返回数据，异常时为null） */
    private T data;
    /** 异常追踪ID（生产环境用于定位日志，可选） */
    private String traceId;

    // ========== 静态工厂方法（简化创建） ==========
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, null);
    }

    public static <T> Result<T> fail(int code, String message, String traceId) {
        return new Result<>(code, message, null, traceId);
    }

    // 快捷方法：参数错误
    public static <T> Result<T> badRequest(String message) {
        return fail(400, message, null);
    }

    // 快捷方法：未登录
    public static <T> Result<T> unauthorized(String message) {
        return fail(401, message, null);
    }

    // 快捷方法：权限不足
    public static <T> Result<T> forbidden(String message) {
        return fail(403, message, null);
    }

    // 快捷方法：系统异常
    public static <T> Result<T> serverError(String message, String traceId) {
        return fail(500, message, traceId);
    }
}