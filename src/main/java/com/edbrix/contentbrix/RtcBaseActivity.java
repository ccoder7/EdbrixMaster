package com.edbrix.contentbrix;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import com.edbrix.contentbrix.baseclasses.AgoraBaseActivity;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.rtc.EventHandler;
import com.edbrix.contentbrix.utils.SessionManager;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public abstract class RtcBaseActivity extends BaseActivity implements EventHandler {
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        registerRtcEventHandler(this);
        configVideo();
        joinChannel();

    }

    private void configVideo() {
        VideoEncoderConfiguration configuration = new VideoEncoderConfiguration(
                Constants.VIDEO_DIMENSIONS[config().getVideoDimenIndex()],
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        );
        configuration.mirrorMode = Constants.VIDEO_MIRROR_MODES[config().getMirrorEncodeIndex()];
        rtcEngine().setVideoEncoderConfiguration(configuration);
    }

    private void joinChannel() {
        int status=0;
        // Initialize token, extra info here before joining channel
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name and uid that
        // you use to generate this token.
        //String token = getString(R.string.agora_access_token); older one of demo code commented on 06/07/2020
        String token = config().getmChannelToken();
        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
            token = null; // default, no token
        }
        Log.d("TAG", ""+status);
        Log.d("TAG ChannelName", ""+config().getChannelName());
        Log.d("TAG token", ""+token);
        Log.d("TAG userId ", ""+29105);
        Log.d("TAG app_id ", ""+ getString(R.string.agora_app_id));
        Log.d("TAG app_certificate ", ""+getString(R.string.agora_app_certificate));
        status=rtcEngine().joinChannel(token, config().getChannelName(), "", Integer.parseInt(sessionManager.getUserId()));//

    }

    protected SurfaceView prepareRtcVideo(int uid, boolean local) {
        // Render local/remote video on a SurfaceView

        SurfaceView surface = RtcEngine.CreateRendererView(getApplicationContext());
        if (local) {
            rtcEngine().setupLocalVideo(
                    new VideoCanvas(
                            surface,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            0,
                            Constants.VIDEO_MIRROR_MODES[config().getMirrorLocalIndex()]
                    )
            );
        } else {
            rtcEngine().setupRemoteVideo(
                    new VideoCanvas(
                            surface,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            uid,
                            Constants.VIDEO_MIRROR_MODES[config().getMirrorRemoteIndex()]
                    )
            );
        }
        return surface;
    }

    protected void removeRtcVideo(int uid, boolean local) {
        if (local) {
            rtcEngine().setupLocalVideo(null);
        } else {
            rtcEngine().setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeRtcEventHandler(this);
        rtcEngine().leaveChannel();
    }
}
