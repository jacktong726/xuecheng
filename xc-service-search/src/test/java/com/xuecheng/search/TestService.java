package com.xuecheng.search;

import com.xuecheng.search.service.EsCourseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestService {
    @Autowired
    EsCourseService esCourseService;

    @Test
    public void testGetAll(){
        System.out.println(esCourseService.getall("402880ee7288abbb017288ac68970000"));
    }
}
