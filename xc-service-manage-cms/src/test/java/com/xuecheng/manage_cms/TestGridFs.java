package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestGridFs {

    @Autowired
    GridFsTemplate gridFsTemplate;

    //定义了Mongodb的配置类(MongoConfig)後, 就可以注入GridFSBucket
    @Autowired
    GridFSBucket gridFSBucket;

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

    //從mongodb中讀取頁面
    //步驟:
    //用_id通過gridFsTemplate查出文件的objectId(mongodb: 在fs.files中找文件信息)
    //用objectId通過gridFSBucket建立下載流(mongodb: 在fs.chunks找出文件內容)
    //取得下載流文件 GridFsResource(mongodb: fs.files和fs.chunks的資料合併返回成一個文件流)
    //使用apache的ioutils工具, 輸出內容
    @Test
    public void testGet() throws IOException {
        GridFSFile gridFsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5eaa6c7c9060543aac87c7da")));
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFsFile.getObjectId());
        GridFsResource gridFsResource = new GridFsResource(gridFsFile, gridFSDownloadStream);
        String s = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
        System.out.println(s);
    }
}
