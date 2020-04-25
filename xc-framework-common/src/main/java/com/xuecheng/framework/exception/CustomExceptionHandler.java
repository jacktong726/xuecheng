package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 使用 @ControllerAdvice + @ExceptionHandler 进行全局的controller异常处理，不用在 Controller 层进行 try-catch
 * 注意: 必須在com.xuecheng.manage_cms.ManageCmsApplication中掃描此包才能生效
 * */
@ControllerAdvice
public class CustomExceptionHandler {
    //引入log4j類記錄錯誤信息
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomExceptionHandler.class);

    //ImmutableMap是Guava包中的類, ImmutableMap的特点的一旦创建不可改变，并且线程安全
    //用這個類來放已知的系統異常集合(例如HttpMessageNotReadableException, 未寫入request body)
    private static ImmutableMap<Class<? extends Exception>, ResultCode> EXCEPTIONS;

    //使用ImmutableMap的Builder方法建立builder, 用來暫時存放exception內容
    protected static ImmutableMap.Builder builder=new ImmutableMap.Builder();

    static {
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALIDPARAM);    //request body不合法異常
        builder.put(HttpRequestMethodNotSupportedException.class,CommonCode.INVALIDMETHOD); //請求方式錯誤
    }

    @ExceptionHandler(CustomException.class)    //只有CustomException被處理
    @ResponseBody   //轉化結果為json格式
    public ResponseResult customExceptionHandler(CustomException e){
        LOGGER.error("catch exception : {}\r\nexception: ",e.getMessage(), e);
        return new ResponseResult(e.getResultCode());
    }

    @ExceptionHandler(Exception.class)    //處理其他系統報的exception
    @ResponseBody   //轉化結果為json格式
    public ResponseResult otherExceptionHandler(Exception e){
        LOGGER.error("catch exception : {}\r\nexception: ",e.getMessage(), e);
        //如果EXCEPTIONS未創建, 就使用builder把內容寫入去
        if (EXCEPTIONS==null){
            EXCEPTIONS=builder.build();
        }
        ResultCode resultCode = EXCEPTIONS.get(e.getClass());
        if (resultCode ==null){
            //如果未識別是那種異常, 就返回99999
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }else{
            //如果已在EXCEPTIONS中記錄此類異常, 則返回resultCode
            return new ResponseResult(resultCode);
        }

    }
}
