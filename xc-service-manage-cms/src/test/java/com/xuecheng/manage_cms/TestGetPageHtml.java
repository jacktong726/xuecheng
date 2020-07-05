package com.xuecheng.manage_cms;

import com.xuecheng.manage_cms.service.CmsPageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestGetPageHtml {

    @Autowired
    CmsPageService cmsPageService;

    @Test
    public void testGetPage(){
        //String pageHtml = cmsPageService.getPageHtml("5abefd525b05aa293098fca6");
        //System.out.println(pageHtml);
    }
}
