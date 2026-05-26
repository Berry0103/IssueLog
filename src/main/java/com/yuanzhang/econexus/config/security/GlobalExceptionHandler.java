package com.yuanzhang.econexus.config.security;

import com.yuanzhang.econexus.dto.request.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.UUID;

/**
 * 全局异常处理器
 * 捕获项目中所有未处理的异常，统一返回响应+记录日志
 */
@Slf4j
@RestControllerAdvice // 全局捕获@RestController的异常
public class GlobalExceptionHandler {

    /**
     * 1. 捕获自定义业务异常（如权限不足、参数错误等自定义异常）
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        // 业务异常记录WARN级别日志（非系统错误）
        log.warn("[业务异常] URL:{} | 错误信息:{} | 异常类型:{}",
                request.getRequestURI(), e.getMessage(), e.getClass().getSimpleName(), e);

        // 根据业务异常的错误码返回对应响应
        Result<Void> result = Result.fail(e.getCode(), e.getMessage(), null);
        return new ResponseEntity<>(result, HttpStatus.valueOf(e.getCode()));
    }

    /**
     * 2. 捕获参数校验异常（@Valid/@Validated 校验失败）
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Void>> handleValidException(Exception e, HttpServletRequest request) {
        // 拼接参数错误信息
        StringBuilder errorMsg = new StringBuilder();
        if (e instanceof MethodArgumentNotValidException) {
            ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors()
                    .forEach(fieldError -> errorMsg.append(fieldError.getField()).append(":").append(fieldError.getDefaultMessage()).append(";"));
        } else if (e instanceof BindException) {
            ((BindException) e).getBindingResult().getFieldErrors()
                    .forEach(fieldError -> errorMsg.append(fieldError.getField()).append(":").append(fieldError.getDefaultMessage()).append(";"));
        }
        String finalMsg = errorMsg.length() > 0 ? errorMsg.substring(0, errorMsg.length() - 1) : "参数校验失败";

        // 记录WARN日志
        log.warn("[参数校验异常] URL:{} | 错误信息:{}", request.getRequestURI(), finalMsg, e);

        // 返回400参数错误
        return ResponseEntity.badRequest().body(Result.badRequest(finalMsg));
    }

    // ========== 新增：未登录异常类 ==========
    public static class NotLoginException extends RuntimeException {
        public NotLoginException(String message) {
            super(message);
        }
        // 可选：固定401错误码，避免硬编码
        public int getCode() {
            return 401;
        }
    }

    // ========== 新增：捕获未登录异常 ==========
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<Void>> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.warn("[未登录异常 401] URL:{} | 错误信息:{}",
                request.getRequestURI(), e.getMessage(), e);
        // 返回标准化401响应
        return new ResponseEntity<>(
                Result.unauthorized(e.getMessage()),
                HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * 3. 捕获Spring Security权限异常（403）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("[权限不足异常] URL:{} | 错误信息:{}", request.getRequestURI(), e.getMessage(), e);
        return new ResponseEntity<>(Result.forbidden("权限不足，无法执行该操作"), HttpStatus.FORBIDDEN);
    }

    /**
     * 4. 捕获登录认证异常（如密码错误）
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Result<Void>> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.warn("[认证失败异常] URL:{} | 错误信息:{}", request.getRequestURI(), e.getMessage(), e);
        return new ResponseEntity<>(Result.unauthorized("账号或密码错误"), HttpStatus.UNAUTHORIZED);
    }

    /**
     * 5. 捕获404异常（接口不存在）
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handle404Exception(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("[404异常] URL:{} | 错误信息:{}", request.getRequestURI(), e.getMessage(), e);
        return new ResponseEntity<>(Result.fail(404, "请求的接口不存在", null), HttpStatus.NOT_FOUND);
    }

    /**
     * 6. 捕获所有未定义的系统异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGlobalException(Exception e, HttpServletRequest request) {
        // 生成唯一traceId，用于生产环境定位日志
        String traceId = UUID.randomUUID().toString().replace("-", "");

        // 系统异常记录ERROR级别日志（含traceId+堆栈）
        log.error("[系统异常] URL:{} | 追踪ID:{} | 错误信息:{} | 异常类型:{}",
                request.getRequestURI(), traceId, e.getMessage(), e.getClass().getSimpleName(), e);

        // 生产环境返回友好提示，隐藏具体异常信息
        String userMsg = "系统繁忙，请稍后重试（追踪ID：" + traceId + "）";
        return new ResponseEntity<>(Result.serverError(userMsg, traceId), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== 自定义业务异常类（需单独定义） ==========
    public static class BusinessException extends RuntimeException {
        private int code; // 自定义错误码

        public BusinessException(int code, String message) {
            super(message);
            this.code = code;
        }

        public BusinessException(String message) {
            this(500, message);
        }

        // getter/setter
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }
}