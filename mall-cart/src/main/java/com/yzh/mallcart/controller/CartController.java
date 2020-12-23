package com.yzh.mallcart.controller;

import com.yzh.common.constant.AuthServerConstant;
import com.yzh.mallcart.interceptor.CartInterceptor;
import com.yzh.mallcart.service.CartService;
import com.yzh.mallcart.to.UserInfoTo;
import com.yzh.mallcart.vo.Cart;
import com.yzh.mallcart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Controller
public class CartController {
    @Autowired
    CartService cartService;


    @ResponseBody
    @GetMapping("/currentUserCartItem")
    public List<CartItem> getCartItem(){
        return cartService.getUserCartItem();
    }

    @GetMapping("/cartCheckChange")
    public String checkeChange(@RequestParam("skuId") Long skuId,@RequestParam("check")Integer ckeck){
        cartService.checkChange(skuId,ckeck);
        return "redirect:http://cart.yzhmall.com/cart.html";
    }

    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart =cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @GetMapping("/success.html")
    public String successPage(){
        return "success";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId
            , @RequestParam("num")Integer num, RedirectAttributes ra) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId,num);
        ra.addAttribute("skuId",skuId);
        return "redirect:http://cart.yzhmall.com/addCartSuccess.html";
    }

    //防止刷新的时候还自动添加商品数量
    @GetMapping("/addCartSuccess.html")
    public String addCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        CartItem cartItem=cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }
}
