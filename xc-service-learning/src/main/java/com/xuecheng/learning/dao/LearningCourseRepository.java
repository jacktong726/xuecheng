package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.learning.XcLearningCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningCourseRepository extends JpaRepository<XcLearningCourse,String> {
    Optional<XcLearningCourse> findByUserIdAndAndCourseId(String userId,String CourseId);
}
