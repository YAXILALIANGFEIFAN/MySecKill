package com.fuchentao.seckill.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtil {

    //3456789  1[3-9][0-9]{9}  1\d{10}  检查手机号还是下面这个正则表达式好用
    public static final Pattern mobilePattern = Pattern.compile("1[3-9][0-9]{9}");

    /**
     * 验证器，验证输入的字符串是不是手机号
     * @param str
     * @return
     */
    public static boolean isMobile(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        Matcher matcherOne = mobilePattern.matcher(str);
        return matcherOne.matches();
    }

}
