package com.fishshop.common;

import java.time.LocalDateTime;

/**
 * 统一接口响应结构。
 *
 * <p>前端只需要按 code、message、data 判断接口结果，避免每个 Controller 自己拼不同格式。</p>
 */
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 成功响应，data 可为任意业务数据。
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应，同时允许业务自定义提示文案。
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 根据统一结果码生成失败响应。
     */
    public static ApiResponse<Void> fail(ResultCode resultCode) {
        return new ApiResponse<Void>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 根据自定义错误码和提示生成失败响应。
     */
    public static ApiResponse<Void> fail(int code, String message) {
        return new ApiResponse<Void>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
