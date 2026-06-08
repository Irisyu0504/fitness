package org.example.fitness_backend.common;

import lombok.Data;

/**
 * 统一 API 响应包装类。
 * <p>所有 Controller 接口均返回此类型，保证前端接收到一致的数据结构。</p>
 * <p>成功时 code=200，失败时 code 为对应的 HTTP 状态码或业务错误码。</p>
 *
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}
