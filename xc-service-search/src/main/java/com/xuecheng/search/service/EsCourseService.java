package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EsCourseService {

    @Autowired
    RestHighLevelClient client;

    @Value("${xuecheng.elasticsearch.course.index}")
    private String es_index;
    @Value("${xuecheng.elasticsearch.course.type}")
    private String es_type;
    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String source_field;

    public QueryResponseResult list(int page, int size, CourseSearchParam courseSearchParam) {
        SearchRequest searchRequest = new SearchRequest(es_index);
        searchRequest.types(es_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(courseSearchParam);

        searchSourceBuilder.query(boolQueryBuilder);
        String[] source_fields = source_field.split(",");
        searchSourceBuilder.fetchSource(source_fields,new String[]{});
        //分頁
        if (page<=0){page=1;}
        if (size<=0){size=4;}
        searchSourceBuilder.from((page-1)*size);//第幾個開始
        searchSourceBuilder.size(size);//每頁幾個

        //高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");//设置前缀
        highlightBuilder.postTags("</font>");//设置后缀
        // 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

        Map searchResultMap = getSearchResult(searchRequest);
        List<CoursePub> list = (List<CoursePub>) searchResultMap.get("list");
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        queryResult.setTotal((long)searchResultMap.get("total"));
        queryResult.setList(list);
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    public BoolQueryBuilder getBoolQueryBuilder(CourseSearchParam courseSearchParam){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        String keyword = courseSearchParam.getKeyword();
        if (StringUtils.isNotEmpty(keyword)){
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery
                    (keyword, "name", "teachplan", "description")
                    .minimumShouldMatch("70%")
                    .field("name",10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        String mt = courseSearchParam.getMt();
        if (StringUtils.isNotEmpty(mt)){
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",mt));
        }
        String st = courseSearchParam.getSt();
        if (StringUtils.isNotEmpty(st)){
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",st));
        }
        String grade = courseSearchParam.getGrade();
        if (StringUtils.isNotEmpty(grade)){
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",grade));
        }
        Float price_min = courseSearchParam.getPrice_min();
        if (price_min!=null){
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(price_min));
        }
        Float price_max = courseSearchParam.getPrice_max();
        if (price_max!=null){
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(price_max));
        }
        return boolQueryBuilder;
    }


    public Map getSearchResult(SearchRequest searchRequest){
        SearchResponse searchResponse = null;
        Map<String,Object> map=new HashMap();
        List<CoursePub> list=new ArrayList<>();
        try {
            searchResponse = client.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            long totalHits = hits.getTotalHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                CoursePub coursePub = new CoursePub();
                //取出source
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出id
                coursePub.setId((String) sourceAsMap.get("id"));
                //取出名称
                String name = (String) sourceAsMap.get("name");
                Map<String, HighlightField> highlightFields = hit.getHighlightFields(); //取出高亮字段内容
                HighlightField nameField = highlightFields.get("name");
                if (nameField != null) {
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text str : fragments) {
                        stringBuffer.append(str.string());
                    }
                    name = stringBuffer.toString();
                }
                coursePub.setName(name);
                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格(new+old)
                if(sourceAsMap.get("price")!=null ){
                    double price = (double) sourceAsMap.get("price");
                    coursePub.setPrice((float)price);
                }
                if(sourceAsMap.get("price_old")!=null ){
                    double price_old = (double) sourceAsMap.get("price_old");
                    coursePub.setPrice_old((float)price_old);
                }
                list.add(coursePub);
            }
            map.put("list",list);
            map.put("total",totalHits);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
