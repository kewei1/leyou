package com.leyou.item.api;


import com.leyou.item.pojo.Specification;
import com.leyou.item.pojo.Specparm;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@RequestMapping("spec")
public interface SpecificationApi {

    @GetMapping("params")
   List<Specparm> querySpecificationByCategoryGId(
            @RequestParam(value = "gid" ,required = false) Long gid ,
            @RequestParam(value = "cid" ,required = false) Long cid  ,
            @RequestParam(value = "searching" ,required = false) Boolean searching );



    @GetMapping("groups/{cid}")
    List<Specification> querySpecGroups(@PathVariable("cid") Long cid);



    // 查询规格参数组，及组内参数
    @GetMapping("{cid}")
   List<Specification> querySpecsByCid(@PathVariable("cid") Long cid);






}
