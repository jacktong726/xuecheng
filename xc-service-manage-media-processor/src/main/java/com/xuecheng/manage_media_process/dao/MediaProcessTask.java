package com.xuecheng.manage_media_process.dao;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {

    @Autowired
    MediaFileRepository mediaFileRepository;

    //ffmpeg绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg_path;
    //上传文件根目录
    @Value("${xc-service-manage-media.video-location}")
    String serverPath;
    
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",
                    containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg){
        //解析mediaId, 返回mediaFile對象
        Map<String,String> map = JSON.parseObject(msg, Map.class);
        String mediaId = map.get("mediaId");
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()){
            return;
        }
        MediaFile mediaFile = optional.get();
        //目前只處理avi, 其他不處理
        if (!"avi".equalsIgnoreCase(mediaFile.getFileType())){
            mediaFile.setProcessStatus("303004");//处理状态为无需处理
            mediaFileRepository.save(mediaFile);
            return ;
        }else{
            mediaFile.setProcessStatus("303001");//处理状态为未处理
            mediaFileRepository.save(mediaFile);
        }
        //利用mp4VideoUtil生成mp4
        //String ffmpeg_path, String video_path, String mp4_name, String mp4folder_path
        String video_path = serverPath+mediaFile.getFilePath()+mediaFile.getFileId()+".avi";
        String mp4_name=mediaFile.getFileId()+".mp4";
        String mp4folder_path=serverPath+mediaFile.getFilePath();
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        String result = mp4VideoUtil.generateMp4();
        if (!"success".equals(result)){
            mediaFile.setProcessStatus("303003");//处理状态为处理失败
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        //利用hlsVideoUtil生成h3u8
        //String ffmpeg_path, String video_path, String m3u8_name,String m3u8folder_path
        String m3u8_name=mediaFile.getFileId()+".m3u8";
        String m3u8folder_path=serverPath+mediaFile.getFilePath()+"hls/";
        String mp4_video_path=serverPath+mediaFile.getFilePath()+mediaFile.getFileId()+".mp4";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path, mp4_video_path, m3u8_name, m3u8folder_path);
        String result2 = hlsVideoUtil.generateM3u8();
        if (!"success".equals(result2)){
            mediaFile.setProcessStatus("303003");//处理状态为处理失败
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result2);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        mediaFile.setProcessStatus("303002");   //处理状态为处理成功
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        mediaFile.setFileUrl(mediaFile.getFilePath()+"hls/"+mediaId+".m3u8");
        mediaFileRepository.save(mediaFile);

    }

}
