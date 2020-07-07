package com.xuecheng.order.dao;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.task.XcTask;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public interface XcTaskRepository extends JpaRepository<XcTask,String> {
    //返回指定時間前的信息任務列表
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable, Date updateTime);

    //使用乐观锁方式校验任务id和版本号是否匹配，匹配则版本号加1
    @Modifying
    @Transactional
    @Query("update XcTask t set t.version=t.version+1 where t.id =:id and t.version =:version")
    int updateTaskVersion(@Param(value = "id") String id,@Param(value = "version") int version);
}
