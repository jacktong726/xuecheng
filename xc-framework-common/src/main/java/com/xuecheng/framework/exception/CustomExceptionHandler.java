package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 使用 @ControllerAdvice + @ExceptionHandler 进行全局的异常处理，不用在 Controller 层进行 try-catch
 * 注意: 必須在com.xuecheng.manage_cms.ManageCmsApplication中掃描此包才能生效
 * */
@ControllerAdvice
public class CustomExceptionHandler {
    //引入log4j類記錄錯誤信息
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(CustomException.class)    //只有CustomException被處理
    @ResponseBody   //轉化結果為json格式
    public ResponseResult customExceptionHandler(CustomException e){
        LOGGER.error("catch exception : {}\r\nexception: ",e.getMessage(), e);
        return new ResponseResult(e.getResultCode());
    }
}
