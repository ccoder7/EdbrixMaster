
package com.edbrix.contentbrix.data;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Userproductaccessresponse implements Serializable {

    @SerializedName("Success")
    @Expose
    private int success;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("Message")
    @Expose
    private String message;
    @SerializedName("ProductAccessList")
    @Expose
    private List<ProductAccessList> productAccessList = null;

    @JsonProperty("Success")
    public int getSuccess() {
        return success;
    }@JsonProperty("Success")
    public void setSuccess(Integer success) {
        this.success = success;
    }

    @JsonProperty("Code")
    public String getCode() {
        return code;
    }@JsonProperty("Code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("Message")
    public String getMessage() {
        return message;
    }@JsonProperty("Message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("ProductAccessList")
    public List<ProductAccessList> getProductAccessList() {
        return productAccessList;
    }
    @JsonProperty("ProductAccessList")
    public void setProductAccessList(List<ProductAccessList> productAccessList) {
        this.productAccessList = productAccessList;
    }

}
