package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;

    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //檢查檔案是否存在+mongodb是否有記錄, 兩者都有才視為已存在
        //1.檢查文件是否存在
        boolean fileExist = checkFileExist(fileMd5, fileExt);
        //2.檢查文件是存在於mongodb中
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (fileExist && optional.isPresent()){
            throw new CustomException(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CheckChunkResult checkChunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //檢查文件分塊是否已存在
        String fileChunkFolderPath = getFileChunkFolderPath(fileMd5, chunk);
        File fileChunk = new File(fileChunkFolderPath +"/"+ chunk);
        if (fileChunk.exists()){
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,true);
        }
        return new CheckChunkResult(CommonCode.SUCCESS,false);
    }

    public ResponseResult uploadChunk(MultipartFile file, Integer chunk, String fileMd5) {
        //建立上傳目錄, 如不存在則創建
        File fileChunkFolder = new File(getFileFolderPath(fileMd5)+"/chunk/");
        if (!fileChunkFolder.exists()){
            fileChunkFolder.mkdirs();   //注意: mkdir只能創建一級目錄
        }
        //建立file input和output stream, 讀取上傳文件並寫入到服務器中
        InputStream inputStream =null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = file.getInputStream();
            String fileChunkFolderPath = getFileChunkFolderPath(fileMd5, chunk);
            File fileChunk = new File(fileChunkFolderPath +"/"+ chunk);
            fileOutputStream = new FileOutputStream(fileChunk); //注意: 寫入新檔時, 檔案可以不存在, 但目錄一定要存在
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException(MediaCode.CHUNK_FILE_UPLOAD_FAIL);
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public ResponseResult mergeChunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1.合併chunk file
        File mergeFile = mergeFile(fileMd5, fileExt);
        if (mergeFile==null){
            throw new CustomException(MediaCode.MERGE_FILE_FAIL);
        }
        //2.檢查合併文件的md5和上傳過來的md5是否一致, 如否代表上傳不完整
        boolean b = checkMd5(mergeFile, fileMd5);
        if (!b){
            throw new CustomException(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //3.上傳到mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件路径保存相对路径
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5,fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);;
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //檢查文件是否存在
    private boolean checkFileExist(String fileMd5,String fileExt){
        String filePath = getFilePath(fileMd5,fileExt);
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     */
    private String getFilePath(String fileMd5,String fileExt){
        return upload_location+"/"+
                fileMd5.substring(0,1)+
                "/"+fileMd5.substring(1,2)+
                "/"+fileMd5+
                "/"+fileMd5+"."+fileExt;
    }

    private String getFileFolderPath(String fileMd5){
        return upload_location+"/"+
                fileMd5.substring(0,1)+
                "/"+fileMd5.substring(1,2)+
                "/"+fileMd5;
    }

    private String getFileFolderRelativePath(String fileMd5, String fileExt) {
        return fileMd5.substring(0,1)+
                "/"+fileMd5.substring(1,2)+
                "/"+fileMd5+
                "/"+fileMd5+"."+fileExt;
    }

    private String getFileChunkFolderPath(String fileMd5, Integer chunk){
        return upload_location+"/"+
                fileMd5.substring(0,1)+
                "/"+fileMd5.substring(1,2)+
                "/"+fileMd5+
                "/chunk";
    }

    private File mergeFile(String fileMd5,String fileExt){
        //創建chunks文件list並排序
        String fileFolderPath = getFileFolderPath(fileMd5);
        File file = new File(fileFolderPath + "/chunk/");
        File[] files = file.listFiles();
        List<File> chunkFileList = Arrays.asList(files);
        Collections.sort(chunkFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int o1name = Integer.parseInt(o1.getName());
                int o2name = Integer.parseInt(o2.getName());
                return o1name-o2name;
            }
        });
        //創建合併文件(如已存在則刪除舊檔)
        File mergeFile = new File(fileFolderPath + "/" + fileMd5 + "." + fileExt);
        if (mergeFile.exists()){
            mergeFile.delete();
        }
        //合併文件
        try {
            boolean newFile = mergeFile.createNewFile();
            RandomAccessFile writeFile = new RandomAccessFile(mergeFile, "rw");
            byte[] b = new byte[1024];
            for (File chunkFile : chunkFileList) {
                RandomAccessFile readFile = new RandomAccessFile(chunkFile, "r");
                int len=-1;
                while ((len=readFile.read(b))!=-1){
                    writeFile.write(b,0,len);
                }
                readFile.close();
            }
            writeFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mergeFile;
    }

    //檢查文件md5是否一致
    private boolean checkMd5(File file,String fileMd5){
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            if (fileMd5.equals(md5Hex)){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
