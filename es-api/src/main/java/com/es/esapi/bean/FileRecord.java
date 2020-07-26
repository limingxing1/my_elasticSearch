package com.es.esapi.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/**
 * 文件记录
 */
@Data
public class FileRecord {
    // id 主键-唯一标识
    private String id;
    // 文件名称
    private String fileName;
    // 文件类型
    private String fileType;
    // 文件描述
    private String fileDesc;
    // 上传时间
    private long uploadTime;
    // 领域
    private String domain;
    // 局点ID
    private String siteId;
    // 局点名称
    private String siteName;
    // s3环境桶信息
    private String s3Filebucket;
    // true 表示 已迁移到S3环境上，false表示未迁移到S3环境
    private boolean transfer;
    // 文件路径
    private String filePath;
    // 上传客户端
    private String uploadClient;
}
