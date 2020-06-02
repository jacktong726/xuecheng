package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface TeachPlanMapper {
    TeachplanNode findTeachPlanList(String courseId);
}
