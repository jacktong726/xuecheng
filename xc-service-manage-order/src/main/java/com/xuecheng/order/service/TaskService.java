package com.xuecheng.order.service;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    XcTaskRepository xcTaskRepository;

    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //取出前n条任务,取出指定时间之前处理的任务
    public List<XcTask> findTaskList(Date updateTime, int n){
        Page<XcTask> xcTaskPage = xcTaskRepository.findByUpdateTimeBefore(PageRequest.of(0, n), updateTime);
        return xcTaskPage.getResult();
    }

    public void publish(XcTask xcTask){
        String mqExchange = xcTask.getMqExchange();
        String mqRoutingkey = xcTask.getMqRoutingkey();
        rabbitTemplate.convertAndSend(mqExchange,mqRoutingkey,xcTask);
        xcTask.setUpdateTime(new Date());
        xcTaskRepository.save(xcTask);
    }

    //使用乐观锁方法校验任务
    @Transactional
    public int getTask(String taskId,int version){
        int i = xcTaskRepository.updateTaskVersion(taskId, version);    //如果成功的話, XcTask的version會+1
        return i;   //如果i<=0, 代表有關記錄被鎖了(version被其他線程+1),不能成功更新
    }

    @Transactional
    public void finishTask(String taskId){
        Optional<XcTask> optional = xcTaskRepository.findById(taskId);
        if (optional.isPresent()){
            XcTask xcTask = optional.get();
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.deleteById(taskId);
        }
    }
}
