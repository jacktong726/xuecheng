package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachPlanMediaPubRepository extends JpaRepository<TeachplanMediaPub,String> {
    void deleteByCourseId(String courseId);
}
