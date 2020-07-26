package com.es.esapi;

import com.alibaba.fastjson.JSON;
import com.es.esapi.bean.GoodsInfo;
import com.es.esapi.bean.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsApiApplicationTests {
	@Autowired
    // @Qualifier对应spring中bean方法名/类名
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient client;

	/**
	 * 创建索引
	 */
	@Test
	void createIndex() throws IOException {
		// 1.创建索引请求
		CreateIndexRequest request = new CreateIndexRequest("fileupload");
		// 2.客户端执行请求
		CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
		System.out.println(response);
	}
	/**
	 * 获取索引
	 */
	@Test
	void GetIndex() throws IOException {
		// 1.构建请求
		GetIndexRequest request = new GetIndexRequest("fileUpload");
		// 2.客户端执行请求
		boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
		// 是否存在索引
		if (exists) {
			GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
			System.out.println(response);
		}
	}
	/**
	 * 删除索引
	 */
	@Test
	void DeleteIndex() throws IOException {
		// 1.构建请求
		DeleteIndexRequest request = new DeleteIndexRequest("fileupload");
		// 2.客户端执行请求
		AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
		System.out.println(response);
	}
	/**
	 * 添加文档
	 */
	@Test
	void AddDocument() throws IOException {
		GoodsInfo goodsInfo = new GoodsInfo("/ouuu/ouuusff.jpg","oiuus12fsdfasdf","$89");

		IndexRequest request = new IndexRequest("goods")
				.id("1")
				.timeout(TimeValue.timeValueSeconds(1));

		request.source(JSON.toJSONString(goodsInfo), XContentType.JSON);

		IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

		System.out.println(indexResponse.toString());
		System.out.println(indexResponse.status());
	}
	/**
	 * 判断文档是否存在
	 */
	@Test
	void isExistDocument() throws IOException {
		GetRequest request = new GetRequest("index1", "1");
		//不获取_source内容,提升效率
		request.fetchSourceContext(new FetchSourceContext(false));
		request.storedFields("_none_");

		boolean exists = client.exists(request, RequestOptions.DEFAULT);
		System.out.println(exists);
	}

	/**
	 * 删除文档
	 * @throws IOException
	 */
	@Test
	void deleteDocument() throws IOException {
		DeleteRequest request = new DeleteRequest("goods", "1");
		request.timeout("1s");
		DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);

		System.out.println(deleteResponse.status());
	}

	/**
	 * 更新文档
	 * @throws IOException
	 */
	@Test
	void updateDocument() throws IOException {
		UpdateRequest request = new UpdateRequest("index1", "1");
		request.timeout("1s");

		User user = new User();
		user.setAge(20);
		user.setName("uuuu");
		request.doc(JSON.toJSONString(user), XContentType.JSON);
		UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);

		System.out.println(updateResponse.status());
	}
	/**
	 * 批量插入数据
	 */
	@Test
	void BulkRequest() throws IOException {

		BulkRequest bulkRequest = new BulkRequest()
				.timeout("5s");
		List<User> users = Arrays.asList(new User("dai1", 1), new User("dai2", 2), new User("dai3", 3));

		for (User user : users) {
			bulkRequest.add(new IndexRequest("index1")
					//.id("xxx")
					.source(JSON.toJSONString(user), XContentType.JSON));
		}

		BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		//是否失败,false表示成功
		System.out.println(bulkResponse.hasFailures());
		System.out.println(bulkResponse.status());
	}
	//高亮-查询
	@Test
	void search() throws IOException {
		SearchRequest request = new SearchRequest("fileupload");

		//构建搜索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("id","1be053b6-07d3-42a7-8368-0bb5410bf612");
//		MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
		sourceBuilder.query(termQueryBuilder)
				.timeout(new TimeValue(5, TimeUnit.SECONDS));

		request.source(sourceBuilder);

		SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits(), true));
		System.out.println("===================================");
		for (SearchHit documentFields : searchResponse.getHits()) {
			System.out.println(documentFields.getSourceAsMap());
		}

	}

}
