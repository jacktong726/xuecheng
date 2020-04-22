package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "cms页面管理系统", tags = {"cmsSite管理接口，提供增，删，改，查"})
public interface CmsSiteControllerApi {
    @ApiOperation("查询所有CmsSite列表")
    public QueryResponseResult findAll() ;
}
