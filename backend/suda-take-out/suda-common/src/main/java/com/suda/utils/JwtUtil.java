package com.suda.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * 将给定的密钥字符串拉伸/截断为 256 位 HS256 密钥。
     * JJWT 0.12.x 要求密钥 >= 256 bits，短密钥（如 "itcast"）会直接抛 WeakKeyException。
     */
    private static SecretKey deriveKey(String secretKey) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(secretKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, "HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 生成签名使用的密钥（SHA-256 拉伸确保 >= 256 bits）
        SecretKey key = deriveKey(secretKey);

        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 设置jwt的body
        return Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明
                .claims(claims)
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(key, Jwts.SIG.HS256)
                // 设置过期时间
                .expiration(exp)
                .compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 生成签名使用的密钥（SHA-256 拉伸确保 >= 256 bits）
        SecretKey key = deriveKey(secretKey);
        // 使用相同的密钥解析JWT并获取声明内容
        return Jwts.parser()
                // 设置签名的秘钥
                .verifyWith(key)
                .build()
                // 解析token
                .parseSignedClaims(token)
                .getPayload();
    }

}
