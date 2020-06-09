package com.xuecheng.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRobbin {

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testRobbin() {
        //服务id
        String serviceId = "XC-SERVICE-MANAGE-CMS";
        //通过服务id调用
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://"+serviceId+"/cms/page/5a754adf6abb500ad05688d9", Map.class);
        Map body = forEntity.getBody();
        System.out.println(body);
    }
}
