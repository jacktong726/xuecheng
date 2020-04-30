package com.xuecheng.manage_cms.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CmsPageService {
    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    //用於請求靜態頁面數據模型
    @Autowired
    RestTemplate restTemplate;

    //用請請求靜態頁面模版
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    /**
     * 分頁查詢
     * QueryResponseResult是自定義對象, 包裝了QueryResult和ResultCode(也是自定義對象)
     * 其中queryResult包裝了結果集和記錄條數
     *
     * @QueryPageRequest 封裝的查詢條件如下：
     * 站点Id：精确匹配
     * 模板Id：精确匹配
     * 页面别名：模糊匹配
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        //防止調用queryPageRequest發生空指針
        if (queryPageRequest == null) {
            queryPageRequest = new QueryPageRequest();
        }

        //先判斷page和size參數是否合法(>0), 如不合法則設為默認值
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 10;
        }

        //創建一個條件的example
        //首先使用queryPageRequest帶有的屬性, 新建一個CmsPage對象
        CmsPage cmsPage = new CmsPage();
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {  //StringUtils是apache的工具包
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //再創建一個ExampleMatcher, 如果是完全匹配, 寫ExampleMatcher.matching()就夠了
        //如果有模糊匹配, 就要加上.withMatcher("對應的屬性",ExampleMatcher.GenericPropertyMatchers.contains());
        //除了contain外, 還有endsWith(),startsWith()等其他方法
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //最後使用Example.of(cmsPage,matcher)構建出example對象
        Example<CmsPage> example = Example.of(cmsPage, matcher);

        //調用dao
        //.findAll(Example,Pageable)
        //Page<CmsPage> all = cmsPageRepository.findAll(PageRequest.of(page - 1, size));
        Page<CmsPage> all = cmsPageRepository.findAll(example, PageRequest.of(page - 1, size));
        //建構QueryResult對象
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        //建構及返回QueryResponseResult對象
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);

    }

    public ResponseResult add(CmsPage cmsPage) {
        //未統一管理exception前
/*        if (cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(
                cmsPage.getPageName(),cmsPage.getSiteId(),cmsPage.getPageWebPath()
        )==null){
            cmsPageRepository.save(cmsPage);
            return new ResponseResult(CommonCode.SUCCESS);
        }else {
            return new ResponseResult(CommonCode.FAIL);
        }
*/
        //統一管理exception後
        //如果資料庫中已存在同一cmspage
        if (cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(
                cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath()
        ) != null) {
            throw new CustomException(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        cmsPageRepository.save(cmsPage);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CmsPageResult findById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            CmsPage cmsPage = cmsPageRepository.findById(id).get();
            return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
        } else {
            return new CmsPageResult(CommonCode.FAIL, null);
        }

    }

    public ResponseResult update(String id, CmsPage cmsPage) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            cmsPage.setPageId(id);
            cmsPageRepository.save(cmsPage);
            return new ResponseResult(CommonCode.SUCCESS);
        } else {
            return new ResponseResult(CommonCode.FAIL);
        }
    }

    public ResponseResult delete(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        } else {
            return new ResponseResult(CommonCode.FAIL);
        }
    }

    //通過cmsPage id, 輸出靜態化頁面
    public String getPageHtml(String id) {
        //取得數據模型
        Map model = getModelByPageId(id);
        if (model==null){
            throw new CustomException(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //取得頁面模版
        String template = getTemplateByPageId(id);
        if (template==null){
            throw new CustomException(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //返回靜態化html
        String html = generateHtml(template, model);
        if (html==null){
            throw new CustomException((CmsCode.CMS_GENERATEHTML_HTMLISNULL));
        }
        return html;
    }

    //页面静态化
    public String generateHtml(String inputTemplate, Map model) {
        Configuration configuration=new Configuration(Configuration.getVersion());
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",inputTemplate);
        configuration.setTemplateLoader(stringTemplateLoader);
        try {
            //得到模板
            Template template = configuration.getTemplate("template","utf-8");
            //輸出
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    //获取页面模板
    public String getTemplateByPageId(String pageId) {
        Optional<CmsPage> cmsPage = cmsPageRepository.findById(pageId);
        if (cmsPage.isPresent()){
            String templateId = cmsPage.get().getTemplateId();
            if (StringUtils.isEmpty(templateId)){
                throw new CustomException(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
            }
            Optional<CmsTemplate> cmsTemplate = cmsTemplateRepository.findById(templateId);
            if (cmsTemplate.isPresent()){
                String templateFileId = cmsTemplate.get().getTemplateFileId();
                //由mongodb中取得html template文件的具體流程說明, 見TestGridFs類的內容
                GridFSFile gridFsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
                GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFsFile.getObjectId());
                GridFsResource gridFsResource = new GridFsResource(gridFsFile, gridFSDownloadStream);
                try {
                   return IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //获取页面模型数据
    public Map getModelByPageId(String pageId) {
        Optional<CmsPage> cmsPage = cmsPageRepository.findById(pageId);
        if (cmsPage.isPresent()){
            String dataUrl = cmsPage.get().getDataUrl();
            if (StringUtils.isEmpty(dataUrl)){
                throw new CustomException(CmsCode.CMS_GENERATEHTML_DATAISNULL);
            }
            ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
            return forEntity.getBody();
        }
        return null;
    }
}
