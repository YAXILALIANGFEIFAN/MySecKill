package com.fuchentao.seckill.result;


public class CodeMsg {

    private int code;
    private String msg;

    //定义通用的异常
    public static CodeMsg success = new CodeMsg(0, "success");
    public static CodeMsg serverError = new CodeMsg(500100, "服务端异常");
    public static CodeMsg bindError =
            new CodeMsg(500101, "参数校验异常: %s");
    public static CodeMsg requestIllegal =
            new CodeMsg(500102, "非法请求");
    public static CodeMsg accessLimit =
            new CodeMsg(500103, "请求过于频繁");

    //登录模块的异常
    public static CodeMsg sessionError =
            new CodeMsg(500210, "session不存在或者已失效");
    public static CodeMsg passwordEmpty =
            new CodeMsg(500211, "登录密码不能为空");
    public static CodeMsg mobileEmpty =
            new CodeMsg(500212, "手机号不能为空");
    public static CodeMsg mobileError =
            new CodeMsg(500213, "手机号格式错误");
    public static CodeMsg mobileNotExit =
            new CodeMsg(500214, "手机号不存在");
    public static CodeMsg passwordError =
            new CodeMsg(500215, "密码错误");


    //订单模块异常
    public static CodeMsg orderNotExist =
            new CodeMsg(500400, "订单不存在");


    //秒杀模块的异常
    public static CodeMsg seckillOver =
            new CodeMsg(500500, "商品全部秒杀完毕");
    public static CodeMsg seckillRepeat =
            new CodeMsg(500501, "不能重复秒杀");



    private CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;

    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public CodeMsg fillArgs(Object...args) {
        int code = this.code;
        String msg = String.format(this.msg, args);
        return new CodeMsg(code, msg);
    }

    @Override
    public String toString() {
        return "CodeMsg{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
