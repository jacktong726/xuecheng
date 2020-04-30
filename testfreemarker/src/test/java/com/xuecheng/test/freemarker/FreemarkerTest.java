package com.xuecheng.test.freemarker;

import com.xuecheng.test.freemarker.model.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FreemarkerTest {

    //基于模板生成静态化文件
    @Test
    public void testGenerateHtml() throws IOException, TemplateException {
//创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
//设置模板路径
        String classpath = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
//设置字符集
        configuration.setDefaultEncoding("utf-8");
//加载模板
        Template template = configuration.getTemplate("test1.ftl");
//数据模型
        Map<String, Object> map = getMap();
//静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//静态化内容
        System.out.println(content);
        InputStream inputStream = IOUtils.toInputStream(content);
//输出文件
        FileOutputStream outputStream = new FileOutputStream(new File(classpath + "/test1.html"));
        int copy = IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }

    public Map getMap(){
        Map<String, Object> map=new HashMap<>();
        map.put("name", "黑马程序员");
        Student stu1 = new Student();
        stu1.setName("小明");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);
        //stu2.setBirthday(new Date());
        List<Student> friends = new ArrayList<>();
        friends.add(stu1);
        stu2.setFriends(friends);
        stu2.setBestFriend(stu1);
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);
        //向数据模型放数据
        map.put("stus", stus);
        //准备map数据
        HashMap<String, Student> stuMap = new HashMap<>();
        stuMap.put("stu1", stu1);
        stuMap.put("stu2", stu2);
        //向数据模型放数据
        map.put("stu1", stu1);
        //向数据模型放数据
        map.put("stuMap", stuMap);
        return map;
    }
}