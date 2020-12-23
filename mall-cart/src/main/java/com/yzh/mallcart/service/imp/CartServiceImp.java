package com.yzh.mallcart.service.imp;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yzh.common.utils.R;
import com.yzh.common.vo.MemberVo;
import com.yzh.mallcart.feign.ProductFeign;
import com.yzh.mallcart.interceptor.CartInterceptor;
import com.yzh.mallcart.service.CartService;
import com.yzh.mallcart.to.UserInfoTo;
import com.yzh.mallcart.vo.Cart;
import com.yzh.mallcart.vo.CartItem;
import com.yzh.mallcart.vo.SkuInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImp implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeign productFeign;

    @Autowired
    ThreadPoolExecutor executor;
    //redis
    private final String CART_PREFIX="yzhmall:cart";
    /**
     * 给购物车添加数据
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String sku =(String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(sku)){
            CartItem cartItem = new CartItem();
            //远程查询需要添加的商品信息
            CompletableFuture<Void> getInfoTask = CompletableFuture.runAsync(() -> {
                R info = productFeign.info(skuId);
                ObjectMapper mapper = new ObjectMapper();
                LinkedHashMap<String, String> skuInfo = (LinkedHashMap<String, String>) info.get("skuInfo");
                String s = null;
                try {
                    s = mapper.writeValueAsString(skuInfo);
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                }
                SkuInfoEntity skuInfoEntity = JSON.parseObject(s, SkuInfoEntity.class);
                //将商品加入购物车

                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfoEntity.getSkuDefaultImg());
                cartItem.setTitle(skuInfoEntity.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(skuInfoEntity.getPrice());
            }, executor);

            CompletableFuture<Void> Attr = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeign.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(Attr,getInfoTask).get();

            String s=JSON.toJSONString(cartItem);
            //sku的组合信息，使用多线程
            cartOps.put(skuId.toString(),s);
            return cartItem;
        }
        else {
            CartItem cartItem= JSON.parseObject(sku, CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String cartItemString = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(cartItemString, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()!=null){
            //已经登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //先查看临时购物车是否有数据
            List<CartItem> cart1 = getCart(CART_PREFIX + userInfoTo.getUserKey());
            if (cart1!=null){
                for (CartItem cartItem : cart1) {
                    addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
                clearCart(CART_PREFIX + userInfoTo.getUserKey());
            }
            //合并完后的购物车
            List<CartItem> cart2 = getCart(cartKey);
            cart.setItems(cart2);
        }else {
            //没登录
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> cart1 = getCart(cartKey);
            cart.setItems(cart1);
            }
        return cart;
    }

    /**
     * 清理购物车
     *
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkChange(Long skuId, Integer ckeck) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(ckeck.equals(1)?true:false);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    /**
     * 获取当前用户的购物车
     * @return
     */
    @Override
    public List<CartItem> getUserCartItem() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()==null){
            return null;
        }else {
            //购物车里的key
            String s = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> collect = getCart(s).stream().filter(cartItem -> cartItem.getCheck()).
                    map(item->{
                        item.setPrice(productFeign.getPrice(item.getSkuId()));
                        return item;
                    }).collect(Collectors.toList());
            return collect;
        }
    }


    /**
     * 查看购物车是否有数据
     * @param cartKey
     * @return
     */
    private List<CartItem> getCart(String cartKey) {
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map((obj) -> {
                String obj1 = (String) obj;
                CartItem cartItem = JSON.parseObject(obj1, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 获取购物车信息
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey="";
        if (!StringUtils.isEmpty(userInfoTo.getUserId())){
            //已经登录
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        }else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }
}
