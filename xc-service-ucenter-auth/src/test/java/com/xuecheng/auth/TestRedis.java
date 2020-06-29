package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {
/**
{
    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6IlhjV2ViQXBwIiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOm51bGwsInV0eXBlIjpudWxsLCJpZCI6bnVsbCwiZXhwIjoxNTkzMzE0ODc3LCJqdGkiOiJjMThjZTUzZC1kODI1LTQzMWYtODRjZi1lODAzMGI4ZGE1ZjciLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.fxwI3DYnE3Y1lOz0rQxfgP7jNDEzuuE4t8RJn2fw2hxuWDxjH_02P_cH9bSPjweq33JBoNyI3TWcKQU1yYbVGBrjEpKwGYQLYFbr6-5Eac-f65dR7Dh04B__rfHc3jvNPRifddJqb5gJmxxstvnzs5p6ptP59cxrVUbqlDgvkHTG7hKO2GSsC7LLED_pXOjCTK7WgyOdaQ91HeM9M7hY3wmJeyL2KG3y2X25KhicJm-tJxCmD_5JfC7UeNCuGY7SI81YB1Duy00QBi5ckMTRWBuSkAqvsir9mKysOIK6qFBITkzcNZn2CxunHuQG-86SGV0m0PRoROaL0LZ2uGo4uw",
    "token_type": "bearer",
    "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6IlhjV2ViQXBwIiwic2NvcGUiOlsiYXBwIl0sImF0aSI6ImMxOGNlNTNkLWQ4MjUtNDMxZi04NGNmLWU4MDMwYjhkYTVmNyIsIm5hbWUiOm51bGwsInV0eXBlIjpudWxsLCJpZCI6bnVsbCwiZXhwIjoxNTkzMzE0ODc3LCJqdGkiOiI5NDkzNGFmNS0zYjVjLTQ0YTQtOThmZC1iNTBjYzU3NDMyMmYiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.WalKZOWCBaxdGcFRojcpH9pAcJX04lSLS316jmxVbz-F3U43mg2aB2WSMGrotkm_w1DVnrF8w2JYB9sMsvoysSVfyQgqP69VdMqAOcNc02cVrfIVNnv5iUVFruGwX0dJs7HYlTdamuj7DNfslIM8CVjEGiYEmq_4CtW_SNBRIRvW2IEJnCg4gh6D54UZw5oMqe-FcYoO_BSS9OHQIxgQVepQolj-jsJRycHQMKf6WknpSKSruaXR7J__uzN-AR5u536xu6h5Z-1S58NUChSWB80z_92AxPFLIGWegg3vI-aYud2bYlXujycgRq6p_4Ldu-G-5hkhXI6GaJRGk9yWnA",
    "expires_in": 43199,
    "scope": "app",
    "jti": "c18ce53d-d825-431f-84cf-e8030b8da5f7"
}
*/

    // StringRedisTemplate和RedisTemplate的分別:
    // https://zhuanlan.zhihu.com/p/92134214
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis(){
        String key="user_token:c18ce53d-d825-431f-84cf-e8030b8da5f7";
        Map map=new HashMap<>();
        map.put("access_token","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6IlhjV2ViQXBwIiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOm51bGwsInV0eXBlIjpudWxsLCJpZCI6bnVsbCwiZXhwIjoxNTkzMzE0ODc3LCJqdGkiOiJjMThjZTUzZC1kODI1LTQzMWYtODRjZi1lODAzMGI4ZGE1ZjciLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.fxwI3DYnE3Y1lOz0rQxfgP7jNDEzuuE4t8RJn2fw2hxuWDxjH_02P_cH9bSPjweq33JBoNyI3TWcKQU1yYbVGBrjEpKwGYQLYFbr6-5Eac-f65dR7Dh04B__rfHc3jvNPRifddJqb5gJmxxstvnzs5p6ptP59cxrVUbqlDgvkHTG7hKO2GSsC7LLED_pXOjCTK7WgyOdaQ91HeM9M7hY3wmJeyL2KG3y2X25KhicJm-tJxCmD_5JfC7UeNCuGY7SI81YB1Duy00QBi5ckMTRWBuSkAqvsir9mKysOIK6qFBITkzcNZn2CxunHuQG-86SGV0m0PRoROaL0LZ2uGo4uw");
        map.put("refresh_token","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6IlhjV2ViQXBwIiwic2NvcGUiOlsiYXBwIl0sImF0aSI6ImMxOGNlNTNkLWQ4MjUtNDMxZi04NGNmLWU4MDMwYjhkYTVmNyIsIm5hbWUiOm51bGwsInV0eXBlIjpudWxsLCJpZCI6bnVsbCwiZXhwIjoxNTkzMzE0ODc3LCJqdGkiOiI5NDkzNGFmNS0zYjVjLTQ0YTQtOThmZC1iNTBjYzU3NDMyMmYiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.WalKZOWCBaxdGcFRojcpH9pAcJX04lSLS316jmxVbz-F3U43mg2aB2WSMGrotkm_w1DVnrF8w2JYB9sMsvoysSVfyQgqP69VdMqAOcNc02cVrfIVNnv5iUVFruGwX0dJs7HYlTdamuj7DNfslIM8CVjEGiYEmq_4CtW_SNBRIRvW2IEJnCg4gh6D54UZw5oMqe-FcYoO_BSS9OHQIxgQVepQolj-jsJRycHQMKf6WknpSKSruaXR7J__uzN-AR5u536xu6h5Z-1S58NUChSWB80z_92AxPFLIGWegg3vI-aYud2bYlXujycgRq6p_4Ldu-G-5hkhXI6GaJRGk9yWnA");
        String jsonString = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set(key,jsonString,10, TimeUnit.MINUTES);
        //stringRedisTemplate.boundValueOps(key).set(jsonString,10, TimeUnit.MINUTES);
    }

    @Test
    public void testVerify(){
        String key="user_token:c18ce53d-d825-431f-84cf-e8030b8da5f7";
        String s = stringRedisTemplate.opsForValue().get(key);
        Long expire = stringRedisTemplate.getExpire(key);
        System.out.println(s);
        System.out.println(expire);
    }
}
