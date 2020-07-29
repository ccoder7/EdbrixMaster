/**
 *
 */

package com.edbrix.contentbrix.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.edbrix.contentbrix.data.KnowledgeBrixUserData;
import com.edbrix.contentbrix.data.UserData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Wrapper for managing session data.
 *
 * @author rajk
 */
public class SessionManager {

    private static final String LOGTAG = "SessionManager";

    private static final String APP_SHARED_PREFS_NAME = "Edbrix";

    private static final String PREFS_SESSION_VIZIPP_LAST_LOGGED_USERNAME = "SessionLastVUsername";
    private static final String PREFS_SESSION_VIZIPP_USERNAME = "SessionVUsername";
    private static final String PREFS_SESSION_VIZIPP_PASSWORD = "SessionVPassword";
    private static final String PREFS_SESSION_VIZIPP_TOKEN = "SessionVToken";

    private static final String PREFS_SESSION_USERNAME = "SessionUsername";
    private static final String PREFS_SESSION_PASSWORD = "SessionPassword";
    private static final String PREFS_SESSION_PROFILE_TOKEN = "SessionProfileToken";
    private static final String PREFS_SESSION_DEVICE_TOKEN = "SessionDeviceToken";
    private static final String PREFS_SESSION_FCM_TOKEN = "SessionFCMToken";
    private static final String PREFS_SESSION_DPBOX_TOKEN = "SessionDPBoxToken";
    private static final String PREFS_SESSION_DPBOX_DISPLAY_NAME = "SessionDPBoxName";
    private static final String PREFS_SESSION_ORG_ID = "SessionOrgId";
    private static final String PREFS_SESSION_ORG_NAME = "SessionOrgName";
    private static final String PREFS_SESSION_SUB_COURSE_DATA = "SessionSubjectCourseData";
    private static final String PREFS_SESSION_LOGGED_USERDATA = "SessionLoggedUsersData";
    private static final String PREFS_SESSION_LOGGED_VIZIPP_USERDATA = "SessionLoggedVizippUsersData";
    private static final String PREFS_SESSION_LOGGEDUSERSLIST = "SessionLoggedUsersList";
    private static final String PREFS_SESSION_LOGGEDUSERSCREDENTIALSLIST = "SessionLoggedUsersCredentialsList";
    private static final String PREFS_SESSION_RECORDED_SLIDE_DETAILS = "SessionRecordedSlideDetails";
    private static final String PREF_SESSION_VIBRATE_PHONE_STATE = "SessionVibratePhoneState";
    private static final String PREF_SESSION_APPUPDATE_SKIP_KEY = "AppUpdateSkip";
    private static final String PREFS_SESSION_PROFILE_IMAGE_BASE64 = "SessionProfileImageBase64";
    private static final String PREF_SESSION_WALKTHROUGH_SKIP_KEY = "wlkSkip";
    private static final String PREF_SESSION_INFORMATION_DIALOG_SEEN = "infoSeen";
    private static final String PREF_SESSION_DRIVE_FLAG = "DriveConnectedFlag";
    private static final String PREF_SESSION_VIDEO_UPLOAD_COUNT = "VideoUploadCount";
    private static final String PREF_SESSION_VIDEO_RECORD_COUNT = "VideoRecordCount";
    private static final String PREFS_SESSION_STRIPE_PAYMENT_TOKEN = "SessionStripeToken";
    private static final String PREFS_SESSION_INSTALLED_DATE_TIME = "InstalledDateTime";
    private static final String PREFS_SESSION_IS_USER_ACTIVE = "IsUserActive";
    private static final String PREFS_SESSION_IS_SCREEN_CAST_ACTIVE = "IsScreenCastActive";
    private static final String PREFS_SESSION_USER_ID = "UserId";
    private static final String PREFS_SESSION_USER_TYPE = "UserType";



    private SharedPreferences sharedPrefs;

    public SessionManager(Context context) {
        if (context != null) {
            this.sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        } else {
            Log.w(LOGTAG, "Invalid context!");
        }
    }

