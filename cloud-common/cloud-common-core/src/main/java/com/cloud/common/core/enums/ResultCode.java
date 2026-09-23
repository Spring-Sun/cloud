package com.cloud.common.core.enums;

import lombok.Getter;

/**
 * 业务结果码。
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILURE(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "登录状态已过期，请重新登录"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
