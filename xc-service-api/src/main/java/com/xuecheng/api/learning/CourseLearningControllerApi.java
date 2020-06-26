package com.xuecheng.api.learning;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "学习服务",tags = {"学习服务管理"})
public interface CourseLearningControllerApi {

    @ApiOperation("获取课程学习地址")
    public GetMediaResult getmedia(String courseId,String teachplanId);
}
