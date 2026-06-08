package org.example.fitness_backend.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * <p>统一捕获 Controller 层抛出的异常，将其转换为标准的 {@link Result} 响应格式，
 * 避免将异常堆栈直接暴露给前端。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务运行时异常（如参数校验失败、Token 无效等），
     * 将异常消息直接返回给前端，便于定位问题。
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        return Result.error(500, e.getMessage());
    }

    /**
     * 处理未预期的系统异常，返回通用错误信息，
     * 避免将内部实现细节暴露给前端。
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(500, "服务器内部错误");
    }
}
