package com.xuecheng.test;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.test.client.CmsClient;
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
public class TestFeign {

    @Autowired
    CmsClient cmsClient;

    @Test
    public void testFeign(){
        Map map = cmsClient.findByIdddd("5a754adf6abb500ad05688d9");
        System.out.println(map);
    }
}
