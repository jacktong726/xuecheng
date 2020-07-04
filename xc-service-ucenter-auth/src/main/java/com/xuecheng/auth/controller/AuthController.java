package com.xuecheng.auth.controller;

import com.alibaba.fastjson.JSON;
import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthController implements AuthControllerApi {

    @Autowired
    AuthService authService;

    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;    //cookie的作用域
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;       //cookie的保存時間, 這裏設定為session期間保存


    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        //校验账号是否输入
        String username = loginRequest.getUsername();
        if (StringUtils.isEmpty(username)){
            throw new CustomException(AuthCode.AUTH_USERNAME_NONE);
        }
        //校验密码是否输入
        String password = loginRequest.getPassword();
        if (StringUtils.isEmpty(password)){
            throw  new CustomException(AuthCode.AUTH_PASSWORD_NONE);
        }
        //請求authService, 傳入用戶名 密碼 clientId clientSecret, 返回AuthToken類
        AuthToken authToken=authService.login(username,password,clientId,clientSecret);
        //将authToken中的access_token写入cookie
        String access_token = authToken.getAccess_token();
        this.writeCookie(access_token);
        //返回LoginResult
        return new LoginResult(CommonCode.SUCCESS, access_token);
    }

    //将authToken中的access_token写入cookie
    //注意, 這個cookie的域是在xuecheng.com中,所以用postman訪問localhost:40400/auth/userlogin不能寫入cookie
    //必須訪問ucenter.xuecheng.com/openapi/auth/userlogin才能寫入cookie(另外使用nginx設定代理, 引導回localhost:40400)
    private void writeCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
        /**@HttpServletResponse response,
         * @String domain,
         * @String path,
         * @String name,
         * @String value,
         * @int maxAge,
         * @boolean httpOnly false表示允许浏览器获取
         * */
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token,cookieMaxAge,false);
    }

    //由cookies中取得login時存入的uid(即jti), 由service向redis請求token, 返回到前端
    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt() {
        String uid = this.getUidFromCookies();
        AuthToken authToken=authService.getUserJwt(uid);
        if (authToken==null){
            return new JwtResult(CommonCode.FAIL,null);
        }
        String jsonString = JSON.toJSONString(authToken);
        return new JwtResult(CommonCode.SUCCESS,jsonString);
    }

    //用戶logout的話, 會清空cookies中的uid和刪除redia中的user_token
    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        String uid = this.getUidFromCookies();
        authService.delToken(uid);
        this.removeUidFromCookies();
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private String getUidFromCookies(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if (map==null || map.get("uid")==null){
            return null;
        }
        return map.get("uid");
    }

    private void removeUidFromCookies(){
        HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/","uid",null,0,false);
    }
}
