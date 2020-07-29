package com.edbrix.contentbrix;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edbrix.contentbrix.stats.LocalStatsData;
import com.edbrix.contentbrix.stats.RemoteStatsData;
import com.edbrix.contentbrix.stats.StatsData;
import com.edbrix.contentbrix.ui.VideoGridContainer;
import com.edbrix.contentbrix.utils.Constant;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.ss.ScreenSharingClient;
import io.agora.rtc.video.AgoraVideoFrame;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;


public class StartBroadcastActivity extends RtcBaseActivity {

    private static final String LOG_TAG = StartBroadcastActivity.class.getSimpleName();

    private FrameLayout mFlCam;
    private FrameLayout mFlSS;
    private boolean mSS = false;
    private VideoEncoderConfiguration mVEC;
    private ScreenSharingClient mSSClient;

    private VideoGridContainer mVideoGridContainer;
    private ImageView mMuteAudioBtn;
    private ImageView mMuteVideoBtn;
    MediaProjection projection;
    VirtualDisplay display;
    MediaProjectionManager projectManager;
    boolean isStartSharing = false;
    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    private static final int PROJECTION_REQ_CODE = 1 << 2;
    //private static final int DEFAULT_VIDEO_TYPE = ExternalVideoInputManager.TYPE_LOCAL_VIDEO;
    private static final int DEFAULT_SHARE_FRAME_RATE = 15;
    //private IExternalVideoInputService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_broadcast);

        //screen sharing
        mFlSS = (FrameLayout) findViewById(R.id.screen_share_preview);
        mSSClient = ScreenSharingClient.getInstance();
        mSSClient.setListener(mListener);

