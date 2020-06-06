package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {

    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    @Test
    public void testSysDicDao(){
        SysDictionary sysdic = sysDictionaryRepository.findByDType("200");
        System.out.println(sysdic);

    }
}
