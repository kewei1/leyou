package com.leyou.item.web;

import com.leyou.item.pojo.Specification;
import com.leyou.item.pojo.Specparm;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<Specification>> querySpecificationByCategoryCid(@PathVariable("cid") Long cid){


        return ResponseEntity.ok(specificationService.queryById(cid));
    }

    @GetMapping("params")
    public ResponseEntity<List<Specparm>> querySpecificationByCategoryGId(@RequestParam("gid") Long gid){


        return ResponseEntity.ok(specificationService.queryByGId(gid));
    }
}

