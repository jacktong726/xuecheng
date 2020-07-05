package com.xuecheng.manage_course.exception;

import com.xuecheng.framework.exception.CustomExceptionHandler;
import com.xuecheng.framework.model.response.CommonCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
/**
 * 這個ExceptionHandler繼承自common包裡的CustomExceptionHandler,
 * 把spring Security拋出的AccessDeniedException打包, 向用戶端返回CommonCode.UNAUTHORISE信息
 * 因為common包沒有依賴spring security, 所以不能把這個exception直接寫到 CustomExceptionHandler中, 要在course包中建立它的繼承類
 * 原CustomExceptionHandler的異常處理都會被繼承使用, 並在builder中增加course包獨有的exception
 * */
@ControllerAdvice
public class CustomExceptionHandlerExt extends CustomExceptionHandler {
    static {
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }
}
