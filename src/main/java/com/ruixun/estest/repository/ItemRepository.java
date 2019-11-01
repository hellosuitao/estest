package com.ruixun.estest.repository;

import com.ruixun.estest.bean.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ItemRepository extends ElasticsearchRepository<Item, Long> {

    List<Item> findByPriceBetween(Long price1,Long price2);

    Page<Item> findByPriceBetween(Long price1, Long price2, Pageable pageable);
}
