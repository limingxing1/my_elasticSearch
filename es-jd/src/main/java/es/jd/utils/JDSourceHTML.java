package es.jd.utils;

import com.alibaba.fastjson.JSONArray;
import es.jd.pojo.GoodsInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class JDSourceHTML {
    /**
     * 爬取京东的数据
     * @param keywords 关键字
     * @return 商品信息
     * @throws IOException IO异常
     */
    public List<GoodsInfo> parseGoods(String keywords) throws IOException {
        //1.获取请求
        String url = "https://search.jd.com/Search?keyword=" + keywords + "&enc=utf-8 ";
        //2.解析网页（Json返回Document就是Document对象）
        Document document = Jsoup.parse(new URL(url), 30000);
        //3.所有你在js中可以使用的方法，在这里都能用
        Element goodsList = document.getElementById("J_goodsList");
        //4.获取所有的li元素，用于取出商品详情信息
        Elements elements = goodsList.getElementsByTag("li");
        List<GoodsInfo> goodsInfoList = new ArrayList<>();
        //5.获取li元素中的内容，此处element就是每一个li标签
        for (Element element : elements) {
            //注意:此处起初无法获取照片的原因是图片懒加载问题,只要图片多的网站, 所有图片都是延迟加载的！
            String img = element.getElementsByTag("img").eq(0).attr("source-data-lazy-img");
            if (null == img || "".equals(img)){
                img = element.getElementsByTag("img").eq(0).attr("src");
            }

            // 5. 批量插入到es中
            // 6. 获取这些数据实现高亮搜索功能
            String price = element.getElementsByClass("p-price").eq(0).text();
            String title = element.getElementsByClass("p-name").eq(0).text();
            //封装进GoodsInfo对象
            GoodsInfo goodsInfo = new GoodsInfo();
            goodsInfo.setImg(img);
            goodsInfo.setTitle(title);
            goodsInfo.setPrice(price);
            goodsInfoList.add(goodsInfo);
        }
        return goodsInfoList;
    }

//    public static void main(String[] args) throws IOException {
//        JDSourceHTML jdSource = new JDSourceHTML();
//        System.out.println(JSONArray.toJSONString(jdSource.parseGoods("java")));
//    }
}
