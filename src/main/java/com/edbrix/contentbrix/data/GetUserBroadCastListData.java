package com.edbrix.contentbrix.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GetUserBroadCastListData implements Serializable {

    @SerializedName("BroadcastId")
    @Expose
    private String broadcastId;
    @SerializedName("Title")
    @Expose
    private String title;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("ChannelToken")
    @Expose
    private String channelToken;
    @SerializedName("ChatUserToken")
    @Expose
    private String chatUserToken;
    @SerializedName("CreatedDate")
    @Expose
    private String createdDate;
    @SerializedName("DateName")
    @Expose
    private String dateName;
    @SerializedName("MuxStreamId")
    @Expose
    private String muxStreamId;
    @SerializedName("MuxStreamKey")
    @Expose
    private String muxStreamKey;
    @SerializedName("MuxPlaybackId")
    @Expose
    private String muxPlaybackId;
    private final static long serialVersionUID = -4779504315086588975L;

    public String getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(String broadcastId) {
        this.broadcastId = broadcastId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChannelToken() {
        return channelToken;
    }

    public void setChannelToken(String channelToken) {
        this.channelToken = channelToken;
    }

    public String getChatUserToken() {
        return chatUserToken;
    }

    public void setChatUserToken(String chatUserToken) {
        this.chatUserToken = chatUserToken;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getDateName() {
        return dateName;
    }

    public void setDateName(String dateName) {
        this.dateName = dateName;
    }

    public String getMuxStreamId() {
        return muxStreamId;
    }

    public void setMuxStreamId(String muxStreamId) {
        this.muxStreamId = muxStreamId;
    }

    public String getMuxStreamKey() {
        return muxStreamKey;
    }

    public void setMuxStreamKey(String muxStreamKey) {
        this.muxStreamKey = muxStreamKey;
    }

    public String getMuxPlaybackId() {
        return muxPlaybackId;
    }

    public void setMuxPlaybackId(String muxPlaybackId) {
        this.muxPlaybackId = muxPlaybackId;
    }
}
