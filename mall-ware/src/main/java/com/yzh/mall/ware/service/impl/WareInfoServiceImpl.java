package com.yzh.mall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yzh.common.utils.R;
import com.yzh.mall.ware.feign.MemberFeign;
import com.yzh.mall.ware.vo.FareVo;
import com.yzh.mall.ware.vo.MemeberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.ware.dao.WareInfoDao;
import com.yzh.mall.ware.entity.WareInfoEntity;
import com.yzh.mall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeign memberFeign;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key =(String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.eq("id",key).or().like("name",key).or().like("address",key).or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long id) {
        FareVo fareVo=new FareVo();
        R info = memberFeign.info(id);
        Object data = info.get("data");
        String s = JSON.toJSONString(data);
        MemeberAddressVo memeberAddressVo = JSON.parseObject(s, new TypeReference<MemeberAddressVo>() {
        });
        fareVo.setAddressVo(memeberAddressVo);
        fareVo.setFare(new BigDecimal(1));
        return fareVo;
    }

}