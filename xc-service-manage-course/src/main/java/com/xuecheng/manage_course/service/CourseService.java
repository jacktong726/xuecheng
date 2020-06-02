package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.manage_course.dao.TeachPlanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseService {
    @Autowired
    TeachPlanMapper teachPlanMapper;

    public TeachplanNode findTeachPlanList(String courseId){
        return teachPlanMapper.findTeachPlanList(courseId);
    }
}
