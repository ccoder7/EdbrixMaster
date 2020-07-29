
package com.edbrix.contentbrix.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductAccessList implements Serializable {

    @SerializedName("ProductId")
    @Expose
    private String productId;
    @SerializedName("Title")
    @Expose
    private String title;
    @SerializedName("HasAccess")
    @Expose
    private Integer hasAccess;

    @JsonProperty("ProductId")
    public String getProductId() {
        return productId;
    }@JsonProperty("ProductId")
    public void setProductId(String productId) {
        this.productId = productId;
    }

    @JsonProperty("Title")
    public String getTitle() {
        return title;
    }@JsonProperty("Title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("HasAccess")
    public Integer getHasAccess() {
        return hasAccess;
    }@JsonProperty("HasAccess")
    public void setHasAccess(Integer hasAccess) {
        this.hasAccess = hasAccess;
    }

}
