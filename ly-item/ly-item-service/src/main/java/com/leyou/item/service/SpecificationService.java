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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    public List<Specparm> queryParmList(Long gid,Long cid ,Boolean searching) {
        Specparm grop = new Specparm();
        grop.setGroupId(gid);
        grop.setCid(cid);
        grop.setSearching(searching);
        List<Specparm> se = specParamMapper.select(grop);
        if (CollectionUtils.isEmpty(se)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return se;
    }

    public List<Specification> querySpecsByCid(Long cid) {
        // 查询规格组
        List<Specification> groups = this.queryById(cid);
        List<Specparm> parms = queryParmList(null, cid, null);

        Map<Long,List<Specparm>> map =new HashMap<>();

        for (Specparm parm : parms ){
            if (!map.containsKey(parm.getGroupId())){
                //这个组id在map中不存在，新增一个List
                map.put(parm.getGroupId(),new ArrayList<>());
            }
            map.get(parm.getGroupId()).add(parm);
        }

        //填充parm到groups
        for (Specification specification : groups){
            specification.setParams(map.get(specification.getId()));
        }


        return groups;
    }


}

