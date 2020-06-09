package com.xuecheng.test.client;


import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient("XC-SERVICE-MANAGE-CMS")
public interface CmsClient {

    @GetMapping("/cms/page/{id}")
    public Map findByIdddd(@PathVariable("id") String id);
}
