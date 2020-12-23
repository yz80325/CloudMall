package com.yzh.mall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.yzh.common.to.OrderTo;
import com.yzh.common.to.mq.StockDetail;
import com.yzh.common.to.mq.StockLockedTo;
import com.yzh.common.utils.R;
import com.yzh.mall.ware.constant.MqConstant;
import com.yzh.mall.ware.entity.WareOrderTaskDetailEntity;
import com.yzh.mall.ware.entity.WareOrderTaskEntity;
import com.yzh.mall.ware.exception.NoStockException;
import com.yzh.mall.ware.feign.OrderFeign;
import com.yzh.mall.ware.feign.ProductFeignService;
import com.yzh.mall.ware.service.WareOrderTaskDetailService;
import com.yzh.mall.ware.service.WareOrderTaskService;
import com.yzh.mall.ware.vo.*;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.ware.dao.WareSkuDao;
import com.yzh.mall.ware.entity.WareSkuEntity;
import com.yzh.mall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

//监听解锁队列
@RabbitListener(queues = MqConstant.STOCK_RELEASE_QUEUE)
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao skuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeign orderFeign;


    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    /**
     * 订单取消
     */

    public void unLStock(Long skuId,Long wareId,Integer num,Long taskDetailed){
        //解锁
        baseMapper.unLockStock(skuId,wareId,num);
        //更新库存工作单
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(taskDetailed);
        wareOrderTaskDetailEntity.setLockStatus(2);//变为解锁//
        orderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities==null||wareSkuEntities.size()==0){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //远程调用 失败不用回滚
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode()==0){
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            }catch (Exception e){

            }


            baseMapper.insert(wareSkuEntity);
        }else {
            //更新
            baseMapper.addStock(skuId,wareId,skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStack(List<Long> ids) {
        List<SkuHasStockVo> collect = ids.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            Long count = this.baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count==null?false:count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为订单锁库存
     * @param skuLockVo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public boolean lockStock(WareSkuLockVo skuLockVo) {
        /**
         * 保存库存工作清单
         * 方便回溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        //设置锁哪个订单
        wareOrderTaskEntity.setOrderSn(skuLockVo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        //按照下单的收货地址，锁定就近库存
        //找到每个商品在哪有库存
        List<OrderItemVo> locks = skuLockVo.getLocks();
        List<SkuHasStock> collect = locks.stream().map(item -> {
            SkuHasStock skuHasStock = new SkuHasStock();
            Long skuId = item.getSkuId();
            skuHasStock.setSkuId(skuId);
            skuHasStock.setNum(item.getCount());
            //列出cangkuId
            List<Long> waresIds = skuDao.listWareIdHasSkuStock(skuId);
            skuHasStock.setWareId(waresIds);
            return skuHasStock;
        }).collect(Collectors.toList());
        for (SkuHasStock skuHasStock : collect) {
            boolean skuStock=false;
            Long skuId = skuHasStock.getSkuId();
            List<Long> wareIds = skuHasStock.getWareId();
            if (wareIds==null&&wareIds.size()==0){
                throw new NoStockException(skuId);
            }
            //1.如果每个商品都锁定成功，就相当于当前商品锁定了几件发给MQ
            //如果失败，事务回滚，发送出去的消息，即使要解锁记录，由于没有数据
            for (Long wareId : wareIds) {
                //成功就返回1,锁定库存
                Long count=skuDao.lockStockSku(skuId,wareId,skuHasStock.getNum());
                if (count==0){
                    //当前仓库锁失败
                    break;
                }else {
                    //锁定成功
                    skuStock=true;
                    //TODO 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", skuHasStock.getNum(), wareOrderTaskEntity.getId(), wareId, 1);
                    //保存工作单详情
                    orderTaskDetailService.save(wareOrderTaskDetailEntity);
                    //rabbitTemplate
                    StockLockedTo stockLockedTo=new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetail stockDetail = new StockDetail();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity,stockDetail);
                    //只发Id不行，防止回滚以后找不到数据
                    stockLockedTo.setDetail(stockDetail);
                    rabbitTemplate.convertAndSend(MqConstant.STOCK_EXCHANGE,"stock.locked",stockLockedTo);

                }
            }
            if (skuStock==false){
                throw new NoStockException(skuId);
            }
        }
        //全部成功
        return true;

    }

    /**
     * seate测试
     */
    @Override
    @Transactional
    public void test(){
        skuDao.lockStockSku(1L,2L,1);
    }

    /**
     * 解锁库存逻辑
     * @param stockLockedTo
     */
    @Override
    public void unLockStock(StockLockedTo stockLockedTo) {

        Long id= stockLockedTo.getId();//库存Id
        StockDetail detail = stockLockedTo.getDetail();
        Long detailId = detail.getId();
        //判断明细数据表是否有数据
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId!=null){
            //解锁，库存没问题
            //订单情况 1.没有这个订单，必须解锁；2，有这个订单，1）订单状态：已取消：解锁库存 2）没取消：不解锁
            Long id1 = stockLockedTo.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id1);
            String orderSn = taskEntity.getOrderSn();//根据订单号查询状态
            R orderStatus = orderFeign.getOrderStatus(orderSn);
            if (orderStatus.getCode()==0){
                //订单数据返回成功
                String s = JSON.toJSONString(orderStatus.get("order"));
                OrderEntity orderEntity = JSON.parseObject(s, new TypeReference<OrderEntity>() {});

                if (orderEntity==null||orderEntity.getStatus()==4){
                    //订单被取消，解锁库存
                    //判断状态
                    if (byId.getLockStatus()==1){
                        unLStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                    }

                }

            }else {
                //远程调用失败，消息拒绝以后重新放到队列，让别人继续消费解锁
                throw new RuntimeException("远程失败");
            }
        }else {
            //无需解锁

        }
    }

    //解决网络抖动原因，订单没有设置成4（已取消），库存优先到期，导致卡顿的订单无法解锁库存
    @Transactional
    @Override
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查询一下最新工作状态，防止重复解锁库存
        WareOrderTaskEntity task=wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        //按照工作单
        List<WareOrderTaskDetailEntity> list = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).
                eq("lock_status", 1));
        if (list!=null){
            for (WareOrderTaskDetailEntity wareOrderTaskDetailEntity : list) {
                unLStock(wareOrderTaskDetailEntity.getSkuId(),wareOrderTaskDetailEntity.getWareId(),wareOrderTaskDetailEntity.getSkuNum(),wareOrderTaskDetailEntity.getId());
            }
        }
    }

    @Data
    class SkuHasStock{
        private Long skuId;
        private Integer num;
        private List<Long>wareId;
    }

}