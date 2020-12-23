package com.yzh.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.common.utils.PageUtils;
import com.yzh.mall.ware.entity.WareInfoEntity;
import com.yzh.mall.ware.vo.FareVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:42:51
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long id);
}

