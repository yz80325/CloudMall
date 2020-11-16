package com.yzh.common.constant;

public class WareConstant {
    public enum PurchaseStatusEnum{
        CREATED(0,"已创建"),ASSIGNED(1,"已分配")
        ,RECEIVED(2,"已领取"),FINISH(3,"已完成")
        ,HASERROR(4,"有异常");
        private Integer code;
        private String Detail;
        PurchaseStatusEnum(Integer code,String Detail){
            this.code=code;
            this.Detail=Detail;
        }
        public Integer getCode(){
            return this.code;
        }
        public String getDetail(){
            return this.Detail;
        }
    }
    public enum PurchaseDetailStatusEnum{
        CREATED(0,"已创建"),ASSIGNED(1,"已分配")
        ,BUYING(2,"正在采购"),FINISH(3,"已完成")
        ,HASERROR(4,"采购失败");
        private Integer code;
        private String Detail;
        PurchaseDetailStatusEnum(Integer code,String Detail){
            this.code=code;
            this.Detail=Detail;
        }
        public Integer getCode(){
            return this.code;
        }
        public String getDetail(){
            return this.Detail;
        }
    }
}
