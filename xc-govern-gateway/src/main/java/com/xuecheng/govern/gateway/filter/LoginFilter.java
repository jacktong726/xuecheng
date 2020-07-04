package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import com.xuecheng.govern.gateway.servcie.LoginService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.security.krb5.internal.AuthContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    LoginService loginService;

    @Override
    public String filterType() {
        //四种类型：pre、routing、post、error
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**請求資源前, 必須檢證用戶身份,其中必須:
        1、从cookie查询用户身份令牌是否存在，不存在则拒绝访问
            登陸時xc-service-ucenter-auth會向瀏覽器的cookies寫入access_token
        2、从http header查询jwt令牌是否存在，不存在则拒绝访问
            請求資源時, 必須帶有"Authorization":"Bearer jwt_token"的header
        3、从Redis查询user_token令牌是否过期，过期则拒绝访问
            使用cookies中的access_token向redis中查詢jwt_token, 如果查不到就代表已失效
     */
    @Override
    public Object run() throws ZuulException {
        //取得context对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        //取得request对象
        HttpServletRequest request = requestContext.getRequest();
        //使用loginService查询cookies中的用户身份令牌
        String access_token=loginService.getTokenFromCookies(request);
        //使用loginService查询header中的jwt令牌
        String jwt_token=loginService.getJwtFromHeader(request);
        //使用loginService查询Redis令牌的有效期
        long expire=loginService.getExpire(access_token);

        if (StringUtils.isEmpty(access_token) ||
            StringUtils.isEmpty(jwt_token) ||
            expire<=0){
            this.access_denied();
        }

        return null;
    }
    //拒絕訪問時
    private void access_denied(){
        RequestContext context = RequestContext.getCurrentContext();
        context.setSendZuulResponse(false); //拒绝访问, 不再進行返回資源的動作
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String responseBodyString= JSON.toJSONString(responseResult);
        //把結果以json格式寫入到上下文對象的response中
        context.setResponseBody(responseBodyString);
        context.setResponseStatusCode(200);
        HttpServletResponse response = context.getResponse();
        response.setContentType("application/json;charset=utf-8");
    }
}
