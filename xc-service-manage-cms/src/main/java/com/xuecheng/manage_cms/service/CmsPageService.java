package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @QueryPageRequest 封裝的查詢條件
     * */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
        //先判斷page和size參數是否合法(>0), 如不合法則設為默認值
        if (page<=0){
            page=1;
        }
        if (size<=0){
            size=10;
        }
        //調用dao
        Page<CmsPage> all = cmsPageRepository.findAll(PageRequest.of(page - 1, size));
        //建構QueryResult對象
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        //建構及返回QueryResponseResult對象
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);

    }
}
