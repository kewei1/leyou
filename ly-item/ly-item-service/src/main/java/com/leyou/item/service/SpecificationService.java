package com.leyou.item.service;

import com.leyou.common.enuums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.mapper.SpecificationMapper;
import com.leyou.item.pojo.Specification;
import com.leyou.item.pojo.Specparm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<Specification> queryById(Long cid) {
        Specification grop = new Specification();
        grop.setCid(cid);

        List<Specification> select = specificationMapper.select(grop);
        if (CollectionUtils.isEmpty(select)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return select;
    }


    public List<Specparm> queryByGId(Long gid) {
        Specparm grop = new Specparm();
        grop.setGroupId(gid);
        List<Specparm> se = specParamMapper.select(grop);
        if (CollectionUtils.isEmpty(se)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return se;
    }
}

