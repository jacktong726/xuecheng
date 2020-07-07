package com.xuecheng.learning.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.learning.response.MediaCode;
import com.xuecheng.framework.domain.order.XcOrders;
import com.xuecheng.framework.domain.order.XcOrdersDetail;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.LearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CourseLearningService {

    @Autowired
    CourseSearchClient courseSearchClient;

    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    @Autowired
    LearningCourseRepository learningCourseRepository;

    public GetMediaResult getmedia(String courseId, String teachplanId) {
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);
        if (teachplanMediaPub==null){
            throw new CustomException(MediaCode.LEARNING_GETMEDIA_ERROR);
        }
        String mediaUrl = teachplanMediaPub.getMediaUrl();
        return new GetMediaResult(CommonCode.SUCCESS,mediaUrl);
    }

    //向xc_learning_course添加记录，为保证不重复添加选课，先查询历史任务表，如果从历史任务表查询不到任务说
    //明此任务还没有处理，此时则添加选课并添加历史任务。
    @Transactional
    public ResponseResult addCourse(XcTask xcTask){
        if(xcTask == null || StringUtils.isEmpty(xcTask.getId()) || StringUtils.isEmpty(xcTask.getRequestBody())){
            throw new CustomException(LearningCode.CHOOSECOURSE_TASKISNULL);
        }

        //查xc_task_his表,如未做過此任務, 則進行任務並記錄到xc_task_his表; 如已做過直接返回commonCode.success, 不再進行
        Optional<XcTaskHis> xcTaskHisOptional = xcTaskHisRepository.findById(xcTask.getId());
        if (!xcTaskHisOptional.isPresent()){
            //新增到xc_task_his表
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHis.setUpdateTime(new Date());
            xcTaskHisRepository.save(xcTaskHis);
        }else{
            return new ResponseResult(CommonCode.SUCCESS);
        }

        //在xcTask中取得requestBody欄位, 這個欄位放的就是json格式的XcOrder
        String requestBody = xcTask.getRequestBody();
        System.out.println(requestBody);
        XcOrders xcOrders = JSON.parseObject(requestBody, XcOrders.class);
        String userId = xcOrders.getUserId();
        String details = xcOrders.getDetails();
        List<XcOrdersDetail> xcOrdersDetailsList = JSON.parseArray(details, XcOrdersDetail.class);
        for (XcOrdersDetail detail : xcOrdersDetailsList) {
            Date startTime = detail.getStartTime();
            Date endTime = detail.getEndTime();
            String valid = detail.getValid();
            String courseId = detail.getItemId();

            //查learning_course表, 已有此課程則修改, 無則新增
            Optional<XcLearningCourse> learningCourseOptional = learningCourseRepository.findByUserIdAndAndCourseId(userId, courseId);
            if (learningCourseOptional.isPresent()){
                XcLearningCourse xcLearningCourse = learningCourseOptional.get();
                xcLearningCourse.setStartTime(startTime);
                xcLearningCourse.setEndTime(endTime);
                xcLearningCourse.setValid(valid);
                xcLearningCourse.setStatus("501001");
                learningCourseRepository.save(xcLearningCourse);
            }else {
                XcLearningCourse xcLearningCourse = new XcLearningCourse();
                xcLearningCourse.setUserId(userId);
                xcLearningCourse.setCourseId(courseId);
                xcLearningCourse.setStartTime(startTime);
                xcLearningCourse.setEndTime(endTime);
                xcLearningCourse.setStatus("501001");
                xcLearningCourse.setValid(valid);
                learningCourseRepository.save(xcLearningCourse);
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
