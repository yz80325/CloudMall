package com.yzh.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.common.utils.PageUtils;
import com.yzh.mall.ware.entity.PurchaseEntity;
import com.yzh.mall.ware.vo.MergeVo;
import com.yzh.mall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:42:51
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnReceive(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> list);

    void done(PurchaseDoneVo purchaseDoneVo);

}

