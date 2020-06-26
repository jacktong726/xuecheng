package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.MediaCode;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.learning.client.CourseSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseLearningService {

    @Autowired
    CourseSearchClient courseSearchClient;

    public GetMediaResult getmedia(String courseId, String teachplanId) {
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);
        if (teachplanMediaPub==null){
            throw new CustomException(MediaCode.LEARNING_GETMEDIA_ERROR);
        }
        String mediaUrl = teachplanMediaPub.getMediaUrl();
        return new GetMediaResult(CommonCode.SUCCESS,mediaUrl);
    }
}
