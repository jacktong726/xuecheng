package com.xuecheng.Manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFile {

    /*文件分槐測試*/
    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("F:\\javalesson\\javahw\\xcedu\\video\\lucene.avi"); //要分塊的源文件
        String chunkPath="F:\\javalesson\\javahw\\xcedu\\video\\chunk\\";   //分塊文件存放路徑
        long chunkSize=1*1024*1024;     //分塊大小上限: 1mb
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);    //計算分塊數量
        RandomAccessFile readFile = new RandomAccessFile(sourceFile,"r");   //建立源文件讀取流
        byte[] b = new byte[1024];  //讀取流緩存大小(每次讀取1024bytes)
        for (int i = 0; i <chunkNum ; i++) {    //逐塊文件寫入
            File chunkFile = new File(chunkPath + i);   //塊文件路徑名稱
            RandomAccessFile writeFile = new RandomAccessFile(chunkFile, "rw"); //建立塊文件寫入流
            int len=-1; //初始化寫入長度變量
            while ((len=readFile.read(b))!=-1){ //每次由源文件讀取1024bytes數據, 直至整份文件讀取完成(返回len=-1)
                writeFile.write(b,0,len);   //寫入數據到塊文件
                if (chunkFile.length()>chunkSize){ //如果塊文件大小達到1024kb, 結束while循環
                    break;
                }
            }
            writeFile.close();  //塊文件寫入完成, 結束寫入流
        }
        readFile.close();   //源文件讀取完成, 結束讀取流

    }

    @Test
    public void TestMerge() throws IOException {
        String chunkPath="F:\\javalesson\\javahw\\xcedu\\video\\chunk\\";   //分塊文件存放路徑
        File chunkFile = new File(chunkPath);
        File[] files = chunkFile.listFiles();
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int o2name = Integer.parseInt(o2.getName());
                int o1name = Integer.parseInt(o1.getName());
                return o1name-o2name;
            }
        });
        File mergeFile = new File("F:\\javalesson\\javahw\\xcedu\\video\\lucene_merge.avi");
        boolean newFile = mergeFile.createNewFile();
        RandomAccessFile writeFile = new RandomAccessFile(mergeFile, "rw");
        writeFile.seek(0);
        byte[] b = new byte[1024];
        for (File file : fileList) {
            RandomAccessFile readFile = new RandomAccessFile(file, "r");
            int len=-1;
            while ((len=readFile.read(b))!=-1){
                writeFile.write(b,0,len);
            }
            readFile.close();
        }
        writeFile.close();
    }
}
