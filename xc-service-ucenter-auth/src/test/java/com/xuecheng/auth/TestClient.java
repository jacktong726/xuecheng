package com.xuecheng.auth;

import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {

    //用來訪問uri
    @Autowired
    RestTemplate restTemplate;

    //和feign差不多作用, 向eureka遠端調用服務
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Test
    public void testClient(){
        //取得獲取token的uri地址: http://localhost:40400/auth/oauth/token
        ServiceInstance instance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = instance.getUri();
        String uriString = uri.toString() + "/auth/oauth/token";
        //restTemplate請求時, 需要一個帶有header和body的httpEntity, 所以各創建一個對象
        //header和body都是一個MultiValueMap接口類, 實現類為LinkedMultiValueMap
        MultiValueMap<String,String> header=new LinkedMultiValueMap<>();
        String httpHeader = base64HttpHeader("XcWebApp", "XcWebApp");   //必須要帶有轉化為base64的base Auth, 才可以成功請求(服務器會核對oauth_client_details表中的client資料)
        header.add("Authorization",httpHeader);

        MultiValueMap<String,String> body=new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username","itcast");
        body.add("password","123");

        //把body和header放到httpEntity中
        HttpEntity<MultiValueMap<String, String>> httpEntity=new HttpEntity(body,header);
        //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });
        //使用restTemplate請求token
        ResponseEntity<Map> exchange = restTemplate.exchange(uriString, HttpMethod.POST, httpEntity, Map.class);
        Map<String,Object> exchangeBody = exchange.getBody();
        for (Map.Entry<String, Object> entry : exchangeBody.entrySet()) {
            System.out.println(entry.getKey()+":"+entry.getValue());
        }
    }

    private String base64HttpHeader(String clientId,String clientSecret){
        byte[] bytes = Base64Utils.encode((clientId + ":" + clientSecret).getBytes());
        return "Basic "+ new String(bytes);
    }
}
