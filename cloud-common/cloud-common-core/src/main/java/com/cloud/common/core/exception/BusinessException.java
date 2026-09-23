package com.cloud.common.core.exception;

import com.cloud.common.core.enums.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 携带结果码的业务运行时异常。
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAILURE.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

}
