package com.ruixun.estest;

import com.ruixun.estest.bean.Item;
import com.ruixun.estest.repository.ItemRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.InternalRange;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class EstestApplicationTests {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void indexTest(){
        /*创建索引*/
        elasticsearchTemplate.createIndex(Item.class);
        /*创建映射  根据配置自动完成映射*/
        elasticsearchTemplate.putMapping(Item.class);
    }

    @Test
    public void deleteIndex(){
        elasticsearchTemplate.deleteIndex(Item.class);
    }

    @Test
    public void addItem(){
        Item item = new Item(2l,"华为P40","华为6G手机",2l,20000l,"www.image.com");
        itemRepository.save(item);
    }

    @Test
    public void updateItem(){
        Item item = new Item(1l,"华为P30","华为5G手机",2l,10000l,"www.imageupdate.com");
        itemRepository.save(item);
    }

    @Test
    public void find(){
        Iterable<Item> items = itemRepository.findAll();
        items.forEach(item -> System.out.println(item));
    }

    @Test
    public void testFindAll(){
        Iterable<Item> items = itemRepository.findAll(Sort.by(Sort.Direction.DESC, "price"));
        items.forEach(item -> System.out.println(item));
    }

    @Test
    public void testPriceBetween(){
        itemRepository.findByPriceBetween(10000l,20000l).forEach(item -> {
            System.out.println(item);
        });
    }

    @Test
    public void testfindByPriceBetween(){
        Page<Item> page = itemRepository.findByPriceBetween(10000l, 20000l, PageRequest.of(0,1,Sort.by(Sort.Direction.DESC)));
        System.out.println(page.getTotalPages());
        page.getContent().forEach(item -> {
            System.out.println(item);
        });
    }

    /*----------------------高级查询--------------------------*/


    @Test
    /*原生查询*/
    public void testBaseQuery(){
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", "手机");
        Iterable<Item> items = itemRepository.search(matchQueryBuilder);
        items.forEach(System.out::println);
    }

    @Test
    /*原生查询*/
    public void testBaseQueryTemplate(){
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", "手机");
//        Iterable<Item> items = itemRepository.search(matchQueryBuilder);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(matchQueryBuilder);
        List<Item> items = elasticsearchTemplate.queryForList(nativeSearchQuery, Item.class);
        items.forEach(System.out::println);
    }

    /*自定义查询*/
    @Test
    public void NativeQuery(){
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(QueryBuilders.matchQuery("title","手机")).withQuery(QueryBuilders.matchQuery("title","5G"));
        Page<Item> items = itemRepository.search(builder.build());

        items.forEach(System.out::println);

        System.out.println(items.getTotalPages());
        items.getContent().forEach(System.out::println);
    }

    @Test
    public void NativeQuery2(){
        /*构建查询条件*/
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        /*显示结果过滤*/
        builder.withSourceFilter(new FetchSourceFilter(new String[]{"id","title","price"},null));
        /*添加分词查询*/
        builder.withQuery(QueryBuilders.matchQuery("title","华为"));
        /*排序*/
        builder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        /*分页*/
        builder.withPageable(PageRequest.of(0,2));
        /*查询*/
        Page<Item> items = itemRepository.search(builder.build());

        items.forEach(System.out::println);

        System.out.println(items.getTotalPages());
        items.getContent().forEach(System.out::println);
    }
    @Test
    public void NativeQuery3(){
        /*构建查询条件*/
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        /*显示结果过滤*/
        builder.withSourceFilter(new FetchSourceFilter(new String[]{"id","title","price"},null));
        /*添加boolean查询*/
//        builder.withQuery(boolQuery.must(QueryBuilders.matchQuery("title","5")));
        /*分页*/
        builder.withPageable(PageRequest.of(0,2));
        builder.withFilter(QueryBuilders.termQuery("id",2l));
        /*查询*/
        Page<Item> items = itemRepository.search(builder.build());

        items.forEach(System.out::println);

        System.out.println(items.getTotalPages());
        items.getContent().forEach(System.out::println);
    }
    @Test
    /*分桶*/
    public void NativeQuery4(){
        /*构建查询条件*/
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        /*显示结果过滤*/
        builder.withSourceFilter(new FetchSourceFilter(new String[]{""},null));
        /*添加分桶*/
        builder.addAggregation(AggregationBuilders.range("priceName").field("price").addRange(10000l,20000l));
//        builder.addAggregation(AggregationBuilders.terms("cid").field("cid"));

        /*查询*/
        AggregatedPage<Item> page = (AggregatedPage<Item>) itemRepository.search(builder.build());
        InternalRange internalRange = (InternalRange) page.getAggregation("priceName");
        /*获得桶*/
        List<InternalRange.Bucket> buckets = internalRange.getBuckets();
        /*遍历*/
        for (InternalRange.Bucket bucket : buckets) {
            System.out.println(bucket.getKey());
            System.out.println(bucket.getDocCount());
        }
    }
    @Test
    /*在4 的基础上 分桶+度量*/
    public void NativeQuery5(){
        /*构建查询条件*/
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        /*显示结果过滤*/
        builder.withSourceFilter(new FetchSourceFilter(new String[]{""},null));
        /*添加分桶*/
        builder.addAggregation(AggregationBuilders.terms("cidName").field("cid")
                .subAggregation(AggregationBuilders.avg("priceAvg").field("price")));

        /*查询*/
        AggregatedPage<Item> page = (AggregatedPage<Item>) itemRepository.search(builder.build());
        LongTerms longTerms = (LongTerms) page.getAggregation("cidName");
        /*获得桶*/
        List<LongTerms.Bucket> buckets = longTerms.getBuckets();
        /*遍历*/
        for (LongTerms.Bucket bucket : buckets) {
            System.out.println(bucket.getKey()+"--->"+bucket.getDocCount());
            InternalAvg priceAvg = (InternalAvg) bucket.getAggregations().asMap().get("priceAvg");
            System.out.println("平均值"+priceAvg.getValue());
        }
    }

}
