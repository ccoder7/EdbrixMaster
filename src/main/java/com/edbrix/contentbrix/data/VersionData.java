package com.edbrix.contentbrix.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class VersionData implements Serializable {

    @SerializedName("Success")
    @Expose
    private String success;

    @SerializedName("Message")
    @Expose
    private String message;

    @SerializedName("Code")
    @Expose
    private String code;

    @SerializedName("ProductVersion")
    @Expose
    private String productVersion;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }
}
