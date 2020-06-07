package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "課程管理系统", tags = {"課程管理接口，提供增，删，改，查"})
public interface CourseControllerApi {
    @ApiOperation("查询所有教學計劃樹狀列表")
    TeachplanNode findTeachPlanList(String courseId);

    @ApiOperation("新增教學計劃")
    ResponseResult addTeachPlan(Teachplan teachplan);

    @ApiOperation("新增課程")
    ResponseResult addCourseBase(CourseBase courseBase);

    @ApiOperation("修改課程")
    ResponseResult updateCourseBase(String id,CourseBase courseBase);

    @ApiOperation("按id查詢課程")
    CourseBase findCourseBaseById(String id);

    @ApiOperation("分頁查詢課程列表")
    QueryResponseResult findCoursePage(int page, int size,CourseListRequest courseListRequest);

    @ApiOperation("获取课程营销信息")
    public CourseMarket getCourseMarketById(String courseId);

    @ApiOperation("更新课程营销信息")
    public ResponseResult updateCourseMarket(String id,CourseMarket courseMarket);

}
