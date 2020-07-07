package com.xuecheng.order;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.order.XcOrders;
import com.xuecheng.framework.domain.order.XcOrdersDetail;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.utils.IdWorker;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.dao.OrdersRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    @Autowired
    XcTaskRepository xcTaskRepository;

    @Autowired
    OrdersRepository ordersRepository;

    @Autowired
    IdWorker idWorker;

    @Test
    @Transactional
    @Commit
    public void test01(){
        xcTaskRepository.updateTaskVersion("1",1);
    }

    @Test
    public void test02(){
        List<XcOrders> list = ordersRepository.findAll();
        for (XcOrders xcOrders : list) {
            String s = JSON.toJSONString(xcOrders);
            System.out.println(s);
        }
    }

    @Test
    public void test03(){
        Optional<XcOrders> optional = ordersRepository.findById("319867403872112640");
        XcOrders xcOrders1 = optional.get();
        String s = JSON.toJSONString(xcOrders1);
        System.out.println(s);
        XcOrders xcOrders = JSON.parseObject(s, XcOrders.class);
        System.out.println(xcOrders);
        //        Map map = JSON.parseObject(s, Map.class);
//        String detailsString= (String) map.get("details");
//        List<Map> list = JSON.parseArray(detailsString, Map.class);
//        for (Map itemMap : list) {
//            System.out.println(itemMap.get("itemId"));
//        }
        //Map details = JSON.parseObject(detailsString, Map.class);
        //System.out.println(details);
    }

    @Test
    @Commit
    public void test04(){
        XcOrders xcOrders = new XcOrders();
        xcOrders.setOrderNumber(idWorker.nextId()+"");
        xcOrders.setUserId("9999");
        xcOrders.setPrice(1000F);
        xcOrders.setStartTime(new Date());
        xcOrders.setEndTime(new Date());

        XcOrdersDetail xcOrdersDetail = new XcOrdersDetail();
        xcOrdersDetail.setOrderNumber(xcOrders.getOrderNumber());
        xcOrdersDetail.setItemId(idWorker.nextId()+"");
        xcOrdersDetail.setItemNum(10);
        xcOrdersDetail.setStartTime(new Date());
        xcOrdersDetail.setEndTime(new Date());
        List<XcOrdersDetail> list = new ArrayList<>();
        list.add(xcOrdersDetail);

        xcOrders.setDetails(JSON.toJSONString(list));
        ordersRepository.save(xcOrders);

        XcTask xcTask = new XcTask();
        xcTask.setCreateTime(DateUtils.addMinutes(new Date(),-30));
        xcTask.setUpdateTime(DateUtils.addMinutes(new Date(),-29));
        xcTask.setMqExchange(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE);
        xcTask.setMqRoutingkey(RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE_KEY);
        xcTask.setVersion(1);
        xcTask.setRequestBody(JSON.toJSONString(xcOrders));

        System.out.println(xcTask);
        xcTaskRepository.save(xcTask);


    }
}
