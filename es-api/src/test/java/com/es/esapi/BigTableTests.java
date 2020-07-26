package com.es.esapi;

import com.alibaba.fastjson.JSON;
import com.es.esapi.bean.*;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class BigTableTests {
    private static final Object OBJECT_SYNC = new Object();
    @Autowired
    // @Qualifier对应spring中bean方法名/类名
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    /**
     * 创建索引(这里为了测试压力，真实使用时，可以添加多个索引)，通过文件类型区分
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
     * 批量插入数据(插入一千万条数据)
     */
    @Test
    void BulkRequest2() throws IOException {
        List<FileRecord> records = new ArrayList<FileRecord>();
        // 100万数据入库一次
        for (int i = 0; i < 150000; i++) {
            buildFileRecord(records);
        }
        BulkRequest bulkRequest = new BulkRequest()
                .timeout("30m");

        for (FileRecord record1 : records) {
            bulkRequest.add(new IndexRequest("fileupload")
                    .source(JSON.toJSONString(record1), XContentType.JSON));
        }

        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        //是否失败,false表示成功
        System.out.println(bulkResponse.hasFailures());
        System.out.println(bulkResponse.status());
    }
    /**
     * 批量插入数据(插入一千万条数据)
     */
    @Test
    void CountRequest1() throws IOException {



//		SearchResponse response = searchRequestBuilder.execute().actionGet();
//		SearchHits searchHits = response.getHits();
//		System.out.println(response.getHits().getTotalHits());
	}

	//高亮-查询
	@Test
	void search() throws IOException {
		SearchRequest request = new SearchRequest("goods");

		//构建搜索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title","java");
//		MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
		sourceBuilder.query(termQueryBuilder)
				.timeout(new TimeValue(5, TimeUnit.SECONDS));

		request.source(sourceBuilder);

		SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits(), true));
		for (SearchHit documentFields : searchResponse.getHits()) {
			System.out.println(documentFields.getSourceAsMap());
		}
	}

	//高亮-查询
	@Test
	void search2() throws IOException {
		//条件搜索
		SearchRequest searchRequest = new SearchRequest("fileupload");
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		sourceBuilder.from(1);
		sourceBuilder.size(10);
//		MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(,"siteId");
		//设置搜索的类型
		TermQueryBuilder queryBuilder = QueryBuilders.termQuery("siteId", "siteId___c693c977-83a9-401f-86ae-63fea8649fa1");
		sourceBuilder.query(queryBuilder);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

		//执行搜索
		searchRequest.source(sourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(searchResponse);
	}


	private void buildFileRecord(List<FileRecord> records) {
		FileRecord record = new FileRecord();
		String uuid = UUID.randomUUID().toString();
		record.setId(uuid);
		record.setDomain(buildDomain());
		record.setFileDesc("desc___" + UUID.randomUUID().toString());
		record.setFileName("FileName" + uuid);
		record.setFileType(buildFileType());
		record.setFilePath(buildFilePath());
		record.setS3Filebucket("eservice");
		record.setSiteId("siteId___" + uuid);
		record.setSiteName("siteName___" + uuid);
		record.setTransfer(buildTransfer());
		record.setUploadClient(buildUploadClient());
		record.setUploadTime(buildUploadTime());
		records.add(record);
	}

	private String buildFileType() {
		Random random = new Random();
		FileType value = FileType.values()[random.nextInt(FileType.values().length)];
		return value.toString();
	}

	private long buildUploadTime() {
		long time = new Date().getTime();
		Random random = new Random();
		return time - random.nextInt(10000);
	}

	private String buildUploadClient() {
		Random random = new Random();
		UploadClient value = UploadClient.values()[random.nextInt(UploadClient.values().length)];
		return value.toString();
	}

	private String buildDomain() {
		Random random = new Random();
		Domain value = Domain.values()[random.nextInt(Domain.values().length)];
		return value.toString();
	}

	private boolean buildTransfer() {
		Random random = new Random();
		int randomInt = random.nextInt(100);
		return randomInt % 2 == 0;
	}

	private String buildFilePath() {
		Random random = new Random();
		FileSuffix value = FileSuffix.values()[random.nextInt(FileSuffix.values().length)];
		return new StringBuilder('/').append(buildDomain()).append('/')
				.append(buildFileType())
				.append("文件测试.")
				.append(UUID.randomUUID())
				.append('.')
				.append(value.toString()).toString();
	}
}
