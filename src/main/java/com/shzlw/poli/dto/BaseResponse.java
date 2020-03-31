package com.shzlw.poli.dto;

import com.shzlw.poli.constant.ResponseCode;
import lombok.Data;

@Data
public class BaseResponse {

    private Integer code;

    private String msg;

    private Object data;

    public BaseResponse(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static BaseResponse success(String successMsg) {
        return new BaseResponse(ResponseCode.SUCCESS,null,null);
    }

    public static BaseResponse success(String successMsg, Object data) {
        return new BaseResponse(ResponseCode.SUCCESS,null,data);
    }

    public static BaseResponse refuse(String refuseMsg) {
        return new BaseResponse(ResponseCode.REQ_REJECT,refuseMsg,null);
    }

    public static BaseResponse bizError(String failMsg) {
        return new BaseResponse(ResponseCode.BIZ_ERROR,failMsg,null);
    }

}
