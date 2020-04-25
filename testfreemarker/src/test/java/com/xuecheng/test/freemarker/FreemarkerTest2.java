package com.xuecheng.test.freemarker;

import freemarker.cache.StringTemplateLoader;
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
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FreemarkerTest2{

    //模板内容，这里测试时使用简单的字符串作为模板
    String templateString="" +
            "<html>\n" +
            " <head></head>\n" +
            " <body>\n" +
            " 名称：${name}\n" +
            " </body>\n" +
            "</html>";

    @Test
    public void testGenerateHtmlByString() throws IOException, TemplateException {
        Configuration configuration=new Configuration(Configuration.getVersion());
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateString);
        configuration.setTemplateLoader(stringTemplateLoader);
        //得到模板
        Template template = configuration.getTemplate("template","utf-8");
        //数据模型
        Map<String, Object> map = new HashMap<>();
        map.put("name","FF7大神");
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //静态化内容
        System.out.println(content);
        InputStream inputStream = IOUtils.toInputStream(content);
        //输出文件
        FileOutputStream outputStream = new FileOutputStream(new File("f:/test1.html"));
        int copy = IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }

}