    /**
     * Checks if we have valid saved session credentials.
     *
     * @return
     */
    public Boolean hasSessionVizippCredentials() {
        return ((this.getPrefsSessionVizippToken().length() > 0) && (this.getLoggedKnowledgeBrixUserData() != null) && (this.getLoggedKnowledgeBrixUserData().getId().length() > 0));
    }

    /**
     * Checks if we have valid saved session credentials.
     *
     * @return
     */
    public Boolean hasSessionCredentials() {
        return ((this.getSessionOrgId().length() > 0) && (this.getSessionProfileToken().length() > 0) && (this.getLoggedUserData() != null) && (this.getLoggedUserData().getId().length() > 0));
    }

    /**
     * Gets the session's saved username.
     *
     * @return
     */
    public String getSessionUsername() {
        return this.sharedPrefs.getString(PREFS_SESSION_USERNAME, "");
    }

    /**
     * Gets the session's saved username for vizipp.
     *
     * @return
     */
    public String getSessionVizippUsername() {
        return this.sharedPrefs.getString(PREFS_SESSION_VIZIPP_USERNAME, "");
    }

    /**
     * Gets the session's saved username for vizipp.
     *
     * @return
     */
    public String getSessionLastLoggedVizippUsername() {
        return this.sharedPrefs.getString(PREFS_SESSION_VIZIPP_LAST_LOGGED_USERNAME, "");
    }

    /**
     * Gets the session's saved password.
     *
     * @return
     */
    public String getSessionPassword() {
        return this.sharedPrefs.getString(PREFS_SESSION_PASSWORD, "");
    }

    /**
     * Gets the session's saved password for vizipp.
     *
     * @return
     */
    public String getSessionVizippPassword() {
        return this.sharedPrefs.getString(PREFS_SESSION_VIZIPP_PASSWORD, "");
    }

    /**
     * Gets the session's saved vizipp login token.
     *
     * @return
     */
    public String getPrefsSessionVizippToken() {
        return this.sharedPrefs.getString(PREFS_SESSION_VIZIPP_TOKEN, "");
    }

    /**
     * Gets the session's saved profileToken.
     *
     * @return
     */
    public String getSessionProfileToken() {
        return this.sharedPrefs.getString(PREFS_SESSION_PROFILE_TOKEN, "");
    }

    /**
     * Gets the session's saved deviceToken.
     *
     * @return
     */
    public String getSessionDeviceToken() {
        return this.sharedPrefs.getString(PREFS_SESSION_DEVICE_TOKEN, "");
    }

    /**
     * Gets the session's saved device FCM Token.
     *
     * @return
     */
    public String getSessionFCMToken() {
        return this.sharedPrefs.getString(PREFS_SESSION_FCM_TOKEN, "");
    }

    /**
     * Gets the session's saved DropBox Token.
     *
     * @return
     */
    public String getSessionDropBoxToken() {
        return this.sharedPrefs.getString(PREFS_SESSION_DPBOX_TOKEN, "");
    }

    /**
     * Gets the session's saved DropBox Display Name.
     *
     * @return
     */
    public String getSessionDropBoxDisplayName() {
        return this.sharedPrefs.getString(PREFS_SESSION_DPBOX_DISPLAY_NAME, "");
    }

    /**
     * Gets the session's saved Stripe Token.
     *
     * @return
     */
    public String getSessionStripePaymentToken() {
        return this.sharedPrefs.getString(PREFS_SESSION_STRIPE_PAYMENT_TOKEN, "");
    }

    /**
     * Gets the session's app installed date time.
     *
     * @return
     */
    public String getSessionAppInstalledDateString() {
        return this.sharedPrefs.getString(PREFS_SESSION_INSTALLED_DATE_TIME, "");
    }

    /**
     * Gets the session's app user active status.
     *
     * @return
     */
    public boolean getSessionAppIsUserActive() {
        return this.sharedPrefs.getBoolean(PREFS_SESSION_IS_USER_ACTIVE, false);
    }

    /**
     * Gets the session's app Screencast active status.
     *
     * @return
     */
    public boolean getSessionAppIsScreenCastActive() {
        return this.sharedPrefs.getBoolean(PREFS_SESSION_IS_SCREEN_CAST_ACTIVE, false);
    }

