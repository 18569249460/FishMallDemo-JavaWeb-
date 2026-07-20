package com.fishshop.common;

/**
 * 业务异常。
 *
 * <p>Service 层遇到库存不足、无权限、参数不合法等可预期问题时抛出，
 * 由 GlobalExceptionHandler 转成统一 JSON 响应。</p>
 */
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
