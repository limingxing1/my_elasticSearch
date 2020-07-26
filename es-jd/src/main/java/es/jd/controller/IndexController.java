package es.jd.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {
    /**
     * 跳转到index.html主页
     * @return
     */
    @RequestMapping({"/","/index"})
    public String index(){
        return "index";
    }
}
