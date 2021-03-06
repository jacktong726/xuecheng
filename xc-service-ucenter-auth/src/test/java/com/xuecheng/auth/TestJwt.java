package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**测试jwt令牌的生成与验证*/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {
    //keytool -genkeypair -alias xckey -keyalg RSA -keypass xuecheng -keystore xc.keystore -storepass xuechengkeystore
    String key_location = "xc.keystore";
    String keystore_password="xuechengkeystore";
    String publicKey="-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
    String alias = "xckey";
    String keypassword = "xuecheng";

    @Test
    public void testCreateJwt(){
        //流程:取得密鑰證書文件->取得私鑰->利用私鑰和要加密的內容生成jwt token
        ClassPathResource classPathResource = new ClassPathResource(key_location);  //證書文件路徑
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource, keystore_password.toCharArray());
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypassword.toCharArray());
        RSAPrivateKey aPrivate =(RSAPrivateKey) keyPair.getPrivate();   //取得私鑰

        Map map=new HashMap();
        map.put("name","唐俊傑");
        String jsonString = JSON.toJSONString(map);

        Jwt jwt = JwtHelper.encode(jsonString, new RsaSigner(aPrivate));    //生成jwt
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
        //eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoi5ZSQ5L-K5YKRIn0.C3KgBv_f0xIZGgxNVei9T0N2aAZqtuJnCqWaZnlo0GHJX3tn-JewM_xMQ_ZhenygNW7jgfDnWKoUu5HZSZV89mG1l-iLAD-9O1JDmsycC9uw-JmwgAPjdUiseb0GUtCw6OrzWaPrV4E-slaWFnGHiYtjHoCqgK8XOy4s6-tsOkK29DBXBCHAoEpseWt60Dj4JM_DhfK3KacSKo-VgJn6BcXNa6C_8gn-2-dVYPtZiBGI7uCzPvPoeXYqsOasUA60QfWWH7dFS3TKiS0xPJYzMmxSjxwS7U688CSnRJU5jK9wMzEsaOnxPy1IgjTDCb52j7VEXL6X-o-5PGJ3nyArIQ
    }

    @Test
    public void testVerify(){
        String token="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJpdGNhc3QiLCJ1dHlwZSI6IjEwMTAwMiIsImlkIjoiNDkiLCJleHAiOjE1OTM5MjE1NzAsImF1dGhvcml0aWVzIjpbInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYmFzZSIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfZGVsIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9saXN0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wbGFuIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZSIsInhjX3RlYWNobWFuYWdlciIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfbWFya2V0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wdWJsaXNoIiwiY291cnNlX2dldF90ZWFjaHBsYW5MaXN0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9hZGQiXSwianRpIjoiZmEwN2ZlNGYtOGFmNS00MzUzLTllNGItMTg2ZDkxNjgxN2JjIiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.T8dgqn_LnBr5iGtqNVhB8wBQ8Z8I3nBcBcwU-Js9lzWpUPTgrBS9qZJKTnlRwHX0wzMz6H0VHBszPKUjfb-R1aS4gBEB8XMusKFZKoH5QR4PCfV2l7EIzKAtvNJYs6Pmp-kxj0HaKYf3QtJp0b67WhZWYIN3ToAC3DeZz8NKkZ1n9fu4OPrU-qA_gzqAiIXI5mnUUT6X_XI6kUbjoBIYIMeugoKEIrKgbZaXmmAhdqemKgr7qvO_RBqtRlFRlfFZW2YI-K-jWmT-okDQK6WftOYEcDLTAtYJs0OAmLElW4wykFZ5O6UX5zDphzc6zvJshZRPApCEqsEnGYe5WblV6Q";
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));
        String claims = jwt.getClaims();
        System.out.println(claims);
    }
}
