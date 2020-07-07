package com.xuecheng.learning.mq;

import com.rabbitmq.client.Channel;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.CourseLearningService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ChooseCourseTask {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CourseLearningService courseLearningService;

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE)
    public void receiveChooseCourseTask(XcTask xcTask, Message message, Channel channel) throws IOException {
        ResponseResult responseResult = courseLearningService.addCourse(xcTask);
        if (responseResult.isSuccess()){
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE,
                    RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY,xcTask.getId());
        }
    }
}
