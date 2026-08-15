package com.chenpperr.xhs.common.sensitive;

/**
 * 脱敏工具类
 *
 * 手写打码逻辑
 */
public class SensitiveUtils {

    /**
     * 手机号脱敏：保留前三后四，中间打码
     * 13812346789 -> 138****6789
     */
    public static String maskMobile(String mobile)
    {
        if(mobile == null || mobile.length() != 11){
            return mobile;
        }
        return mobile.substring(0,3) + "****" +mobile.substring(7);
    }

    /**
     * 邮箱脱敏：保留首字母和@后面
     * test@qq.com -> t****@qq.com
     */
    public static String maskEmail(String email)
    {
        if(email == null || !email.contains("@")){
            return email;
        }
        int atIndex = email.indexOf("@");
        return email.substring(0,1)+"****" + email.substring(atIndex);
    }

    /**
     * 身份证脱敏：保留前三后四
     */
    public static String maskIdCard(String idCard)
    {
        if(idCard == null || idCard.length() != 18){
            return idCard;
        }
        return idCard.substring(0,3) + "***********" + idCard.substring(14);
    }

}
