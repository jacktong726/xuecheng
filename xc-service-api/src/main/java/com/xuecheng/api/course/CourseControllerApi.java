package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "課程管理系统", tags = {"課程管理接口，提供增，删，改，查"})
public interface CourseControllerApi {
    @ApiOperation("查询所有教學計劃樹狀列表")
    TeachplanNode findTeachPlanList(String courseId);

    @ApiOperation("新增教學計劃")
    ResponseResult addTeachPlan(Teachplan teachplan);
}
