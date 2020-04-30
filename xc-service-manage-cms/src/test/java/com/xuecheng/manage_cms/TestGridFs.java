package com.xuecheng.manage_cms;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestGridFs {

    @Autowired
    GridFsTemplate gridFsTemplate;


    //把頁面模版存到mongodb中, 這裏用到mongodb的gridFs技術
    //存儲後, 文件會分為fs.chunks(文件內容)和fs.files(文件信息)兩部份, 保存到mongodb數據庫中
    //其中fs.chunks會按256K為一份, 切割文件內容
    @Test
    public void testStore() throws FileNotFoundException {
        File file = new File("F:\\javalesson\\javahw\\xcedu\\xcEduService\\testfreemarker\\src\\main\\resources\\templates\\index_banner.ftl");
        FileInputStream inputStream = new FileInputStream(file);
        ObjectId objectId = gridFsTemplate.store(inputStream, "轮播图测试文件01", "");
        //文件存储成功得到一个objectId, 是fs.files集合中的主键
        System.out.println(objectId);
    }
}
