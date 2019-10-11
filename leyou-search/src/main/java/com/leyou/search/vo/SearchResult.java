package com.leyou.search.vo;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.search.pojo.Goods;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult extends PageResult<Goods> {

    private List<Category> categories;

    private List<Brand> brands;

    public SearchResult() {
        super();
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult(Long total, Long totalPage, List<Goods> items, List<Category> categories, List<Brand> brands) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
    }
}