        initUI();
        initData();
        setupVideoProfile();
    }

    private void initUI() {
        TextView roomName = findViewById(R.id.live_room_name);
        roomName.setText(config().getChannelName());
        roomName.setSelected(true);

        initUserIcon();

        int role = getIntent().getIntExtra(
                com.edbrix.contentbrix.Constants.KEY_CLIENT_ROLE,
                io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE);
        boolean isBroadcaster =  (role == Constants.CLIENT_ROLE_BROADCASTER);

        mMuteVideoBtn = findViewById(R.id.live_btn_mute_video);
        mMuteVideoBtn.setActivated(isBroadcaster);

        mMuteAudioBtn = findViewById(R.id.live_btn_mute_audio);
        mMuteAudioBtn.setActivated(isBroadcaster);

        ImageView beautyBtn = findViewById(R.id.live_btn_beautification);
        beautyBtn.setActivated(true);
        rtcEngine().setBeautyEffectOptions(beautyBtn.isActivated(),
                com.edbrix.contentbrix.Constants.DEFAULT_BEAUTY_OPTIONS);

        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mVideoGridContainer.setStatsManager(statsManager());

        rtcEngine().setClientRole(role);
        if (isBroadcaster) startBroadcast();
    }

    private void initUserIcon() {
        Bitmap origin = BitmapFactory.decodeResource(getResources(), R.drawable.fake_user_icon);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), origin);
        drawable.setCircular(true);
        ImageView iconView = findViewById(R.id.live_name_board_icon);
        iconView.setImageDrawable(drawable);
    }

    private void initData() {
        mVideoDimension = com.edbrix.contentbrix.Constants.VIDEO_DIMENSIONS[
                config().getVideoDimenIndex()];
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout topLayout = findViewById(R.id.live_room_top_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.height = mStatusBarHeight + topLayout.getMeasuredHeight();
        topLayout.setLayoutParams(params);
        topLayout.setPadding(0, mStatusBarHeight, 0, 0);
    }

    private void startBroadcast() {
        try{
            rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            SurfaceView surface = prepareRtcVideo(0, true);
            mVideoGridContainer.addUserVideoSurface(0, surface, true);
            mMuteAudioBtn.setActivated(true);
        }catch (Exception e){
            Log.e("StartBroadcastActivity",e.getMessage());
        }
    }

    private void stopBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        removeRtcVideo(0, true);
        mVideoGridContainer.removeUserVideo(0, true);
        mMuteAudioBtn.setActivated(false);
    }


    @Override
    public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
        // Do nothing at the moment
       // Toast.makeText(this,"channel: "+channel+" uid: "+uid+" elapsed: "+elapsed,Toast.LENGTH_LONG).show();
        rtcEngine().addPublishStreamUrl("rtmps://global-live.mux.com:5222/app/"+config().getmMuxStreamKey(),false);///dbdfd405-a0d3-f8b1-a66f-057cf210f60a
        setupRemoteView(uid);

        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(uid == Constant.SCREEN_SHARE_UID) {
                    setupRemoteView(uid);
                }
            }
        });*/
    }

    @Override
    public void onError(int errorCode) {
        Toast.makeText(getApplicationContext(),"error :"+errorCode,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUserJoined(final int uid, int elapsed) {
        // Do nothing at the moment
        //screen sharing
        Log.d(LOG_TAG, "onUserJoined: " + (uid&0xFFFFFFL));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(uid == Constant.SCREEN_SHARE_UID) {
                    setupRemoteView(uid);
                }
            }
        });
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeRemoteUser(uid);
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                renderRemoteUser(uid);
            }
        });
    }

    private void renderRemoteUser(int uid) {
        SurfaceView surface = prepareRtcVideo(uid, false);
        mVideoGridContainer.addUserVideoSurface(uid, surface, false);
    }

    private void removeRemoteUser(int uid) {
        removeRtcVideo(uid, false);
        mVideoGridContainer.removeUserVideo(uid, false);
    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setWidth(mVideoDimension.width);
        data.setHeight(mVideoDimension.height);
        data.setFramerate(stats.sentFrameRate);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setLastMileDelay(stats.lastmileDelay);
        data.setVideoSendBitrate(stats.txVideoKBitRate);
        data.setVideoRecvBitrate(stats.rxVideoKBitRate);
        data.setAudioSendBitrate(stats.txAudioKBitRate);
        data.setAudioRecvBitrate(stats.rxAudioKBitRate);
        data.setCpuApp(stats.cpuAppUsage);
        data.setCpuTotal(stats.cpuAppUsage);
        data.setSendLoss(stats.txPacketLossRate);
        data.setRecvLoss(stats.rxPacketLossRate);
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();
        rtcEngine().removePublishStreamUrl("rtmps://global-live.mux.com:5222/app/"+config().getmMuxStreamKey());
    }

    public void onLeaveClicked(View view) {
        finish();
    }

    public void onSwitchCameraClicked(View view) {
        rtcEngine().switchCamera();
    }

    public void onBeautyClicked(View view) {
        view.setActivated(!view.isActivated());
        rtcEngine().setBeautyEffectOptions(view.isActivated(),
                com.edbrix.contentbrix.Constants.DEFAULT_BEAUTY_OPTIONS);
    }

    public void onMoreClicked(View view) {
        // Do nothing at the moment
    }

    public void onPushStreamClicked(View view) {
        //screen sharing
        if(mSS == false){

            mSSClient.start(getApplicationContext(), getResources().getString(R.string.agora_app_id), null,
                    config().getChannelName(), Constant.SCREEN_SHARE_UID, mVEC);
            //button.setText(getResources().getString(R.string.label_stop_sharing_your_screen));
            mSS = true;

        }else{
           /* projection.stop();
            isStartSharing = false;*/
            mSSClient.stop(getApplicationContext());
            //button.setText(getResources().getString(R.string.label_start_sharing_your_screen));
            mSS = false;
        }

    }

    public void onMuteAudioClicked(View view) {
        if (!mMuteVideoBtn.isActivated()) return;

        rtcEngine().muteLocalAudioStream(view.isActivated());
        view.setActivated(!view.isActivated());
    }

    public void onMuteVideoClicked(View view) {
        if (view.isActivated()) {
            stopBroadcast();
        } else {
            startBroadcast();
        }
        view.setActivated(!view.isActivated());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       /* DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        projection = projectManager.getMediaProjection(resultCode, data);*/

        //display = projection.createVirtualDisplay("Contentbrix", width, height, 720, 0, null, null, null);

        if (requestCode == PROJECTION_REQ_CODE && resultCode == RESULT_OK) {
         //   startScreenShare(data);
        }

    }

    // Screen Sharing
    private final ScreenSharingClient.IStateListener mListener = new ScreenSharingClient.IStateListener() {
        @Override
        public void onError(int error) {
            Log.e(LOG_TAG, "Screen share service error happened: " + error);
        }

        @Override
        public void onTokenWillExpire() {
            Log.d(LOG_TAG, "Screen share service token will expire");
            mSSClient.renewToken(null); // Replace the token with your valid token
        }
    };

    private void setupVideoProfile() {
        rtcEngine().setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        rtcEngine().enableVideo();
        mVEC = new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_1280x720,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT);
        rtcEngine().setVideoEncoderConfiguration(mVEC);
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
    }

    private void setupRemoteView(int uid) {
        SurfaceView ssV = RtcEngine.CreateRendererView(getApplicationContext());
        ssV.setZOrderOnTop(true);
        ssV.setZOrderMediaOverlay(true);
        mFlSS.addView(ssV, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rtcEngine().setupRemoteVideo(new VideoCanvas(ssV, VideoCanvas.RENDER_MODE_FIT, uid));
    }

//-------------------------------------------------------------------

}