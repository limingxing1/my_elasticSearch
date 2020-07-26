package mongodb.api.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * 描述:图书实体类
 *
 * @author zhengql
 * @date 2018/8/9 10:28
 */
@Data
public class Book {
    @Id
    private String id;
    //价格
    private int price;
    //书名
    private String name;
    //简介
    private String info;
    //出版社
    private String publish;
    //创建时间
    private Date createTime;
    //修改时间
    private Date updateTime;
}