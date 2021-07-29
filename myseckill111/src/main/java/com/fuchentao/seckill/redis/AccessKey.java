package com.fuchentao.seckill.redis;


public class AccessKey extends BasePrefix{

    //设置AccessKey的有效期，单位秒
    private static final int accessKeyExpireSeconds = 60 * 5;

    private AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static AccessKey getAccessKey =
            new AccessKey(accessKeyExpireSeconds, "access");

    public static AccessKey getCustomAccessKey(int expireSeconds) {
        return new AccessKey(expireSeconds, "access");
    }

}
