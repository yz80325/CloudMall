package com.yzh.mallcart.service;

import com.yzh.mallcart.vo.Cart;
import com.yzh.mallcart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void checkChange(Long skuId, Integer ckeck);

    List<CartItem> getUserCartItem();

}
