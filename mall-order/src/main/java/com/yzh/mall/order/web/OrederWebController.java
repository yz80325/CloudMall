package com.yzh.mall.order.web;

import com.yzh.mall.order.service.OrderService;
import com.yzh.mall.order.vo.OrderSubmitVo;
import com.yzh.mall.order.vo.OrederConfirmVo;
import com.yzh.mall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

@Controller
public class OrederWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest httpServletRequest) throws ExecutionException, InterruptedException {
        OrederConfirmVo confirmVo=orderService.confirmOrder();
        model.addAttribute("orderConfirmData",confirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){
        //去创建订单
        SubmitOrderResponseVo submitOrder =orderService.submitOrder(vo);
        if (submitOrder.getCode()==0){
            //支付选择页
            model.addAttribute("submitResponse",submitOrder);
            return "pay";
        }else {
            String msg="下单失败";
            switch (submitOrder.getCode()){
                case 1:msg+="令牌过期";
                break;
                case 2:msg+="订单商品价格变动";
                break;
                case 3:msg+="库存锁定失败";
                break;
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.yzhmall.com/toTrade";
        }
    }

}
