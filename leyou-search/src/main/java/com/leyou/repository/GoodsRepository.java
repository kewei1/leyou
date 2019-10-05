package com.leyou.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface GoodsRepository  extends ElasticsearchRepository<Goods,Long> {
}
