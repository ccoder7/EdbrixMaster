package com.edbrix.contentbrix.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserOrganizationList implements Serializable
{

    @SerializedName("Id")
    @Expose
    private String id;
    @SerializedName("OrganizationName")
    @Expose
    private String organizationName;
    @SerializedName("ApiKey")
    @Expose
    private String apiKey;
    @SerializedName("Secretekey")
    @Expose
    private String secretekey;
    @SerializedName("SchoolLogoUrl")
    @Expose
    private String schoolLogoUrl;
    private final static long serialVersionUID = 7112430833584357198L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretekey() {
        return secretekey;
    }

    public void setSecretekey(String secretekey) {
        this.secretekey = secretekey;
    }

    public String getSchoolLogoUrl() {
        return schoolLogoUrl;
    }

    public void setSchoolLogoUrl(String schoolLogoUrl) {
        this.schoolLogoUrl = schoolLogoUrl;
    }

}