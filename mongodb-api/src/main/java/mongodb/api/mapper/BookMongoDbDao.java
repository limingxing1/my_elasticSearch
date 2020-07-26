package mongodb.api.mapper;

import mongodb.api.bean.Book;
import mongodb.api.mapper.base.MongoDbDao;
import org.springframework.stereotype.Repository;

/**
 * 描述:
 *
 * @author zhengql
 * @date 2018/8/9 11:46
 */
@Repository
public class BookMongoDbDao extends MongoDbDao<Book> {
    @Override
    protected Class<Book> getEntityClass() {
        return Book.class;
    }
}