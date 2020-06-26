package com.xuecheng.search.controller;

import com.xuecheng.api.search.EsCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.EsCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search/course")
public class EsCourseController implements EsCourseControllerApi {
    @Autowired
    EsCourseService esCourseService;

    @Override
    @GetMapping(value="/list/{page}/{size}")
    public QueryResponseResult list(@PathVariable("page") int page,
                                    @PathVariable("size") int size,
                                    CourseSearchParam courseSearchParam) {
        return esCourseService.list(page,size,courseSearchParam);
    }

    @Override
    @GetMapping("/getall/{id}")
    public Map<String, CoursePub> getall(@PathVariable("id") String courseId) {
        return esCourseService.getall(courseId);
    }

    @Override
    @GetMapping(value="/getmedia/{teachplanId}")
    public TeachplanMediaPub getmedia(@PathVariable("teachplanId") String teachplanId) {
        String[] teachplanIds = new String[]{teachplanId};
        //為了程序的可擴展性, service的搜索功能設計成可以傳入teachplanId數組, 返回queryResponseResult<TeachplanMediaPud>
        QueryResponseResult queryResponseResult=esCourseService.getmedia(teachplanIds);
        QueryResult<TeachplanMediaPub> queryResult = queryResponseResult.getQueryResult();
        if (queryResult==null || queryResult.getList().size()<1){
            return new TeachplanMediaPub();
        }
        return queryResult.getList().get(0);
    }
}
