package com.edbrix.contentbrix.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AuthenticateUserOrganisationResponse implements Serializable {

    @SerializedName("Success")
    @Expose
    private Integer success;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("Message")
    @Expose
    private String message;
    @SerializedName("UserOrganizationList")
    @Expose
    private List<UserOrganizationList> userOrganizationList = null;
    private final static long serialVersionUID = 4515986850543581949L;

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

    public List<UserOrganizationList> getUserOrganizationList() {
        return userOrganizationList;
    }

    public void setUserOrganizationList(List<UserOrganizationList> userOrganizationList) {
        this.userOrganizationList = userOrganizationList;
    }

}
