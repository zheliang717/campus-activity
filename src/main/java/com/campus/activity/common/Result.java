package com.campus.activity.common;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果
 */
@Data
public class Result {

    private Integer code;    // 状态码: 200-成功 400-参数错误 401-未登录 403-无权限 500-服务器错误
    private String message;  // 提示信息
    private Object data;     // 返回数据

    private Result() {}

    public static Result ok() {
        Result r = new Result();
        r.code = 200;
        r.message = "操作成功";
        return r;
    }

    public static Result ok(Object data) {
        Result r = ok();
        r.data = data;
        return r;
    }

    public static Result ok(String message, Object data) {
        Result r = ok(data);
        r.message = message;
        return r;
    }

    public static Result error(String message) {
        Result r = new Result();
        r.code = 500;
        r.message = message;
        return r;
    }

    public static Result error(Integer code, String message) {
        Result r = new Result();
        r.code = code;
        r.message = message;
        return r;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
