package com.leyou.auth.test;

import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "F:\\key\\rsa.pub";

    private static final String priKeyPath = "F:\\key\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    /**
     * 生成公钥和私钥
     */
    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU3MTQ4NTE4NX0.Vc9PibQXRgwMVpP8NXXu34_37kyYXE9hJ0wi75dSSJM0O3i2GyW84HxbKWwu93FF11xkXewLmpwj3LsncMwdABX3v0jpq-MG-zMZQfkAqkHqVcpdY-_-cIG5kNbcc89waUodK2YM8KxWZYQRKSNm1Z9pW0x-TFv-DlNInWQt2Jc";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}

