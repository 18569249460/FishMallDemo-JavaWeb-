package com.fishshop.common;

/**
 * 统一结果码。
 *
 * <p>这里的 code 作为接口业务状态码使用，HTTP 状态码由 SpringMVC 响应层决定。</p>
 */
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    NOT_IMPLEMENTED(501, "功能尚未实现"),
    SERVER_ERROR(500, "服务器异常，请稍后重试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
