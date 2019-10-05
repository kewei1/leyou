package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
    public class BrandService {

        @Autowired
        private BrandMapper brandMapper;

        public PageResult<Brand> queryBrandByPageAndSort(
                Integer page, Integer rows, String sortBy, Boolean desc, String key) {
            // 开始分页
            PageHelper.startPage(page, rows);
            // 过滤
            Example example = new Example(Brand.class);
            if (StringUtils.isNotBlank(key)) {
                example.createCriteria().andLike("name", "%" + key + "%")
                        .orEqualTo("letter", key);
            }
            if (StringUtils.isNotBlank(sortBy)) {
                // 排序
                String orderByClause = sortBy + (desc ? " DESC" : " ASC");
                example.setOrderByClause(orderByClause);
            }
            // 查询
            List<Brand> list = brandMapper.selectByExample(example);


            PageInfo<Brand> pageInfo = new PageInfo<>(list);
            /**
             * 5.返回分页结果
             */
            return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());
        }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // 新增品牌信息
        this.brandMapper.insertSelective(brand);
        // 新增品牌和分类中间表
        for (Long cid : cids) {
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
        }
    }

    public List<Brand> queryBrandByCategory(Long cid) {
        return this.brandMapper.queryByCategoryId(cid);

    }


    public Brand queryBrandById(Long id) {
        Brand brand = new Brand();
        brand.setId(id);

        return this.brandMapper.selectOne(brand);
    }
}
