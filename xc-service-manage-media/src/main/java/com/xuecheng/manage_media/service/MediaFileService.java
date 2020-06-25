package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class MediaFileService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;

    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        if (queryMediaFileRequest==null){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        MediaFile mediaFile = new MediaFile();
        String fileOriginalName = queryMediaFileRequest.getFileOriginalName();
        String processStatus = queryMediaFileRequest.getProcessStatus();
        String tag = queryMediaFileRequest.getTag();
        if (!StringUtils.isEmpty(fileOriginalName)){
            mediaFile.setFileOriginalName(fileOriginalName);
        }
        if (!StringUtils.isEmpty(processStatus)){
            mediaFile.setProcessStatus(processStatus);
        }
        if (!StringUtils.isEmpty(tag)){
            mediaFile.setTag(tag);
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("processStatus", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<MediaFile> mediaFileExample = Example.of(mediaFile, matcher);

        if (page<=0){
            page=1;
        }
        Page<MediaFile> mediaFilePage = mediaFileRepository.findAll(mediaFileExample, PageRequest.of(page-1, size));
        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setList(mediaFilePage.getContent());
        queryResult.setTotal(mediaFilePage.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    public ResponseResult delete(String fileId) {
        Optional<MediaFile> optional = mediaFileRepository.findById(fileId);
        if (!optional.isPresent()){
            throw new CustomException(MediaCode.FILE_NOT_EXIST);
        }
        MediaFile mediaFile = optional.get();
        String fileRelativePath = mediaFile.getFilePath();
        String filePath=upload_location+"/"+fileRelativePath;
        File file = new File(filePath);
        boolean b = deleteDir(file);
        if (!b){
            throw new CustomException(MediaCode.DELETE_FILE_FAIL);
        }
        mediaFileRepository.deleteById(fileId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private boolean deleteDir(File dir){
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir
                        (new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        if(dir.delete()) {
            System.out.println("目录已被删除！");
            return true;
        } else {
            System.out.println("目录删除失败！");
            return false;
        }
    }
}
