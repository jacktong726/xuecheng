package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.xuecheng.manage_cms_client.service.CmsPageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConsumerPostPage {

    @Autowired
    CmsPageService cmsPageService;

    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void PostPage(String msg, Message message, Channel channel){
        Map map = JSON.parseObject(msg, Map.class);
        String pageId = (String) map.get("pageId");
        if (StringUtils.isNotEmpty(pageId)){
            cmsPageService.savePageToServerPath(pageId);
        }
    }
}
