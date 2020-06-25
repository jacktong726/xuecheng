package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.BeanUtilsExt;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TeachPlanMapper teachPlanMapper;

    @Autowired
    TeachPlanRepository teachPlanRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachPlanMediaRepository teachPlanMediaRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    //讀取配置文件中的course-publish參數
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    public TeachplanNode findTeachPlanList(String courseId) {
        return teachPlanMapper.findTeachPlanList(courseId);
    }

    @Transactional
    public ResponseResult addTeachPlan(Teachplan teachplan) {
        /**新增新課程計劃時,要注意:
         * 1. 檢查傳的入參數不能為null(teachplan、pname和courseid都不能為空)
         * 2. 傳入的teachplan中, 如父節點為空, 表示是二級節點, 要把父節點設為一級節點(所以必須先找出父節點)
         * 3. 如果teachplan的table中沒有一級節點(根節點), 必須先在table中增加一個(courseId+pname和課程id+課程name一致)*/
        //檢查參數
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getPname()) ||
                StringUtils.isEmpty(teachplan.getCourseid())) {
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        //檢查是否有傳入上級節點, 如沒有則寫入一個
        if (StringUtils.isEmpty(teachplan.getParentid())){
            Teachplan rootTeachPlan = findRootTeachPlan(teachplan.getCourseid());
            teachplan.setParentid(rootTeachPlan.getId());
        }
        //按上級節點的grade設定本課程計划的grade
        String grade = teachPlanRepository.findById(teachplan.getParentid()).get().getGrade();
        if ("1".equals(grade)){
            teachplan.setGrade("2");
        }else{
            teachplan.setGrade("3");
        }
        teachPlanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //在teachPlan表中按courseId找出一級(根)課程計划節點
    public Teachplan findRootTeachPlan(String courseId){
        if (StringUtils.isEmpty(courseId)){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        //dao按courseId查teachPlan表中的一級節點
        List<Teachplan> list = teachPlanRepository.findByCourseidAndParentid(courseId, "0");
        if (list.size()<=0 || list==null){  //如果找不到一級(根)節點, 就新增一個並返回
            Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
            if (!optional.isPresent()){
                throw new CustomException(CommonCode.INVALIDPARAM);
            }
            CourseBase courseBase = optional.get();
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setParentid("0");
            teachplan.setPname(courseBase.getName());
            teachplan.setGrade("1");
            teachplan.setStatus("0");
            teachPlanRepository.save(teachplan);
            return teachplan;
        }
        //如果找到一級(根)節點,就返回
        Teachplan teachplan = list.get(0);
        return teachplan;
    }

    public QueryResponseResult findCoursePage(int page, int size,CourseListRequest courseListRequest){
        PageHelper.startPage(page,size);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(null);
        QueryResult queryResult = new QueryResult<CourseInfo>();
        queryResult.setList(courseListPage.getResult());
        queryResult.setTotal(courseListPage.getTotal());
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    @Transactional
    public ResponseResult addCourseBase(CourseBase courseBase){
        if (courseBase==null){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CourseBase findCourseBaseById(String id){
        if (StringUtils.isEmpty(id)){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        return optional.orElse(null);
    }

    @Transactional
    public ResponseResult updateCourseBase(String id,CourseBase courseBase){
        if (StringUtils.isEmpty(id)||courseBase==null){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()){
            CourseBase courseBaseOld = optional.get();
            BeanUtilsExt.copyPropertiesIgnoreNull(courseBase,courseBaseOld);
            courseBaseRepository.save(courseBaseOld);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return null;
    }

    public CourseMarket getCourseMarketById(String id){
        if (StringUtils.isEmpty(id)){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        Optional<CourseMarket> optional = courseMarketRepository.findById(id);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket){
        if (StringUtils.isEmpty(id)||courseMarket==null){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        CourseMarket courseMarketOld = getCourseMarketById(id);
        if (courseMarketOld==null){
            courseMarketRepository.save(courseMarket);
        }else{
            BeanUtilsExt.copyPropertiesIgnoreNull(courseMarket,courseMarketOld);
            courseMarketRepository.save(courseMarketOld);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic){
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()){
            CoursePic coursePicOld = optional.get();
            coursePicOld.setPic(pic);
            coursePicRepository.save(coursePicOld);
        }else{
            CoursePic coursePic = new CoursePic();
            coursePic.setCourseid(courseId);
            coursePic.setPic(pic);
            coursePicRepository.save(coursePic);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CoursePic findCoursePic(String courseId){
        if (StringUtils.isEmpty(courseId)){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult deletePicByCourseId(String courseId){
        if (StringUtils.isEmpty(courseId)){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        long result = coursePicRepository.deleteByCourseid(courseId);   //返回1表示成功
        if (result>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    public CourseView getCourseView(String id){
        if (StringUtils.isEmpty(id)){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        CourseView courseView = new CourseView();

        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()){
            courseView.setCourseBase(courseBaseOptional.get());
        }

        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if (courseMarketOptional.isPresent()){
            courseView.setCourseMarket(courseMarketOptional.get());
        }

        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if (coursePicOptional.isPresent()){
            courseView.setCoursePic(coursePicOptional.get());
        }

        TeachplanNode teachPlanList = teachPlanMapper.findTeachPlanList(id);
        if (teachPlanList!=null){
            courseView.setTeachplanNode(teachPlanList);
        }
        return courseView;
    }

    public CoursePublishResult preview(String courseId){
        CmsPage cmsPage = this.makeCmsPage(courseId);
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        //返回保存結果和頁面預覧url
        if (cmsPageResult.isSuccess()){
            String pageId = cmsPageResult.getCmsPage().getPageId();
            return new CoursePublishResult(CommonCode.SUCCESS,previewUrl+pageId);
        }
        return new CoursePublishResult(CommonCode.FAIL,null);
    }

    public CmsPage makeCmsPage(String courseId){
        //先由mysql中取得courseBase資料
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()){
            throw new CustomException(CourseCode.COURSE_GET_NOTEXISTS);
        }
        CourseBase courseBase = optional.get();
        //再使用courseBase資料構建cmsPage
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setPageName(courseId+".html");
        cmsPage.setPageAliase(courseBase.getName());
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageType("0");
        cmsPage.setPageCreateTime(new Date());
        cmsPage.setTemplateId(publish_templateId);
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);
        return cmsPage;
    }

    @Transactional
    public CoursePublishResult publish(String courseId){
        CmsPage cmsPage = this.makeCmsPage(courseId);
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()){
            throw new CustomException(CommonCode.FAIL);
        }
        this.changePublishStatus(courseId,"202002");
        //保存到couserPub表中, 以便logstash同步同elasticSearch
        //需要打用F:\logstash-6.2.1\bin, 打開cmd輸入以下命令來開啟服務端:
        //logstash.bat -f ..\config\mysql.conf
        saveCoursePub(courseId);

        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);

    }

    private void saveCoursePub(String courseId) {
        //創建coursePub pojo
        CoursePub coursePubNew=createCoursePub(courseId);
        if (coursePubNew==null){
            throw new CustomException(CourseCode.COURSE_COURSEPUBISNULL);
        }
        Optional<CoursePub> optionalCoursePub = coursePubRepository.findById(courseId);
        if (optionalCoursePub.isPresent()){
            CoursePub coursePubOld = optionalCoursePub.get();
            BeanUtils.copyProperties(coursePubNew,coursePubOld);
            coursePubRepository.save(coursePubOld);
        }
        coursePubRepository.save(coursePubNew);

    }

    private CoursePub createCoursePub(String courseId) {
        CoursePub coursePub = new CoursePub();
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(courseId);
        if (optionalCourseBase.isPresent()){
            CourseBase courseBase = optionalCourseBase.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(courseId);
        if (optionalCoursePic.isPresent()){
            CoursePic coursePic = optionalCoursePic.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(courseId);
        if (optionalCourseMarket.isPresent()){
            CourseMarket courseMarket = optionalCourseMarket.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }
        TeachplanNode teachPlanList = this.findTeachPlanList(courseId);
        String teachPlan = JSON.toJSONString(teachPlanList);
        coursePub.setTeachplan(teachPlan);

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        coursePub.setTimestamp(date);
        coursePub.setPubTime(simpleDateFormat.format(date));
        return coursePub;


    }

    public CourseBase changePublishStatus(String courseId,String status){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()){
            CourseBase courseBase = optional.get();
            courseBase.setStatus(status);
            courseBaseRepository.save(courseBase);
            return courseBase;
        }
        return null;
    }

    @Transactional
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia==null||StringUtils.isEmpty(teachplanMedia.getMediaId())){
            throw new CustomException(CommonCode.INVALIDPARAM);
        }
        String teachplanId = teachplanMedia.getTeachplanId();
        //只允許3級節點teachplan選擇視頻
        Optional<Teachplan> teachplanOptional = teachPlanRepository.findById(teachplanId);
        if (!teachplanOptional.isPresent()){
            throw new CustomException(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan teachplan = teachplanOptional.get();
        String grade = teachplan.getGrade();
        if (!"3".equals(grade)){
            throw new CustomException(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }

        Optional<TeachplanMedia> teachplanMediaOptional = teachPlanMediaRepository.findById(teachplanId);
        if (teachplanMediaOptional.isPresent()){
            TeachplanMedia teachplanMediaOld = teachplanMediaOptional.get();
            teachplanMediaOld.setMediaId(teachplanMedia.getMediaId());
            teachplanMediaOld.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
            teachplanMediaOld.setMediaUrl(teachplanMedia.getMediaUrl());
            teachPlanMediaRepository.save(teachplanMediaOld);
        }else {
            teachPlanMediaRepository.save(teachplanMedia);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
