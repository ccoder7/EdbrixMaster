package com.edbrix.contentbrix;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.myscript_certificate.MyCertificate;
import com.edbrix.contentbrix.rtc.AgoraEventHandler;
import com.edbrix.contentbrix.rtc.EngineConfig;
import com.edbrix.contentbrix.rtc.EventHandler;
import com.edbrix.contentbrix.stats.StatsManager;
import com.edbrix.contentbrix.utils.FileUtil;
import com.edbrix.contentbrix.utils.PrefManager;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.myscript.iink.Engine;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.agora.rtc.RtcEngine;
import io.fabric.sdk.android.Fabric;


/**
 * Created by rajk on 21/09/17.
 */

public class ContentBrixApp extends Application {

    public static String APP_VERSION = "0.1";
    public static String ANDROID_ID = "0000000000000000";
    private static final String TAG = ContentBrixApp.class.getSimpleName();
    private SessionManager sessionManager;
    private static Engine engine;

    private RtcEngine mRtcEngine;
    private EngineConfig mGlobalConfig = new EngineConfig();
    private AgoraEventHandler mHandler = new AgoraEventHandler();
    private StatsManager mStatsManager = new StatsManager();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        VolleySingleton.createInstance(getApplicationContext());
        File yourAppStorageDir = new File(Environment.getExternalStorageDirectory(), "/" + getResources().getString(R.string.app_name) + "/");
        if (!yourAppStorageDir.exists()) {
            boolean isDirCreated = yourAppStorageDir.mkdirs();
            Log.d(TAG, "App mediaStorageDirectory created :" + isDirCreated);
        }

//        initImageLoader(getApplicationContext());


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar calendar = Calendar.getInstance();

        String dateString = dateFormat.format(calendar.getTime());
        //
        sessionManager = new SessionManager(this);
        sessionManager.updateSessionAppInstalledDateTime(dateString);
        sessionManager.updateSessionAppIsActiveUser(true);

        Log.d(TAG, "App Installed Date String :" + dateString);

        try {
            mRtcEngine = RtcEngine.create(getApplicationContext(), getString(R.string.agora_app_id), mHandler);
            // Sets the channel profile of the Agora RtcEngine.
            // The Agora RtcEngine differentiates channel profiles and applies different optimization algorithms accordingly. For example, it prioritizes smoothness and low latency for a video call, and prioritizes video quality for a video broadcast.
            mRtcEngine.setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            mRtcEngine.enableVideo();
            mRtcEngine.setLogFile(FileUtil.initializeLogFile(this));
        } catch (Exception e) {
            e.printStackTrace();
        }

        initConfig();

    }
    private void initConfig() {
        SharedPreferences pref = PrefManager.getPreferences(getApplicationContext());
        mGlobalConfig.setVideoDimenIndex(pref.getInt(
                Constants.PREF_RESOLUTION_IDX, Constants.DEFAULT_PROFILE_IDX));

        boolean showStats = pref.getBoolean(Constants.PREF_ENABLE_STATS, false);
        mGlobalConfig.setIfShowVideoStats(showStats);
        mStatsManager.enableStats(showStats);

        mGlobalConfig.setMirrorLocalIndex(pref.getInt(Constants.PREF_MIRROR_LOCAL, 0));
        mGlobalConfig.setMirrorRemoteIndex(pref.getInt(Constants.PREF_MIRROR_REMOTE, 0));
        mGlobalConfig.setMirrorEncodeIndex(pref.getInt(Constants.PREF_MIRROR_ENCODE, 0));
    }

    public static synchronized Engine getEngine()
    {
        if (engine == null) {
            engine = Engine.create(MyCertificate.getBytes());
        }
        return engine;
    }

    public EngineConfig engineConfig() {
        return mGlobalConfig;
    }

    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public StatsManager statsManager() {
        return mStatsManager;
    }

    public void registerEventHandler(EventHandler handler) {
        mHandler.addHandler(handler);
    }

    public void removeEventHandler(EventHandler handler) {
        mHandler.removeHandler(handler);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RtcEngine.destroy();
    }
}
