package com.xuecheng.govern.gateway.servcie;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.rmi.server.UID;
import java.util.Map;

@Service
public class LoginService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //查询cookies中的用户身份令牌
    public String getTokenFromCookies(javax.servlet.http.HttpServletRequest request) {
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        return map.get("uid");
    }

    //查询header中的jwt令牌
    public String getJwtFromHeader(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)||
            !authorization.startsWith("Bearer ")
        ){
            return null;
        }
        return authorization;
    }

    //查询Redis令牌的有效期
    public long getExpire(String access_token) {
        Long expire = stringRedisTemplate.getExpire("user_token:" + access_token);
        return expire;
    }
}
