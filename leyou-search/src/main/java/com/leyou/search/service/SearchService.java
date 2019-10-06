import com.leyou.item.pojo.Spu;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;



    public Goods buildGoods(Spu spu) throws IOException {

        String all = spu.getTitle();

        //构建goods对象
        Goods goods = new Goods();
        goods.setId(spu.getId());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(all);  // 搜索字段，包含标题，分类，品牌，规格等
        goods.setPrice(null); //TODO 所有Sku价格集合
        goods.setSkus(null);  //TODO  所有Sku集合的json格式
        goods.setSpecs(null); //TODO  所有所搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

}

