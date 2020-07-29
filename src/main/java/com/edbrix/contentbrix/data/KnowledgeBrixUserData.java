
package com.edbrix.contentbrix.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeBrixUserData implements Serializable
{
    private String AccessToken;
    @SerializedName("Id")
    @Expose
    private String id;
    @SerializedName("FirstName")
    @Expose
    private String firstName;
    @SerializedName("LastName")
    @Expose
    private String lastName;
    @SerializedName("Email")
    @Expose
    private String email;
    @SerializedName("UserType")
    @Expose
    private String userType;
    @SerializedName("ImageUrl")
    @Expose
    private String imageUrl;

    @SerializedName("ApiKey")
    @Expose
    private String ApiKey;

    @SerializedName("Secretekey")
    @Expose
    private String Secretekey;

    private final static long serialVersionUID = 5947178788505561922L;


    @JsonProperty("Id")
    public String getId() {
        return id;
    }@JsonProperty("Id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("FirstName")
    public String getFirstName() {
        return firstName;
    }@JsonProperty("FirstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty("LastName")
    public String getLastName() {
        return lastName;
    }@JsonProperty("LastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty("Email")
    public String getEmail() {
        return email;
    }@JsonProperty("Email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("UserType")
    public String getUserType() {
        return userType;
    }@JsonProperty("UserType")
    public void setUserType(String userType) {
        this.userType = userType;
    }

    @JsonProperty("ImageUrl")
    public String getImageUrl() {
        return imageUrl;
    }@JsonProperty("ImageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }



    @JsonProperty("AccessToken")
    public String getAccessToken() {
        return AccessToken;
    }@JsonProperty("AccessToken")
    public void setAccessToken(String accessToken) {
        AccessToken = accessToken;
    }

    @JsonProperty("ApiKey")
    public String getApiKey() {     return ApiKey; }
    public void setApiKey(String apiKey) {  ApiKey = apiKey;    }

    @JsonProperty("Secretekey")
    public String getSecretekey() {     return Secretekey;  }
    public void setSecretekey(String secretekey) {  Secretekey = secretekey;    }

}