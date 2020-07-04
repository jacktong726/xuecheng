package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "用户中心",description = "用户中心管理")
public interface UcenterControllerApi {
    public XcUserExt getUserext(String username);


}
