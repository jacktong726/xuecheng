package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "cmsConfig管理系统", tags = {"cmsConfig主要為freemarker服務, 為頁面靜態化提供數據模型"})
public interface CmsConfigControllerApi {
    @ApiOperation("按id查询CmsConfig")
    public CmsConfig findById(String id) ;
}
