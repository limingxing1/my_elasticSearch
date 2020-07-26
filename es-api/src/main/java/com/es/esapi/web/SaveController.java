package com.es.esapi.web;

import com.alibaba.fastjson.JSON;
import com.es.esapi.bean.*;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.*;


@Controller
public class SaveController {
    @Autowired
    // @Qualifier对应spring中bean方法名/类名
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    /**
     * 插入數據
     * @return
     * @throws IOException
     */
    @GetMapping("/save")
    @ResponseBody
    public boolean insertFileRecord() throws IOException {
        List<FileRecord> records = new ArrayList<FileRecord>();
        // 100万数据入库一次
        for (int i = 0; i < 10000; i++) {
            buildFileRecord(records);
        }
        BulkRequest bulkRequest = new BulkRequest()
                .timeout("600s");

        for (FileRecord record1 : records) {
            bulkRequest.add(new IndexRequest("fileupload")
                    .source(JSON.toJSONString(record1), XContentType.JSON));
        }

        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        //是否失败,false表示成功
        return bulkResponse.hasFailures();
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
