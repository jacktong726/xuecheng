package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class CmsPageService {
    @Autowired
    CmsPageRepository cmsPageRepository;

    /**
     * 分頁查詢
     * QueryResponseResult是自定義對象, 包裝了QueryResult和ResultCode(也是自定義對象)
     * 其中queryResult包裝了結果集和記錄條數
     * @QueryPageRequest 封裝的查詢條件如下：
     * 站点Id：精确匹配
     * 模板Id：精确匹配
     * 页面别名：模糊匹配
     * */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
        //防止調用queryPageRequest發生空指針
        if (queryPageRequest==null){
            queryPageRequest=new QueryPageRequest();
        }

        //先判斷page和size參數是否合法(>0), 如不合法則設為默認值
        if (page<=0){
            page=1;
        }
        if (size<=0){
            size=10;
        }

        //創建一個條件的example
        //首先使用queryPageRequest帶有的屬性, 新建一個CmsPage對象
        CmsPage cmsPage = new CmsPage();
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())){  //StringUtils是apache的工具包
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //再創建一個ExampleMatcher, 如果是完全匹配, 寫ExampleMatcher.matching()就夠了
            //如果有模糊匹配, 就要加上.withMatcher("對應的屬性",ExampleMatcher.GenericPropertyMatchers.contains());
            //除了contain外, 還有endsWith(),startsWith()等其他方法
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        //最後使用Example.of(cmsPage,matcher)構建出example對象
        Example<CmsPage> example=Example.of(cmsPage,matcher);

        //調用dao
        //.findAll(Example,Pageable)
        //Page<CmsPage> all = cmsPageRepository.findAll(PageRequest.of(page - 1, size));
        Page<CmsPage> all = cmsPageRepository.findAll(example,PageRequest.of(page - 1, size));
        //建構QueryResult對象
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        //建構及返回QueryResponseResult對象
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);

    }
}
