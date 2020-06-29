package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;   //redis中token的存活時間, 這裏是1200秒/20分鐘

    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //遠端請求springSecurity返回token, 構建AuthToken
        AuthToken authToken = this.getToken(username, password, clientId, clientSecret);
        //把AuthToken存到redis中
        boolean b = this.saveToken(authToken);
        if (!b){
            throw new CustomException(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        //返回AuthToken
        return authToken;
    }

    private AuthToken getToken(String username, String password, String clientId, String clientSecret){
        //取得要訪問的uri
        ServiceInstance instance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = instance.getUri();
        String uriString = uri.toString() + "/auth/oauth/token";
        //構建restTemplate需要的requestEntity
        MultiValueMap<String, String> body=new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);
        MultiValueMap<String, String> headers=new LinkedMultiValueMap<>();
        headers.add("Authorization",getBase64String(clientId,clientSecret));
        HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity = new HttpEntity<>(body, headers);
        //restTemplate發出token請求(先設定放行400和401錯誤)
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException{
                if (response.getRawStatusCode()!=401 && response.getRawStatusCode()!=400){
                    super.handleError(response);
                }
            }
        });
        ResponseEntity<Map> exchange = restTemplate.exchange(uriString, HttpMethod.POST, multiValueMapHttpEntity, Map.class);
        Map map = exchange.getBody();
        if (map==null||
                map.get("access_token") == null ||
                map.get("refresh_token") == null ||
                map.get("jti") == null){
            throw new CustomException(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String)map.get("jti"));
        authToken.setRefresh_token((String)map.get("refresh_token"));
        authToken.setJwt_token((String)map.get("access_token"));
        return authToken;
    }

    private String getBase64String(String clientId,String clientSecret){
        String s = clientId + ":" + clientSecret;
        byte[] bytes = Base64Utils.encode(s.getBytes());
        return "Basic "+ new String(bytes);
    }


    private boolean saveToken(AuthToken authToken){
        String access_token = authToken.getAccess_token();
        String authTokenString = JSON.toJSONString(authToken);
        stringRedisTemplate.opsForValue().set("user_token:" +access_token,authTokenString,tokenValiditySeconds, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire("user_token:"+access_token);  //如果redis中找不到這個record, 就會返回負數
        return expire>0;
    }
}
