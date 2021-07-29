package com.fuchentao.seckill.util;


import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    //对明文字符串执行MD5加密，注意DigestUtils导包不要搞错了
    public static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }

    private static final String salt = "1a2b3c4d";

    //password转换成pass_cli
    public static String inputPasswordToFormPassword(String inputPassword) {
        String str = "" + salt.charAt(0) + salt.charAt(2) +
                     inputPassword +
                     salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    //pass_cli转换成pass_server
    public static String formPasswordToDBPassword(String formPassword, String saltDB) {
        String str = "" + saltDB.charAt(0) + saltDB.charAt(2) +
                     formPassword +
                     saltDB.charAt(5) + saltDB.charAt(4);
        return md5(str);
    }

    //password转换成pass_server  db放在开头的时候小写，放在中间的时候全大写
    public static String inputPasswordToDBPassword(String inputPassword, String saltDB) {
        String formPassword = inputPasswordToFormPassword(inputPassword);
        String dbPassword = formPasswordToDBPassword(formPassword, saltDB);
        return dbPassword;
    }

//    public static void main(String[] args) {
////        d3b1294a61a07da9b49b6e22b2cbd7f9
//        String formPassword = inputPasswordToFormPassword("123456");
//
////        b7797cce01b4b131b433b6acf4add449
//        String dbPassword = formPasswordToDBPassword(formPassword, "1a2b3c4d");
//
//        System.out.println(formPassword);
//        System.out.println(dbPassword);
//        System.out.println(
//                inputPasswordToDBPassword("123456", "1a2b3c4d")
//        );
//    }

}
