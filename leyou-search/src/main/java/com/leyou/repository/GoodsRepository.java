package com.leyou.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

//extends ElasticsearchRepository<Goods,Long>
public interface GoodsRepository  extends ElasticsearchRepository<Goods,Long> {
}
