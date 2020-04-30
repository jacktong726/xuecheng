package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

@Controller
public class PagePreviewController extends BaseController {

    @Autowired
    CmsPageService cmsPageService;

    @RequestMapping("/cms/preview/{pageId}")
    public void preview(@PathVariable("pageId") String pageId){
        String pageHtml = cmsPageService.getPageHtml(pageId);
        try {
            ServletOutputStream servletOutputStream=response.getOutputStream();
            servletOutputStream.write(pageHtml.getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
