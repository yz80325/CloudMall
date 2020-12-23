package com.yzh.mallcart.vo;

import java.math.BigDecimal;
import java.util.List;

public class Cart {
    List<CartItem>items;
    private Integer countNum;
    private Integer countType;

    private BigDecimal totalAmount;//商品总价

    private BigDecimal reduce=new BigDecimal("0.00");//优惠

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count=0;
        if(items==null&&items.size()>0){
            for (CartItem item:items){
                count+=1;
            }
        }
        return count;
    }

    public Integer getCountType() {

        return countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal bigDecimal=new BigDecimal("0");
        if(items==null&&items.size()>0){
            for (CartItem item:items){
                if (item.getCheck()){
                    BigDecimal totalPrice = item.getTotalPrice();
                    bigDecimal=bigDecimal.add(totalPrice);
                }

            }
        }
        BigDecimal subtract = bigDecimal.subtract(getReduce());
        return subtract;
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
