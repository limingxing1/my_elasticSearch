package es.jd.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import es.jd.pojo.GoodsInfo;
import es.jd.utils.JDSourceHTML;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
public class SearchController {
    @Autowired
    JDSourceHTML jdSourceHTML;
    @Autowired
    // @Qualifier对应spring中bean方法名/类名
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    @GetMapping("/save")
    @ResponseBody
    public boolean save(String keywords) {
        List<GoodsInfo> goodsInfos = new ArrayList<>();
        try {
            goodsInfos = jdSourceHTML.parseGoods(keywords);
        } catch (IOException e) {
            log.error("search goods error.", e);
            return false;
        }
        // 将商品信息存入es库中
        return !goodsInfos.isEmpty() && addDocument(goodsInfos);
    }

    /**
     * 通过关键字进行搜索
     *
     * @param keyword 关键字
     * @return 结果
     */
    @GetMapping("/search/{keyword}/{currentPage}/{pageSize}")
    @ResponseBody
    public List<Map<String, Object>> search(@PathVariable String keyword, @PathVariable int pageSize, @PathVariable int currentPage) throws IOException {
        return highLightSearchBykeywords(keyword, currentPage, pageSize);
    }

    /**
     * 批量添加文档
     *0
     * @return 是否添加成功
     */
    private boolean addDocument(List<GoodsInfo> goodsInfos) {
        BulkRequest bulkRequest = new BulkRequest()
                .timeout("2s");

        for (GoodsInfo user : goodsInfos) {
            bulkRequest.add(new IndexRequest("goods").source(JSON.toJSONString(user), XContentType.JSON));
        }

        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("save goods Exception", e);
            return false;
        }
        //是否失败,false表示成功
        return bulkResponse.hasFailures();
    }

    /**
     * 批量添加文档——OTHER
     *
     * @return 是否添加成功
     */
    private List<Map<String, Object>> searchBykeywords(String keywords, int currentPage, int pageSize) {
        if (currentPage <= 0) {
            currentPage = 1;
        }

        //条件搜索
        SearchRequest searchRequest = new SearchRequest("goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.from(currentPage);
        sourceBuilder.size(pageSize);
//        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title",keywords);
//        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title",keywords);
//        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title",keywords);
        MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(keywords, "title");
        sourceBuilder.query(queryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("search goods error.", e);
            return null;
        }

        // 将MAP转为对象
        List<Map<String, Object>> goodsInfos = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits()) {
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            System.out.println(sourceAsMap);
            goodsInfos.add(sourceAsMap);
        }
        return goodsInfos;
    }
    /**
     * 批量添加文档——OTHER
     *
     * @return 是否添加成功
     */
    private List<Map<String, Object>> highLightSearchBykeywords(String keywords, int currentPage, int pageSize) throws IOException {
        if(currentPage<1){
            currentPage=1;
        }
        SearchRequest searchRequest = new SearchRequest("goods");
        //设置搜索
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置高亮的条件
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        //搜索的条件-设置s高亮
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.from(currentPage);
        searchSourceBuilder.size(pageSize);
        //设置搜索的类型
        TermQueryBuilder queryBuilder = QueryBuilders.termQuery("title", keywords);
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //将构建的条件加入到请求中
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        //设置数据存储集合
        List<Map<String, Object>> mapList=new ArrayList<>();
        Arrays.stream(searchResponse.getHits().getHits()).forEach(
                (s)->{
                    Map<String, HighlightField> highlightFields = s.getHighlightFields();
                    //得到高亮字段的对象
                    HighlightField name = highlightFields.get("title");
                    Map<String, Object> sourceAsMap = s.getSourceAsMap();
                    if(Objects.nonNull(name)){
                        Text[] fragments = name.fragments();
                        String title="";
                        for (Text fragment : fragments) {
                            title+=fragment;
                        }
                        sourceAsMap.put("title",title);
                    }
                    mapList.add(sourceAsMap);
                }
        );
        return mapList;
    }
}

