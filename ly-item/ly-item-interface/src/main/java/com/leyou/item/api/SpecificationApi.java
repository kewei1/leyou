package com.leyou.item.api;


import com.leyou.item.pojo.Specparm;
import org.springframework.web.bind.annotation.GetMapping;
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

}
