package com.leyou.common.vo;


import com.leyou.common.enuums.ExceptionEnum;
import lombok.Data;

@Data
public class ExceptionResule {
    private int code;
    private String message;
    private long timeStamp;

    public ExceptionResule(ExceptionEnum e) {
        this.code=e.getCode();
        this.message=e.getMsg();
        this.timeStamp=System.currentTimeMillis();
    }
}
