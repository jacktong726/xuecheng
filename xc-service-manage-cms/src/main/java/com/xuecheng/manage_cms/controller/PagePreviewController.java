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
//            由于Nginx先请求cms的课程预览功能得到html页面，再解析页面中的ssi标签，这里必须保证cms页面预览返回的
//            页面的Content-Type为text/html;charset=utf-8
//            ssi标签: <!--#include virtual="/include/header.html"-->
            response.setHeader("Content-type","text/html;charset=utf-8");
            ServletOutputStream servletOutputStream=response.getOutputStream();
            servletOutputStream.write(pageHtml.getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
