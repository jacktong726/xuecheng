package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.cms.CmsConfig;
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
public class TestRestTemplate {

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testRestTemplate(){
//        ResponseEntity<CmsConfig> forEntity =
//                restTemplate.getForEntity("http://localhost:31001/cms/config/5a791725dd573c3574ee333f",
//                        CmsConfig.class);
//        System.out.println(forEntity);
    }
}
