package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.framework.domain.ucenter.response.UcenterCode;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UcenterService {

    @Autowired
    XcUserRepository xcUserRepository;

    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;

    public XcUserExt findXcUserExtByUsername(String username){
        if (StringUtils.isEmpty(username)){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        XcUser xcUser = xcUserRepository.findByUsername(username);
        if (xcUser==null){
            return null;
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        String userId = xcUser.getId();
        Optional<XcCompanyUser> optional = xcCompanyUserRepository.findById(userId);
        if (optional.isPresent()){
            XcCompanyUser xcCompanyUser = optional.get();
            xcUserExt.setCompanyId(xcCompanyUser.getCompanyId());
        }
        return xcUserExt;
    }

}
