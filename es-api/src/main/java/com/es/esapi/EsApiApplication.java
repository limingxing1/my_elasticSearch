package com.es.esapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 需要分词，如果不是一个单词，乱序的情况下，分词基本失效。
 * 查询的时候分词，基本没有用，且匹配不准确，可以用于保存一些比较有意义的数据。
 */
@SpringBootApplication
public class EsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EsApiApplication.class, args);
	}

}
