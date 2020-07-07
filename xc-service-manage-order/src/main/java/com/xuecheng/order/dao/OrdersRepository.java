package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.order.XcOrders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<XcOrders,String> {
}
