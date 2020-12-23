package com.yzh.mall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.mysql.cj.x.protobuf.MysqlxCrud;
import com.yzh.common.to.OrderTo;
import com.yzh.common.to.SkuHasStockVo;
import com.yzh.common.to.mq.SeckillOrderTo;
import com.yzh.common.utils.R;
import com.yzh.common.vo.MemberVo;
import com.yzh.mall.order.constant.OrderConstant;
import com.yzh.mall.order.dao.OrderItemDao;
import com.yzh.mall.order.entity.OrderItemEntity;
import com.yzh.mall.order.enume.OrderStatusEnum;
import com.yzh.mall.order.feign.CartFeign;
import com.yzh.mall.order.feign.MemberFeign;
import com.yzh.mall.order.feign.ProductFeign;
import com.yzh.mall.order.feign.WmsFeign;
import com.yzh.mall.order.inerceptor.LoginUserInterceptor;
import com.yzh.mall.order.service.OrderItemService;
import com.yzh.mall.order.to.OrderCreateTo;
import com.yzh.mall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.order.dao.OrderDao;
import com.yzh.mall.order.entity.OrderEntity;
import com.yzh.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    //保证每一个用户有自己的消息
    private ThreadLocal<OrderSubmitVo> threadLocal=new ThreadLocal<>();

    @Autowired
    MemberFeign memberFeign;
    @Autowired
    CartFeign cartFeign;

    @Autowired
    OrderDao orderDao;

    @Autowired
    OrderItemDao orderItemDao;

    @Autowired
    ProductFeign productFeign;

    @Autowired
    WmsFeign wmsFeign;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单需要的数据
     * @return
     */
    @Override
    public OrederConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrederConfirmVo orederConfirmVo=new OrederConfirmVo();
        MemberVo memberVo = LoginUserInterceptor.login.get();

        //获取主线程的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddress = CompletableFuture.runAsync(() -> {
            //其他线程也放入请求
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //收货列表
            List<MemeberAddressVo> address = memberFeign.findAddress(memberVo.getId());
            orederConfirmVo.setAddressVos(address);
        }, threadPoolExecutor);

        CompletableFuture<Void> order = CompletableFuture.runAsync(() -> {
            //其他线程也放入请求
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> cartItem = cartFeign.getCartItem();
            orederConfirmVo.setOrderItems(cartItem);
        }, threadPoolExecutor).thenRunAsync(()->{
            List<OrderItemVo> orderItems = orederConfirmVo.getOrderItems();
            List<Long> skuIds = orderItems.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R<List<SkuHasStockVo>> skuStock = wmsFeign.getSkuStock(skuIds);
            List<SkuHasStockVo> data = skuStock.getData();
            if (data!=null){
                Map<Long, Boolean> collect = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::isHasStock));
                orederConfirmVo.setStocks(collect);
            }
        },threadPoolExecutor);

        CompletableFuture<Void> Integration = CompletableFuture.runAsync(() -> {
            //远程积分
            Integer integration = memberVo.getIntegration();
            orederConfirmVo.setIntegration(integration);
        }, threadPoolExecutor);

        //防重令牌
        String replace = UUID.randomUUID().toString().replace("_", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberVo.getId(),replace,30, TimeUnit.MINUTES);
        orederConfirmVo.setOrderToken(replace);
        CompletableFuture.allOf(getAddress,order,Integration).get();
        return orederConfirmVo;
    }

    /**
     * 下单
     * @param vo
     * @return
     */
    //@GlobalTransactional//全局事务seata
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        threadLocal.set(vo);
        SubmitOrderResponseVo submitOrderResponseVo=new SubmitOrderResponseVo();
        submitOrderResponseVo.setCode(0);
        //1.验证令牌 必须保证原子性
        //lua脚本 0代表失败
        String scripts="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        MemberVo memberVo = LoginUserInterceptor.login.get();

        String orderToken = vo.getOrderToken();
        //执行令牌api原子性
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(scripts, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberVo.getId()), orderToken);
        if (execute == 0L){
            //失败
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        }else {
            OrderCreateTo order = createOrder();
            //验价略过
            saveOrder(order);
            //库存锁定，只要有异常抛出
            WareSkuLockVo lockVo=new WareSkuLockVo();
            lockVo.setOrderSn(order.getOrder().getOrderSn());
            List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(item.getSkuId());
                orderItemVo.setCount(item.getSkuQuantity());
                orderItemVo.setTitle(item.getSkuName());
                return orderItemVo;
            }).collect(Collectors.toList());
            lockVo.setLocks(collect);
            //远程锁库存
            R r = wmsFeign.orderLockStock(lockVo);
            if (r.getCode()==0){
                //锁成功
                submitOrderResponseVo.setOrderEntity(order.getOrder());
                //订单创建成功发送消息
                rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());

                return submitOrderResponseVo;
            }else {
                submitOrderResponseVo.setCode(3);
                throw new RuntimeException("扣库存失败");
            }


        }
    }

    @Override
    public OrderEntity getOrderBySn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn",orderSn));
    }

    /**
     * 定时关单
     * @param orderEntity
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        OrderEntity orderent = this.getById(orderEntity.getId());
        //关单
        if (orderent.getStatus()==OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderent,orderTo);
            //给MQ发
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);
        }

    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        //保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + seckillOrderTo.getNum()));
        orderEntity.setPayAmount(multiply);
        //保存订单
        this.save(orderEntity);
        //保存订单项信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        orderItemEntity.setSkuQuantity(seckillOrderTo.getNum());
        orderItemService.save(orderItemEntity);


    }

    /**
     * 保存订单数据
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        this.save(order);
        orderDao.insert(order);
    }

    private OrderCreateTo createOrder(){
        OrderCreateTo orderCreateTo=new OrderCreateTo();
        String timeId = IdWorker.getTimeId();
        //生成订单号
        OrderEntity orderEntity = buildOrder(timeId);
        //创建每一个订单项
        List<OrderItemEntity> orderItemEntities = buildOrders(timeId);
        //比价格
        compterePrice(orderEntity,orderItemEntities);
        return orderCreateTo;
    }

    private void compterePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal bigDecimal = new BigDecimal("0.0");
        //叠加
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            BigDecimal realAmount = orderItemEntity.getRealAmount();
            bigDecimal=bigDecimal.add(realAmount);
        }
        orderEntity.setTotalAmount(bigDecimal);
    }

    private OrderEntity buildOrder(String timeId) {
        MemberVo memberVo = LoginUserInterceptor.login.get();
        OrderEntity orderEntity=new OrderEntity();
        orderEntity.setOrderSn(timeId);
        orderEntity.setMemberId(memberVo.getId());
        //获取收货地址信息
        OrderSubmitVo orderSubmitVo = threadLocal.get();
        //收货地址
        R fare = wmsFeign.getFare(orderSubmitVo.getAddrId());
        Object data = fare.get("data");
        String s = JSON.toJSONString(data);
        FareVo fareVo = JSON.parseObject(s, new TypeReference<FareVo>() {});
        //设置运费信息
        orderEntity.setFreightAmount(fareVo.getFare());
        orderEntity.setReceiverCity(fareVo.getAddressVo().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getAddressVo().getDetailAddress());
        orderEntity.setReceiverName(fareVo.getAddressVo().getName());
        orderEntity.setReceiverPhone(fareVo.getAddressVo().getPhone());
        orderEntity.setReceiverPostCode(fareVo.getAddressVo().getPostCode());
        orderEntity.setReceiverProvince(fareVo.getAddressVo().getProvince());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        return orderEntity;
    }

    /**
     * 构建每一个订单项
     * 
     * @return
     */
    private List<OrderItemEntity> buildOrders(String items) {
        //获取订单，最后确定购物项的价格
        List<OrderItemVo> cartItem = cartFeign.getCartItem();
        if (cartItem!=null&&cartItem.size()>0){
            List<OrderItemEntity> collect = cartItem.stream().map(item -> {
                OrderItemEntity orderItem = buildOrderItem(item);
                orderItem.setOrderSn(items);

                return orderItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //1.订单号信息

        //商品的Spu
        R spuInfoBySkuId = productFeign.getSpuInfoBySkuId(item.getSkuId());
        Object data = spuInfoBySkuId.get("data");
        String s = JSON.toJSONString(data);
        SpuInfoEntity spu = JSON.parseObject(s, new TypeReference<SpuInfoEntity>() {});
        itemEntity.setSpuBrand(spu.getSpuName());
        itemEntity.setSpuId(spu.getId());
        itemEntity.setSpuName(spu.getSpuName());
        itemEntity.setCategoryId(spu.getCatalogId());
        //商品的Sku
        itemEntity.setSkuId(item.getSkuId());
        itemEntity.setSkuPic(item.getImage());
        itemEntity.setSkuName(item.getTitle());
        itemEntity.setSkuPrice(item.getPrice());
        String sa = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(sa);
        itemEntity.setSkuQuantity(item.getCount());
        //优惠
        //积分
        itemEntity.setGiftGrowth(item.getPrice().intValue());
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        BigDecimal multiply = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = multiply.subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }

}