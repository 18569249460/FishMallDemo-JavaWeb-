package com.fishshop.common;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Iterator;

/**
 * 全局异常处理器。
 *
 * <p>集中捕获 Controller 层抛出的异常，统一转换为 ApiResponse，避免接口返回 HTML 错误页。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务主动抛出的异常，例如未登录、权限不足、库存不足等。
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理 @RequestBody 参数校验失败。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError == null ? ResultCode.BAD_REQUEST.getMessage() : fieldError.getDefaultMessage();
        return ApiResponse.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理表单或查询参数绑定校验失败。
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError == null ? ResultCode.BAD_REQUEST.getMessage() : fieldError.getDefaultMessage();
        return ApiResponse.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理单个参数上的校验失败。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException ex) {
        Iterator<ConstraintViolation<?>> iterator = ex.getConstraintViolations().iterator();
        String message = iterator.hasNext() ? iterator.next().getMessage() : ResultCode.BAD_REQUEST.getMessage();
        return ApiResponse.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 兜底异常处理，避免未捕获异常直接暴露容器默认错误页或英文异常堆栈。
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        return ApiResponse.fail(ResultCode.SERVER_ERROR.getCode(), ResultCode.SERVER_ERROR.getMessage());
    }
}
