package com.fuchentao.seckill.exception;

/*
定义一个全局异常


 */

import com.fuchentao.seckill.result.CodeMsg;

public class GlobalException extends RuntimeException {

    public static final long serialVersionUID = 1L;
    private CodeMsg codeMsg;

    public GlobalException(CodeMsg codeMsg) {
        super(codeMsg.toString());
        this.codeMsg = codeMsg;
    }

    public CodeMsg getCodeMsg() {
        return codeMsg;
    }

    public void setCodeMsg(CodeMsg codeMsg) {
        this.codeMsg = codeMsg;
    }
}
