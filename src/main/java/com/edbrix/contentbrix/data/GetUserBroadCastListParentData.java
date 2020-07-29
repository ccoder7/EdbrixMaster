package com.edbrix.contentbrix.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class GetUserBroadCastListParentData implements Serializable {

    @SerializedName("Success")
    @Expose
    private Integer success;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("Message")
    @Expose
    private String message;
    @SerializedName("UserBroadcast")
    @Expose
    private List<GetUserBroadCastListData> userBroadcast = null;
    private final static long serialVersionUID = 6873570993211146297L;

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<GetUserBroadCastListData> getUserBroadcast() {
        return userBroadcast;
    }

    public void setUserBroadcast(List<GetUserBroadCastListData> userBroadcast) {
        this.userBroadcast = userBroadcast;
    }
}
