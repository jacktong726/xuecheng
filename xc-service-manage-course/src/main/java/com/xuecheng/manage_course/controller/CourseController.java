package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/course")
public class CourseController implements CourseControllerApi {

    @Autowired
    CourseService courseService;

    @Autowired
    HttpServletRequest request;

    @Override
    @PreAuthorize("hasAuthority('course_get_teachplanList')")   //方法授權(測試時userDetail中有寫入這個權限, 可以正常訪問)
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachPlanList(@PathVariable("courseId")String courseId) {
        return courseService.findTeachPlanList(courseId);
    }

    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachPlan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachPlan(teachplan);
    }

    @Override
    @PostMapping("/coursebase/add")
    public ResponseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    @Override
    @PutMapping("/coursebase/{id}")
    public ResponseResult updateCourseBase(@PathVariable("id") String id, @RequestBody CourseBase courseBase) {
        return courseService.updateCourseBase(id,courseBase);
    }

    @Override
    @GetMapping("/coursebase/{id}")
    //@PreAuthorize("hasAuthority('course_get_coursebase')")  //方法授權(測試時userDetail中未有寫入這個權限, 不能訪問)
    public CourseBase findCourseBaseById(@PathVariable String id) {
        return courseService.findCourseBaseById(id);
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
        //String companyId="1";
        //ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //HttpServletRequest request = servletRequestAttributes.getRequest();

        //前端的請求都帶有authorization: Bearer token的請求頭
        //解析token就可以得到帶有的companyId信息
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        XcOauth2Util.UserJwt userJwtFromHeader = xcOauth2Util.getUserJwtFromHeader(request);
        if (userJwtFromHeader==null){
            throw new CustomException(CommonCode.UNAUTHENTICATED);
        }
        String companyId = userJwtFromHeader.getCompanyId();

        if (courseListRequest==null){
            courseListRequest=new CourseListRequest();
        }
        courseListRequest.setCompanyId(companyId);
        return courseService.findCoursePage(page,size,courseListRequest);
    }

    @Override
    @GetMapping("/coursemarket/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    @Override
    @PutMapping("/coursemarket/{id}")
    public ResponseResult updateCourseMarket(@PathVariable("id") String id, @RequestBody CourseMarket courseMarket) {
        return courseService.updateCourseMarket(id,courseMarket);
    }

    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId,
                                       @RequestParam("pic") String pic) {
        return courseService.addCoursePic(courseId,pic);
    }

    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursePic(courseId);
    }

    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deletePicByCourseId(courseId);
    }

    @Override
    @GetMapping("/courseview/{id}")
    public CourseView courseview(@PathVariable("id") String id) {
        return courseService.getCourseView(id);
    }

    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String id){
        return courseService.publish(id);
    }

    @Override
    @PostMapping("/savemedia")
    public ResponseResult savemedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.savemedia(teachplanMedia);
    }
}
