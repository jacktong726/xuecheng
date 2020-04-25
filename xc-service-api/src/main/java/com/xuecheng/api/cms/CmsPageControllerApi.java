package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @Api 系列注解作用在swagger網頁顯示的內容中
 * */
@Api(value = "cms页面管理系统", tags = {"cms页面管理接口，提供增，删，改，查"})
public interface CmsPageControllerApi {

    @ApiOperation("分页查询CmsPage页面列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "path", dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页显示的个数", required = true, paramType = "path", dataType = "int")
    })
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) ;

    @ApiOperation("新增CmsPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cmsPage",value = "cmsPage",required = true,paramType = "body",dataType = "CmsPage")
    })
    public ResponseResult add(CmsPage cmsPage) ;

    @ApiOperation("按id查詢CmsPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "頁面id",required = true,paramType = "path",dataType = "String")
    })
    public CmsPageResult findById(String id) ;

    @ApiOperation("按id修改CmsPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "頁面id",required = true,paramType = "path",dataType = "String"),
            @ApiImplicitParam(name = "cmsPage",value = "cmsPage",required = true,paramType = "body",dataType = "CmsPage")
    })
    public ResponseResult update(String id,CmsPage cmsPage) ;

    @ApiOperation("按id刪除CmsPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "頁面id",required = true,paramType = "path",dataType = "String")
    })
    public ResponseResult delete(String id) ;
}
