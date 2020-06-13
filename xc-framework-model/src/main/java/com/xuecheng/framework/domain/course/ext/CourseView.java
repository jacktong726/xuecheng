package com.xuecheng.framework.domain.course.ext;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
/**
 * 這個pojo是用來顯示課程詳情頁面用的
 * */
@Data
@ToString
@NoArgsConstructor
public class CourseView implements Serializable {
    private CourseBase courseBase;
    private CourseMarket courseMarket;
    private CoursePic coursePic;
    private TeachplanNode teachplanNode;
}
