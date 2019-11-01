package com.ruixun.estest.bean;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
//@Getter
//@Setter
@Document(indexName = "products", type = "docs", shards = 3, replicas = 2)
public class Item {

    @Id
    private Long id;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String name;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Long)
    private Long cid;
    @Field(type = FieldType.Long)
    private Long price;
    @Field(index = false, type = FieldType.Keyword)
    private String image;

    public Item() {
    }

    public Item(Long id, String name, String title, Long cid, Long price, String image) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.cid = cid;
        this.price = price;
        this.image = image;
    }
}
