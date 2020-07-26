package mongodb.api.service;

import mongodb.api.bean.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 描述:
 * mongo
 *
 * @author zhengql
 * @date 2018/8/9 10:24
 */
@Service
public class MongoDbService {
    private static final Logger logger = LoggerFactory.getLogger(MongoDbService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存对象
     *
     * @param book
     * @return
     */
//    public String saveObj(Book book) {
//        logger.info("--------------------->[MongoDB save start]");
//        book.setCreateTime(new Date());
//        book.setUpdateTime(new Date());
//        mongoTemplate.save(book);
//
//        return "添加成功";
//    }

    /**
     * 保存对象-文件记录
     *
     * @return
     */
    public String saveFileRecord() {
        logger.info("--------------------->[MongoDB save start]");
        List fileRecordList = new ArrayList<FileRecord>();
        for (int i = 0; i < 10000; i++) {
            buildFileRecord(fileRecordList);
        }
        mongoTemplate.insertAll(fileRecordList);

        return "添加成功";
    }

    /**
     * 查询所有
     *
     * @return
     */
    public List<FileRecord> findByPage() {
        int pageNum = 10;
        int pageSize = 10;
        List fileRecordList = new ArrayList<FileRecord>();

        Query query = new Query();
        System.out.println("数据库总数为 ::: " + mongoTemplate.count(new Query(), FileRecord.class));
        // 通过 _id 来排序
        query.with(Sort.by(Sort.Direction.ASC, "id"));

        if (pageNum != 1) {
            // number 参数是为了查上一页的最后一条数据
            int number = (pageNum - 1) * pageSize;
            query.limit(number);

            List<FileRecord> fileRecords = mongoTemplate.find(query, FileRecord.class);

            // 取出最后一条
            FileRecord fileRecord = fileRecords.get(fileRecords.size() - 1);

            // 取到上一页的最后一条数据 id，当作条件查接下来的数据
            String id = fileRecord.getId();

            // 从上一页最后一条开始查（大于不包括这一条）
            query.addCriteria(Criteria.where("id").gt(id));
        }
        // 页大小重新赋值，覆盖 number 参数
        query.limit(pageSize);
        // 即可得到第n页数据
        fileRecordList = mongoTemplate.find(query, FileRecord.class);

        return fileRecordList;
    }


    /**
     * 查询所有
     *
     * @return
     */
    public List<Book> findAll() {
        logger.info("--------------------->[MongoDB find start]");
        return mongoTemplate.findAll(Book.class);
    }


    /***
     * 根据id查询
     * @param id
     * @return
     */
    public Book getBookById(String id) {
        logger.info("--------------------->[MongoDB find start]");
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(query, Book.class);
    }

    /**
     * 根据名称查询
     *
     * @param name
     * @return
     */
    public Book getBookByName(String name) {
        logger.info("--------------------->[MongoDB find start]");
        Query query = new Query(Criteria.where("name").is(name));
        return mongoTemplate.findOne(query, Book.class);
    }

    /**
     * 更新对象
     *
     * @param book
     * @return
     */
    public String updateBook(Book book) {
        logger.info("--------------------->[MongoDB update start]");
        Query query = new Query(Criteria.where("_id").is(book.getId()));
        Update update = new Update().set("publish", book.getPublish())
                .set("info", book.getInfo())
                .set("updateTime", new Date());
        //updateFirst 更新查询返回结果集的第一条
        mongoTemplate.updateFirst(query, update, Book.class);
        //updateMulti 更新查询返回结果集的全部
//        mongoTemplate.updateMulti(query,update,Book.class);
        //upsert 更新对象不存在则去添加
//        mongoTemplate.upsert(query,update,Book.class);
        return "success";
    }

    /***
     * 删除对象
     * @param book
     * @return
     */
    public String deleteBook(Book book) {
        logger.info("--------------------->[MongoDB delete start]");
        mongoTemplate.remove(book);
        return "success";
    }

    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    public String deleteBookById(String id) {
        logger.info("--------------------->[MongoDB delete start]");
        //findOne
        Book book = getBookById(id);
        //delete
        deleteBook(book);
        return "success";
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