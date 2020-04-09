package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cms/page")
public class CmsPageController implements CmsPageControllerApi {

    @Override
    @GetMapping("/list/{page}/{size}")  //get方法的requestMapping
    public QueryResponseResult findList(@PathVariable int page, @PathVariable int size, QueryPageRequest queryPageRequest) {
        QueryResult queryResult=new QueryResult();
        List<CmsPage> list=new ArrayList<>();
        CmsPage cmsPage=new CmsPage();
        cmsPage.setPageName("測試頁面");
        list.add(cmsPage);
        queryResult.setList(list);
        queryResult.setTotal(1L);
        QueryResponseResult result = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return result;
    }
}
