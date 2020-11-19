package com.yzh.mall.search;

import com.alibaba.fastjson.JSON;
import com.yzh.mall.search.config.MallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallSearchApplicationTests {

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	/**
	 * 聚合查询
	 * @throws IOException
	 */
	@Test
	public void searchData() throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		//指定索引
		searchRequest.indices("bank");

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

		TermsAggregationBuilder AgeAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
		AvgAggregationBuilder BlanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
		searchSourceBuilder.aggregation(AgeAgg);
		searchSourceBuilder.aggregation(BlanceAvg);

		searchRequest.source(searchSourceBuilder);
		//2,执行解锁
		SearchResponse search = restHighLevelClient.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);

		SearchHits hits = search.getHits();
		SearchHit[] AllHits = hits.getHits();
		for (SearchHit allHit : AllHits) {
			String sourceAsString = allHit.getSourceAsString();
			System.out.println(sourceAsString);
		}


	}
	@Test
	public void contextLoads() throws IOException {
		IndexRequest indexRequest=new IndexRequest("users");
		indexRequest.id("1");
		User user=new User();
		user.setName("小明");
		user.setGender("F");
		String s = JSON.toJSONString(user);
		indexRequest.source(s, XContentType.JSON);
		//执行
		IndexResponse index = restHighLevelClient.index(indexRequest, MallElasticSearchConfig.COMMON_OPTIONS);
		System.out.println(index);
	}
	@Data
	class User{
		private String name;
		private String gender;
	}

}