    /**
     * Gets the session's saved Course Id.
     *
     * @return
     */
    /*public SubjectData getSessionSubjectCourseData() {
        try {
            return (SubjectData) ObjectSerializer.deserialize(this.sharedPrefs.getString(PREFS_SESSION_SUB_COURSE_DATA, ObjectSerializer.serialize(new SubjectData())));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }*/

    /**
     * Gets the session's saved Organization Id.
     *
     * @return
     */
    public String getSessionOrgId() {
        return this.sharedPrefs.getString(PREFS_SESSION_ORG_ID, "");
    }

    /**
     * Gets the session's saved Organization Id.
     *
     * @return
     */
    public String getSessionOrgName() {
        return this.sharedPrefs.getString(PREFS_SESSION_ORG_NAME, "");
    }

    /**
     * Get the session's saved Edbrix UserData object
     *
     * @return UserData object
     */
    public UserData getLoggedUserData() {
        try {
            return (UserData) ObjectSerializer.deserialize(this.sharedPrefs.getString(PREFS_SESSION_LOGGED_USERDATA, ObjectSerializer.serialize(new UserData())));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the session's saved VizippUserData object
     *
     * @return VizippUserData object
     */
    /*public VizippUserData getLoggedVizippUserData() {
        try {
            return (VizippUserData) ObjectSerializer.deserialize(this.sharedPrefs.getString(PREFS_SESSION_LOGGED_VIZIPP_USERDATA, ObjectSerializer.serialize(new VizippUserData())));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }*/
    public KnowledgeBrixUserData getLoggedKnowledgeBrixUserData() {
        try {
            return (KnowledgeBrixUserData) ObjectSerializer.deserialize(this.sharedPrefs.getString(PREFS_SESSION_LOGGED_VIZIPP_USERDATA, ObjectSerializer.serialize(new KnowledgeBrixUserData())));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Updates the saved session credentials.
     *
     * @param username the username to save.
     */
    public void updateSessionUsername(String username) {

        Editor editor = this.sharedPrefs.edit();
        if ((username != null) && (username.length() > 0)) {
            editor.putString(PREFS_SESSION_USERNAME, username);
        } else {
            editor.remove(PREFS_SESSION_USERNAME);
        }
        editor.commit();
    }

    /**
     * Updates the saved session credentials for vizipp.
     *
     * @param username the username to save.
     */
    public void updateSessionVizippUsername(String username) {

        Editor editor = this.sharedPrefs.edit();
        if ((username != null) && (username.length() > 0)) {
            editor.putString(PREFS_SESSION_VIZIPP_USERNAME, username);
        } else {
            editor.remove(PREFS_SESSION_VIZIPP_USERNAME);
        }
        editor.commit();
    }

    /**
     * Updates the saved session credentials for vizipp.
     *
     * @param username the username to save.
     */
    public void updateSessionLastLoggedVizippUsername(String username) {

        Editor editor = this.sharedPrefs.edit();
        if ((username != null) && (username.length() > 0)) {
            editor.putString(PREFS_SESSION_VIZIPP_LAST_LOGGED_USERNAME, username);
        } else {
            editor.remove(PREFS_SESSION_VIZIPP_LAST_LOGGED_USERNAME);
        }
        editor.commit();
    }

    /**
     * Updates the saved session credentials.
     *
     * @param password the password to save.
     */
    public void updateSessionPassword(String password) {

        Editor editor = this.sharedPrefs.edit();
        if ((password != null) && (password.length() > 0)) {
            editor.putString(PREFS_SESSION_PASSWORD, password);
        } else {
            editor.remove(PREFS_SESSION_PASSWORD);
        }
        editor.commit();
    }

    /**
     * Updates the saved session credentials for vizipp.
     *
     * @param password the password to save.
     */
    public void updateSessionVizippPassword(String password) {

        Editor editor = this.sharedPrefs.edit();
        if ((password != null) && (password.length() > 0)) {
            editor.putString(PREFS_SESSION_VIZIPP_PASSWORD, password);
        } else {
            editor.remove(PREFS_SESSION_VIZIPP_PASSWORD);
        }
        editor.commit();
    }

    /**
     * Updates the saved session credentials.
     *
     * @param accessToken the Vizipp Login Token to save.
     */
    public void updateSessionVizippToken(String accessToken) {

        Editor editor = this.sharedPrefs.edit();
        if ((accessToken != null) && (accessToken.length() > 0)) {
            editor.putString(PREFS_SESSION_VIZIPP_TOKEN, accessToken);
        } else {
            editor.remove(PREFS_SESSION_VIZIPP_TOKEN);
        }
        editor.commit();
    }

    /**
     * Updates the saved session credentials.
     *
     * @param accessToken the AccessToken to save.
     */
    public void updateSessionProfileToken(String accessToken) {

        Editor editor = this.sharedPrefs.edit();
        if ((accessToken != null) && (accessToken.length() > 0)) {
            editor.putString(PREFS_SESSION_PROFILE_TOKEN, accessToken);
        } else {
            editor.remove(PREFS_SESSION_PROFILE_TOKEN);
        }
        editor.commit();
    }

    /**
     * Updates the saved session credentials.
     *
     * @param deviceFCMToken the DeviceFCMToken to save.
     */
    public void updateSessionFCMToken(String deviceFCMToken) {

        Editor editor = this.sharedPrefs.edit();
        if ((deviceFCMToken != null) && (deviceFCMToken.length() > 0)) {
            editor.putString(PREFS_SESSION_FCM_TOKEN, deviceFCMToken);
        } else {
            editor.remove(PREFS_SESSION_FCM_TOKEN);
        }
        editor.commit();
    }

    /**
     * Updates the saved session credentials.
     *
     * @param deviceToken the DeviceToken to save.
     */
    public void updateSessionDeviceToken(String deviceToken) {

        Editor editor = this.sharedPrefs.edit();
        if ((deviceToken != null) && (deviceToken.length() > 0)) {
            editor.putString(PREFS_SESSION_DEVICE_TOKEN, deviceToken);
        } else {
            editor.remove(PREFS_SESSION_DEVICE_TOKEN);
        }
        editor.commit();
    }

    /**
     * Updates the saved session dropbox token.
     *
     * @param dropBoxToken the DropBoxToken to save.
     */
    public void updateSessionDropBoxToken(String dropBoxToken) {

        Editor editor = this.sharedPrefs.edit();
        if ((dropBoxToken != null) && (dropBoxToken.length() > 0)) {
            editor.putString(PREFS_SESSION_DPBOX_TOKEN, dropBoxToken);
        } else {
            editor.remove(PREFS_SESSION_DPBOX_TOKEN);
        }
        editor.commit();
    }

    /**
     * Updates the saved session dropbox token.
     *
     * @param displayName the DropBox DisplayName to save.
     */
    public void updateSessionDropBoxDisplayName(String displayName) {

        Editor editor = this.sharedPrefs.edit();
        if ((displayName != null) && (displayName.length() > 0)) {
            editor.putString(PREFS_SESSION_DPBOX_DISPLAY_NAME, displayName);
        } else {
            editor.remove(PREFS_SESSION_DPBOX_DISPLAY_NAME);
        }
        editor.commit();
    }

    /**
     * Updates the saved session StripePayment token.
     *
     * @param stripeToken the StripePayment token to save.
     */
    public void updateSessionStripePaymentToken(String stripeToken) {

        Editor editor = this.sharedPrefs.edit();
        if ((stripeToken != null) && (stripeToken.length() > 0)) {
            editor.putString(PREFS_SESSION_STRIPE_PAYMENT_TOKEN, stripeToken);
        } else {
            editor.remove(PREFS_SESSION_STRIPE_PAYMENT_TOKEN);
        }
        editor.commit();
    }


    /**
     * Updates the saved session credentials.
     *
     * @param orgId the Organization Id to save.
     */
    public void updateSessionOrgID(String orgId) {

        Editor editor = this.sharedPrefs.edit();
        if ((orgId != null) && (orgId.length() > 0)) {
            editor.putString(PREFS_SESSION_ORG_ID, orgId);
        } else {
            editor.remove(PREFS_SESSION_ORG_ID);
        }
        editor.commit();
    }

    /**
     * Updates the saved session credentials.
     *
     * @param orgName the Organization Id to save.
     */
    public void updateSessionOrgName(String orgName) {

        Editor editor = this.sharedPrefs.edit();
        if ((orgName != null) && (orgName.length() > 0)) {
            editor.putString(PREFS_SESSION_ORG_NAME, orgName);
        } else {
            editor.remove(PREFS_SESSION_ORG_NAME);
        }
        editor.commit();
    }

    /**
     * Updates installed date time .
     *
     * @param dateTime date and time string.
     */
    public void updateSessionAppInstalledDateTime(String dateTime) {

        Editor editor = this.sharedPrefs.edit();
        if ((dateTime != null) && (dateTime.length() > 0)) {
            editor.putString(PREFS_SESSION_INSTALLED_DATE_TIME, dateTime);
        } else {
            editor.remove(PREFS_SESSION_INSTALLED_DATE_TIME);
        }
        editor.commit();
    }

    /**
     * Updates user active status .
     *
     * @param isActive true/false status.
     */
    public void updateSessionAppIsActiveUser(boolean isActive) {
        Editor editor = this.sharedPrefs.edit();
        editor.putBoolean(PREFS_SESSION_IS_USER_ACTIVE, isActive);
        editor.commit();
    }

    /**
     * Updates Screen cast active status .
     *
     * @param isActive true/false status.
     */
    public void updateSessionAppIsScreenCastActive(boolean isActive) {
        Editor editor = this.sharedPrefs.edit();
        editor.putBoolean(PREFS_SESSION_IS_SCREEN_CAST_ACTIVE, isActive);
        editor.commit();
    }

    /**
     * Updates the saved session credentials.
     *
     * @param subjectData the Subject_Course Data to save.
     */
   /* public void updateSessionSubjectCourseData(SubjectData subjectData) {
        Editor editor = this.sharedPrefs.edit();
        try {
            if ((subjectData != null)) {
                editor.putString(PREFS_SESSION_SUB_COURSE_DATA, ObjectSerializer.serialize(subjectData));
            } else {
                editor.remove(PREFS_SESSION_SUB_COURSE_DATA);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }*/

    /**
     * Updates the saved session credentials.
     *
     * @param loggedUserData to save.
     */


    public void updateLoggedUserData(UserData loggedUserData) {
        Editor editor = this.sharedPrefs.edit();
        try {
            if ((loggedUserData != null)) {
                editor.putString(PREFS_SESSION_LOGGED_USERDATA, ObjectSerializer.serialize(loggedUserData));
            } else {
                editor.remove(PREFS_SESSION_LOGGED_USERDATA);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    //public void updateLoggedVizippUserData(VizippUserData loggedVizippUserData) {
    public void updateLoggedKnoledgebrixUserData(KnowledgeBrixUserData loggedknowledgeBrixUserData)
    {
        Editor editor = this.sharedPrefs.edit();
        try {
            if ((loggedknowledgeBrixUserData != null)) {
                editor.putString(PREFS_SESSION_LOGGED_VIZIPP_USERDATA, ObjectSerializer.serialize(loggedknowledgeBrixUserData));
            } else {
                editor.remove(PREFS_SESSION_LOGGED_VIZIPP_USERDATA);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    public void updateSessionVibratePhoneState(boolean flag) {
        Editor editor = this.sharedPrefs.edit();
        editor.putBoolean(PREF_SESSION_VIBRATE_PHONE_STATE, flag);
        editor.commit();
    }

    public boolean getSessionVibratePhoneState() {
        return this.sharedPrefs.getBoolean(PREF_SESSION_VIBRATE_PHONE_STATE, false);
    }

    public void addUpdateSkipWalkthroughPref(boolean flag) {
        Editor editor = this.sharedPrefs.edit();
        editor.putBoolean(PREF_SESSION_WALKTHROUGH_SKIP_KEY, flag);
        editor.commit();
    }

    public boolean getWalkthroughSkipValue() {
        return this.sharedPrefs.getBoolean(PREF_SESSION_WALKTHROUGH_SKIP_KEY, false);
    }

    public boolean getInfoDialogSeenStatus() {
        return this.sharedPrefs.getBoolean(PREF_SESSION_INFORMATION_DIALOG_SEEN, false);
    }

    public void updateInfoDialogSeenStatus(boolean flag) {
        Editor editor = this.sharedPrefs.edit();
        editor.putBoolean(PREF_SESSION_INFORMATION_DIALOG_SEEN, flag);
        editor.commit();
    }

    public void updateDriveConnectedFlag(boolean flag) {
        Editor editor = this.sharedPrefs.edit();
        editor.putBoolean(PREF_SESSION_DRIVE_FLAG, flag);
        editor.commit();
    }

    public boolean getDriveConnected() {
        return this.sharedPrefs.getBoolean(PREF_SESSION_DRIVE_FLAG, false);
    }

    public void updateVideoUploadCount(int count) {
        Editor editor = this.sharedPrefs.edit();
        editor.putInt(PREF_SESSION_VIDEO_UPLOAD_COUNT, count);
        editor.commit();
    }

    public int getVideoUploadCount() {
        return this.sharedPrefs.getInt(PREF_SESSION_VIDEO_UPLOAD_COUNT, 0);
    }

    /**
     * Clears stored session credentials if any.
     */
    public void clearSessionCredentials() {
//        this.updateSessionCredentials(null, null, null);
        this.updateSessionPassword(null);
        this.updateSessionOrgID(null);
        this.updateSessionProfileToken(null);
        this.updateSessionDeviceToken(null);
//        this.updateLoggedUserList(null);
//        this.updateLoggedUsersCredentials(null);
        this.updateLoggedUserData(null);
//        this.addUpdateSkipWalkthroughPref(false);
//        this.updateSessionSubjectCourseData(null);
////        this.updateAppUpdatesPref(false);
    }

    /**
     * Clears stored session of Vizipp User credentials if any.
     */
    public void clearSessionVizippCredentials() {
        this.updateLoggedKnoledgebrixUserData(null);
        this.updateSessionVizippToken(null);
        this.updateSessionVizippUsername(null);
        this.updateSessionVizippPassword(null);
    }


    public void updateLoggedUserList(ArrayList<UserData> loggedUserList) {
        Editor editor = this.sharedPrefs.edit();
        try {
            if ((loggedUserList != null) && (loggedUserList.size() > 0)) {
                editor.putString(PREFS_SESSION_LOGGEDUSERSLIST, ObjectSerializer.serialize(loggedUserList));
            } else {
                editor.remove(PREFS_SESSION_LOGGEDUSERSLIST);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    public void updateLoggedUsersCredentials(HashMap<String, String> usercredentialHashMap) {
        Editor editor = this.sharedPrefs.edit();
        try {
            if ((usercredentialHashMap != null) && (usercredentialHashMap.size() > 0)) {
                editor.putString(PREFS_SESSION_LOGGEDUSERSCREDENTIALSLIST, ObjectSerializer.serialize(usercredentialHashMap));
            } else {
                editor.remove(PREFS_SESSION_LOGGEDUSERSCREDENTIALSLIST);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    public void updateRecordedSlideSession(HashMap<String, String> recordedSlideDetailHashMap) {
        Editor editor = this.sharedPrefs.edit();
        try {
            if ((recordedSlideDetailHashMap != null) && (recordedSlideDetailHashMap.size() > 0)) {
                editor.putString(PREFS_SESSION_RECORDED_SLIDE_DETAILS, ObjectSerializer.serialize(recordedSlideDetailHashMap));
            } else {
                editor.remove(PREFS_SESSION_RECORDED_SLIDE_DETAILS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    public void updateAppUpdatesPref(boolean flag) {
        Editor editor = this.sharedPrefs.edit();
        editor.putBoolean(PREF_SESSION_APPUPDATE_SKIP_KEY, flag);
        editor.commit();
    }

    public boolean getAppUpdatesPrefValue() {
        return this.sharedPrefs.getBoolean(PREF_SESSION_APPUPDATE_SKIP_KEY, false);
    }

    /**
     * Updates the saved session credentials.
     *
     * @param username the username to save.
     * @param password the password to save.
     */
    public void updateSessionCredentials(String username, String password, String profileToken) {

        Editor editor = this.sharedPrefs.edit();
        if ((username != null) && (username.length() > 0)) {
            editor.putString(PREFS_SESSION_USERNAME, username);
        } else {
            editor.remove(PREFS_SESSION_USERNAME);
        }

        if ((password != null) && (password.length() > 0)) {
            editor.putString(PREFS_SESSION_PASSWORD, password);
        } else {
            editor.remove(PREFS_SESSION_PASSWORD);
        }

        if ((profileToken != null) && (profileToken.length() > 0)) {
            editor.putString(PREFS_SESSION_PROFILE_TOKEN, profileToken);
        } else {
            editor.remove(PREFS_SESSION_PROFILE_TOKEN);
        }

        editor.commit();
    }

    public void updateVideoRecordCount(int count) {
        Editor editor = this.sharedPrefs.edit();
        editor.putInt(PREF_SESSION_VIDEO_RECORD_COUNT, count);
        editor.commit();
    }

    public int getVideoRecordCount() {
        return this.sharedPrefs.getInt(PREF_SESSION_VIDEO_RECORD_COUNT, 0);
    }

    public void updateSessionProfileImageBase64(String base64String) {

        Editor editor = this.sharedPrefs.edit();
        if ((base64String != null) && (base64String.length() > 0)) {
            editor.putString(PREFS_SESSION_PROFILE_IMAGE_BASE64, base64String);
        } else {
            editor.remove(PREFS_SESSION_PROFILE_IMAGE_BASE64);
        }
        editor.commit();
    }

    public void updateUserId(String id) {
        Editor editor = this.sharedPrefs.edit();
        if ((id != null) && (id.length() > 0)) {
            editor.putString(PREFS_SESSION_USER_ID, id);
        } else {
            editor.remove(PREFS_SESSION_USER_ID);
        }
        editor.commit();
    }

    public String getUserId(){
        return this.sharedPrefs.getString(PREFS_SESSION_USER_ID, "");
    }

    public void updateUserType(String userType) {
        Editor editor = this.sharedPrefs.edit();
        if ((userType != null) && (userType.length() > 0)) {
            editor.putString(PREFS_SESSION_USER_TYPE, userType);
        } else {
            editor.remove(PREFS_SESSION_USER_TYPE);
        }
        editor.commit();
    }

    public String getUserType(){
        return this.sharedPrefs.getString(PREFS_SESSION_USER_TYPE, "");
    }

    /**
     * Gets the session's user profile image base64.
     *
     * @return
     */
    public String getPrefsSessionProfileImageBase64() {
        return this.sharedPrefs.getString(PREFS_SESSION_PROFILE_IMAGE_BASE64, "");
    }


    public ArrayList<UserData> getLoggedUsersList() {
        try {
            return (ArrayList<UserData>) ObjectSerializer.deserialize(this.sharedPrefs.getString(PREFS_SESSION_LOGGEDUSERSLIST, ObjectSerializer.serialize(new ArrayList<UserData>())));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<String, String> getLoggedUsersCredentialsHashMap() {
        try {
            return (HashMap<String, String>) ObjectSerializer.deserialize(this.sharedPrefs.getString(PREFS_SESSION_LOGGEDUSERSCREDENTIALSLIST, ObjectSerializer.serialize(new HashMap<String, String>())));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<String, String> getRecordedSlideSessionHashMap() {
        try {
            return (HashMap<String, String>) ObjectSerializer.deserialize(this.sharedPrefs.getString(PREFS_SESSION_RECORDED_SLIDE_DETAILS, ObjectSerializer.serialize(new HashMap<String, String>())));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }



}
