package com.edbrix.contentbrix.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CategoryData implements Serializable {

    @Expose
    private Integer success;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("Message")
    @Expose
    private String message;
    @SerializedName("CategoryList")
    @Expose
    private List<CategoryListData> categoryList = null;
    private final static long serialVersionUID = 1186786931113661801L;

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

    public List<CategoryListData> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<CategoryListData> categoryList) {
        this.categoryList = categoryList;
    }
}
