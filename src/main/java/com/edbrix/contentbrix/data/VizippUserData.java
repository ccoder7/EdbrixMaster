package com.edbrix.contentbrix.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by rajk
 */

@JsonIgnoreProperties(ignoreUnknown = true)

public class VizippUserData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String firstName;

    private String userId;

    private String lastName;

    private String emailId;

    private boolean isActive;

    private boolean isVerified;

    private boolean isSubscribed;

    private boolean isPasswordReset;

    private String subscriptionExpiryDate;

    private String APITOKEN;

    private String edbrixUserId;

    private String edbrixUserAccessToken;

   /* private ArrayList<String> userNotificationsList = new ArrayList<String>();
    //private String[] userNotifications;

    public ArrayList<String> getUserNotificationsList() {
        return userNotificationsList;
    }

    public void setUserNotificationsList(String msg) {
        userNotificationsList.add(msg);
        //this.userNotificationsList = userNotificationsList;
    }
*/

    public String getUserId() {
        return userId;
    }

    @JsonProperty("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailId() {
        return emailId;
    }

    @JsonProperty("email")
    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @JsonProperty("isActive")
    public void setActive(String active) {
        isActive =(Integer.parseInt(active)==1) ? true : false;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    @JsonProperty("isVerified")
    public void setVerified(String verified) {
        isVerified = (Integer.parseInt(verified)==1) ? true : false;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }

    @JsonProperty("isSubscribed")
    public void setSubscribed(String subscribed) {
        isSubscribed = (Integer.parseInt(subscribed)==1) ? true : false;
    }

    public boolean isPasswordReset() {
        return isPasswordReset;
    }

    public void setPasswordReset(boolean passwordReset) {
        isPasswordReset = passwordReset;
    }

    @JsonProperty("isPasswordReset")
    public void setPasswordReset(String passwordReset) {
        isPasswordReset = (Integer.parseInt(passwordReset)==1) ? true : false;
    }

    public String getSubscriptionExpiryDate() {
        return subscriptionExpiryDate;
    }

    @JsonProperty("subscriptionExpiryDate")
    public void setSubscriptionExpiryDate(String subscriptionExpiryDate) {
        this.subscriptionExpiryDate = subscriptionExpiryDate;
    }

    public String getAPITOKEN() {
        return APITOKEN;
    }

    @JsonProperty("APITOKEN")
    public void setAPITOKEN(String APITOKEN) {
        this.APITOKEN = APITOKEN;
    }

    public String getEdbrixUserId() {
        return edbrixUserId;
    }

    @JsonProperty("edbrixUserId")
    public void setEdbrixUserId(String edbrixUserId) {
        this.edbrixUserId = edbrixUserId;
    }

    public String getEdbrixUserAccessToken() {
        return edbrixUserAccessToken;
    }

    @JsonProperty("edbrixUserAccessToken")
    public void setEdbrixUserAccessToken(String edbrixUserAccessToken) {
        this.edbrixUserAccessToken = edbrixUserAccessToken;
    }

    @Override
    public String toString() {
        return "ClassPojo [FirstName = " + firstName + ", userId = " + userId + ", LastName = " + lastName + ", EmailId = " + emailId + "]";
    }
}
