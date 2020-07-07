package com.xuecheng.order.mq;

import com.rabbitmq.client.Channel;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class ChooseCourseTask {

    @Autowired
    TaskService taskService;

    //每隔10秒扫描消息表，向mq发送消息
    @Scheduled(fixedDelay = 5000)
    public void sendChooseCourseTask(){
        Date date = new Date();
        Date updateTime = DateUtils.addMinutes(date, -1);
        List<XcTask> taskList = taskService.findTaskList(updateTime, 100);
        for (XcTask xcTask : taskList) {
            /**
             * 考虑订单服务将来会集群部署，为了避免任务在1分钟内重复执行，这里使用乐观锁，实现思路如下：
             1) 每次取任务时判断当前版本及任务id是否匹配，如果匹配则执行任务，如果不匹配则取消执行。
             2) 如果当前版本和任务Id可以匹配到任务则更新当前版本加1.
             */
            Integer version = xcTask.getVersion();
            String taskId = xcTask.getId();
            if (taskService.getTask(taskId,version)>0){
                taskService.publish(xcTask);
            }

        }
    }

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChooseCourseTask(String taskId, Message message, Channel channel){
        taskService.finishTask(taskId);
    }
}
