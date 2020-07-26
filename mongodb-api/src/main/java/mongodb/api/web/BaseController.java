package mongodb.api.web;

import mongodb.api.bean.Book;
import mongodb.api.bean.FileRecord;
import mongodb.api.service.MongoDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/***
 * @author zhengql
 * @date 2018/8/9 10:38
 */
@RestController
public class BaseController {

    @Autowired
    private MongoDbService mongoDbService;

    @PostMapping("/mongo/save")
    public String saveObj() {
        return mongoDbService.saveFileRecord();
    }
//    @PostMapping("/mongo/save")
//    public String saveObj(@RequestBody Book book) {
//        return mongoDbService.saveObj(book);
//    }

    @GetMapping("/mongo/findByPage")
    public List<FileRecord> findAll() {
        return mongoDbService.findByPage();
    }
//    @GetMapping("/mongo/findAll")
//    public List<Book> findAll() {
//        return mongoDbService.findAll();
//    }

    @GetMapping("/mongo/findOne")
    public Book findOne(@RequestParam String id) {
        return mongoDbService.getBookById(id);
    }

    @GetMapping("/mongo/findOneByName")
    public Book findOneByName(@RequestParam String name) {
        return mongoDbService.getBookByName(name);
    }

    @PostMapping("/mongo/update")
    public String update(@RequestBody Book book) {
        return mongoDbService.updateBook(book);
    }

    @PostMapping("/mongo/delOne")
    public String delOne(@RequestBody Book book) {
        return mongoDbService.deleteBook(book);
    }

    @GetMapping("/mongo/delById")
    public String delById(@RequestParam String id) {
        return mongoDbService.deleteBookById(id);
    }
}