package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDoc {
    @Autowired
    RestHighLevelClient client; //一般用高級的這個client, 不成才用下面那個

    @Autowired
    RestClient restClient;

    //增加文檔
    @Test
    public void testAdd() throws IOException {
        //准备json数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jsonMap.put("timestamp", dateFormat.format(new Date()));
        jsonMap.put("price", 5.6f);
        //索引请求对象
        IndexRequest indexRequest = new IndexRequest("xc_course", "doc");
        //指定索引文档内容
        indexRequest.source(jsonMap);
        //索引响应对象
        IndexResponse indexResponse = client.index(indexRequest);
        //获取响应结果
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println(result);

    }

    //更新文档(局部)
    @Test
    public void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("xc_course", "doc", "N4ypuHIB-KTyZoAZL_7G");
        Map<String, String> map = new HashMap<>();
        map.put("name", "spring cloud实战2");
        map.put("pic", "group1/M00/00/00/wKhlQFs6RCeAY0pHAAJx5ZjNDEM428.jpg");
        updateRequest.doc(map);
        UpdateResponse update = client.update(updateRequest);
        RestStatus status = update.status();
        System.out.println(status);
    }

    //根据id删除文档
    @Test
    public void testDelDoc() throws IOException {
        //删除文档id
        String id = "N4ypuHIB-KTyZoAZL_7G";
        //删除索引请求对象
        DeleteRequest deleteRequest = new DeleteRequest("xc_course", "doc", id);
        //响应对象
        DeleteResponse deleteResponse = client.delete(deleteRequest);
        //获取响应结果
        DocWriteResponse.Result result = deleteResponse.getResult();
        System.out.println(result);
    }

    //根据id查询文档
    @Test
    public void getDoc() throws IOException {
        GetRequest getRequest = new GetRequest(
                "xc_course",
                "doc",
                "1");
        GetResponse getResponse = client.get(getRequest);
        boolean exists = getResponse.isExists();
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    public SearchRequest getSearchRequest() {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        return searchRequest;
    }

    public void showSearchResult(SearchRequest searchRequest) throws IOException, ParseException {
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            double price = (double) sourceAsMap.get("price");
            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(score + "---" + id + "---" + name + "---" + price + "---" + timestamp + "---" + description);
        }
    }

    /**
     * search原理:
     * QueryBuilder  ->  SearchSourceBuilder  -->  SearchRequest  -->  client
     * 傳入                     傳入                傳入
     * 由於以下幾個方法的頭尾都一樣, 只有SearchSourceBuilder的部份不一樣, 所以抽出來寫到上面, 簡潔一些
     */

    //搜索全部记录
    @Test
    public void testSearchAll() throws IOException, ParseException {
        SearchRequest searchRequest = this.getSearchRequest();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());   //加入查詢條件-->matchAllQuery() 全部
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{}); //要返回什麼欄位
        searchRequest.source(searchSourceBuilder);

        this.showSearchResult(searchRequest);

    }

    //分頁搜索
    @Test
    public void testSearchByPage() throws IOException, ParseException {
        SearchRequest searchRequest = this.getSearchRequest();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());   //加入查詢條件-->matchAllQuery() 全部
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{}); //要返回什麼欄位
        searchSourceBuilder.from(0);//第幾頁
        searchSourceBuilder.size(2);//每頁幾個
        searchRequest.source(searchSourceBuilder);

        this.showSearchResult(searchRequest);

    }

    //根據ids搜索(精確匹配)
    @Test
    public void testSearchByIds() throws IOException, ParseException {
        SearchRequest searchRequest = this.getSearchRequest();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String[] ids = {"2", "3"};
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id", ids));     //注意是termsQuery, 有s
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{}); //要返回什麼欄位
        searchRequest.source(searchSourceBuilder);

        this.showSearchResult(searchRequest);

    }

    //term條件搜索(不會把term分詞)
    @Test
    public void testSearchByTerm() throws IOException, ParseException {
        SearchRequest searchRequest = this.getSearchRequest();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("name", "spring"));
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{}); //要返回什麼欄位
        searchRequest.source(searchSourceBuilder);

        this.showSearchResult(searchRequest);
    }

    //match條件搜索(match內容會分詞)
    @Test
    public void testSearchByMatch() throws IOException, ParseException {
        SearchRequest searchRequest = this.getSearchRequest();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //只要含有spring、开发、框架其中一個關鍵字就會顯示
        //searchSourceBuilder.query(QueryBuilders.matchQuery("description","spring开发框架").operator(Operator.OR));
        //设置"minimum_should_match": "80%"表示，三个词在文档的匹配占比为80%，即3*0.8=2.4，向上取整得2，表示至少有两个词在文档中要匹配成功。
        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发框架").minimumShouldMatch("80%"));
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{}); //要返回什麼欄位
        searchRequest.source(searchSourceBuilder);

        this.showSearchResult(searchRequest);
    }

    //multi match條件搜索(match內容會分詞)
    @Test
    public void testSearchByMultiMatch() throws IOException, ParseException {
        SearchRequest searchRequest = this.getSearchRequest();
        //多個欄位查詢
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架", "name", "description");
        multiMatchQueryBuilder.minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name", 10);    //把name欄位的权重提升10倍

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(multiMatchQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{}); //要返回什麼欄位
        searchRequest.source(searchSourceBuilder);

        this.showSearchResult(searchRequest);
    }

    //bool搜索(把多個QueryBuilders結合)
    @Test
    public void testSearchByBool() throws IOException, ParseException {
        SearchRequest searchRequest = this.getSearchRequest();
        //第一個QueryBuilder
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring开发", "name", "description");
        multiMatchQueryBuilder.minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name", 10);    //把name欄位的权重提升10倍
        //第二個queryBuilder
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");
        //結合(兩個條件都要滿足)
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{}); //要返回什麼欄位
        searchRequest.source(searchSourceBuilder);

        this.showSearchResult(searchRequest);
    }

    //bool+filter搜索(把多個QueryBuilders結合)
    //filter比queryBuilder性能好, 可以代替上面的例子
    @Test
    public void testSearchByBoolFilter() throws IOException, ParseException {
        SearchRequest searchRequest = this.getSearchRequest();
        //建一個QueryBuilder
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring开发", "name", "description");
        multiMatchQueryBuilder.minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name", 10);    //把name欄位的权重提升10倍
        //加入到bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //加入過濾條件
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{}); //要返回什麼欄位
        searchRequest.source(searchSourceBuilder);

        this.showSearchResult(searchRequest);
    }

    //加入sort
    @Test
    public void testSearchSort() throws IOException, ParseException {
        SearchRequest searchRequest = this.getSearchRequest();
        //建一個boolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //加入過濾條件
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));   //注意sort是在searchSourceBuilder中加的, 不是queryBuilder
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{}); //要返回什麼欄位
        searchRequest.source(searchSourceBuilder);

        this.showSearchResult(searchRequest);
    }

    //高亮显示
    @Test
    public void testHighlight() throws IOException {
        SearchRequest searchRequest = this.getSearchRequest();
        //建一個boolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //建一個QueryBuilder
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发框架", "name", "description");
        multiMatchQueryBuilder.minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name", 10);    //把name欄位的权重提升10倍
        //加入到bool
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //加入過濾條件
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));   //注意sort是在searchSourceBuilder中加的, 不是queryBuilder
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{}); //要返回什麼欄位

        //高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");//设置前缀
        highlightBuilder.postTags("</tag>");//设置后缀
        // 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name"); //名称
            String description = (String) sourceAsMap.get("description");   //description
            //取出高亮字段内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                //名称
                HighlightField nameField = highlightFields.get("name");
                if (nameField != null) {
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text str : fragments) {
                        stringBuffer.append(str.string());
                    }
                    name = stringBuffer.toString();
                }
                //description
                HighlightField descriptionField = highlightFields.get("description");
                if (descriptionField != null) {
                    Text[] fragments = descriptionField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text str : fragments) {
                        stringBuffer.append(str.string());
                    }
                    description = stringBuffer.toString();
                }
            }
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            String studymodel = (String) sourceAsMap.get("studymodel");
            //String description = (String) sourceAsMap.get("description");
            System.out.println(name+"---"+studymodel+"---"+description);
        }
    }
}
