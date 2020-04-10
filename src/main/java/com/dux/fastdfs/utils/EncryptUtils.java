package com.dux.fastdfs.utils;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Md5Hash;

/**
 * description 加密算法类
 *
 * @author chen wei
 * @version 1.0
 *          <p>
 *          Copyright: Copyright (c) 2019
 *          </p>
 * @date 2019/7/31 11:02
 */
public class EncryptUtils {

    /**
     * description 生成盐值
     *
     * @return java.lang.String
     * @author chen wei
     * @date 2019/7/11
     */
    public static String createSalt() {
        SecureRandomNumberGenerator random = new SecureRandomNumberGenerator();
        String salt = random.nextBytes().toHex();
        return salt;
    }

    /**
     * HASH散列次数默认值
     */
    public static final int HASH_ITERATIONS_DEFAULT = 2;

    /**
     * description 加盐获取hash散列值
     *
     * @param password
     * @param salt
     * @return java.lang.String
     * @author chen wei
     * @date 2019/7/11
     */
    public static String md5Salt(String password, String salt) {
        Md5Hash hash;
        hash = new Md5Hash(password, salt, HASH_ITERATIONS_DEFAULT);

        return hash.toHex();
    }

    /**
     * description 获取hash散列值
     *
     * @param password
     * @return java.lang.String
     * @author chen wei
     * @date 2019/7/11
     */
    public static String md5(String password) {
        Md5Hash hash;
        hash = new Md5Hash(password, null, HASH_ITERATIONS_DEFAULT);
        return hash.toHex();
    }

    public static void main(String[] args) {
        String salt = createSalt();
        System.out.println(salt);
        String s = md5Salt("qqwwee", salt);
        System.out.println(s);
    }
}
