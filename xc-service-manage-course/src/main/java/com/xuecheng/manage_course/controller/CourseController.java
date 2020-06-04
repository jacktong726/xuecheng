package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController implements CourseControllerApi {

    @Autowired
    CourseService courseService;

    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachPlanList(@PathVariable("courseId")String courseId) {
        return courseService.findTeachPlanList(courseId);
    }

    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachPlan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachPlan(teachplan);
    }
    /**
     * 複習: @RequestParam用来处理简单类型的绑定, 有两个属性： value、required；
     * value用来指定要传入值的id名称，required用来指示参数是否必须绑定。
     * 例如: /coursebase/list/1/3?companyId=1234556
     * */
    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult findCoursePage(@PathVariable("page") int page,
                                              @PathVariable("size") int size,
                                              @RequestParam(value = "companyId", required = false) CourseListRequest courseListRequest) {
        return courseService.findCoursePage(page,size,courseListRequest);
    }
}
