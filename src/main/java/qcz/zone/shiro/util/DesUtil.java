package qcz.zone.shiro.util;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 27
 */
public class DesUtil {
    /**
     * DES算法密钥
     */
    private static final byte[] DES_KEY = { 21, 1, -110, 82, -32, -85, -128, -65 };

    public static final String HASH_ALGORITHM_NAME_MD5 = "MD5";    // 哈希加密算法名：MD5、SHA-1、SHA-256
    public static final int HASH_ITERATIONS_128 = 128;         // 哈希迭代次数
    public static final int HASH_ITERATIONS_256 = 256;         // 哈希迭代次数
    public static final int HASH_ITERATIONS_512 = 512;         // 哈希迭代次数
    public static final int HASH_ITERATIONS_1024 = 1024;         // 哈希迭代次数

    /**
     * 数据加密，算法（DES）
     *
     * @param data
     *            要进行加密的数据
     * @return 加密后的数据
     */
    @SuppressWarnings("restriction")
    public static String encryptBasedDes(String data) {
        String encryptedData = null;

        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            DESKeySpec deskey = new DESKeySpec(DES_KEY);
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(deskey);
            // 加密对象
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key, sr);
            // 加密，并把字节数组编码成字符串
            encryptedData = new sun.misc.BASE64Encoder().encode(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            // log.error("加密错误，错误信息：", e);
            throw new RuntimeException("加密错误，错误信息：", e);
        }

        return encryptedData;
    }

    /**
     * 数据解密，算法（DES）
     *
     * @param cryptData
     *            加密数据
     * @return 解密后的数据
     */
    @SuppressWarnings("restriction")
    public static String decryptBasedDes(String cryptData) {
        String decryptedData = null;

        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            DESKeySpec deskey = new DESKeySpec(DES_KEY);
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(deskey);
            // 解密对象
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key, sr);
            // 把字符串解码为字节数组，并解密
            decryptedData = new String(cipher.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(cryptData)));
        } catch (Exception e) {
            // log.error("解密错误，错误信息：", e);
            throw new RuntimeException("解密错误，错误信息：", e);
        }

        return decryptedData;
    }

    public static String hashMd5(String str) {
        return hash(HASH_ALGORITHM_NAME_MD5, str);
    }

    public static String hash(String algorithmName, String str) {
        return new SimpleHash(algorithmName, str).toHex();
    }

    public static String hashSaltMd5(String str, Object salt) {
        return hashSalt(HASH_ALGORITHM_NAME_MD5, str, salt, HASH_ITERATIONS_1024);
    }

    public static String hashSalt(String algorithmName, String str, Object salt, int iterations) {
        return new SimpleHash(algorithmName, str, ByteSource.Util.bytes(String.valueOf(salt)), iterations).toHex();
    }
}
