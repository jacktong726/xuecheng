package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
    //在cms_page集中上创建页面名称、站点Id、页面webpath为唯一索引
    //使用這個方法檢查page是否已存在
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String PageWebPath);
}
