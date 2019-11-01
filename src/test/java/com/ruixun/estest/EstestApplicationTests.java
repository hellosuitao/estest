package com.ruixun.estest;

import com.ruixun.estest.bean.Item;
import com.ruixun.estest.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

}
