package com.yzh.common.constant;

public class ProductConstant {
    public enum AttrType {
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");
        private Integer code;
        private String Detail;

        AttrType(Integer code, String Detail) {
            this.code = code;
            this.Detail = Detail;
        }

        public Integer getCode() {
            return this.code;
        }

        public String getDetail() {
            return this.Detail;
        }
    }

    public enum StatusEnum {
        NEW_SPU(0, "新建"), SPU_UP(1, "上架"),
        SPU_DOWN(2, "商品下架");
        private Integer code;
        private String Detail;

        StatusEnum(Integer code, String Detail) {
            this.code = code;
            this.Detail = Detail;
        }

        public Integer getCode() {
            return this.code;
        }

        public String getDetail() {
            return this.Detail;
        }
    }
}
