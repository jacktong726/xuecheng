package com.xuecheng.manage_cms_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * 创建Cms Client工程作为页面发布消费方，将Cms Client部署在多个服务器上，它负责接收到页面发布的消息后从
 * GridFS中下载文件在本地保存。
 * 需求:
 * 1、将cms Client部署在服务器，配置队列名称和站点ID。
 * 2、cms Client连接RabbitMQ并监听各自的“页面发布队列”
 * 3、cms Client接收页面发布队列的消息
 * 4、根据消息中的页面id从mongodb数据库下载页面到本地
 * */
@SpringBootApplication
public class ManageCmsClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageCmsClientApplication.class,args);
    }
}
