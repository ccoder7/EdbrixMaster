package com.edbrix.contentbrix.baseclasses;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.coremedia.iso.boxes.Container;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.BuildConfig;
import com.edbrix.contentbrix.ContentBrixApp;
import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.VideoPlayerActivity;
import com.edbrix.contentbrix.VizippDrawLogin;
import com.edbrix.contentbrix.app.Config;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.commons.DialogManager;
import com.edbrix.contentbrix.commons.GlobalMethods;
import com.edbrix.contentbrix.commons.ToastMessage;
import com.edbrix.contentbrix.data.FileData;
import com.edbrix.contentbrix.rtc.EngineConfig;
import com.edbrix.contentbrix.rtc.EventHandler;
import com.edbrix.contentbrix.stats.StatsManager;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.utils.WindowUtil;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.firebase.messaging.FirebaseMessaging;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;


/**
 * Created by rajk
 */
public class BaseActivity extends AppCompatActivity implements EventHandler {

 /*
  Knowledgebrix Stripe
_______________________________________________

TEST

Publishable key:  pk_test_eNDtGrmLzZTDLmYJtR0Jl27Q
Secret key:   sk_test_wlFRrundUG34Ss9XxAGtrsFN

LIVE
Publishable key:  pk_live_V1cfcweFjmNawcYXh7CO0tq0
Secret key: sk_live_OyM0t13EeaieecLsOT7lTdlX
_______________________________________________

  */

    //    TEST Key
//    private static final String stripePublishableKey = "pk_test_eNDtGrmLzZTDLmYJtR0Jl27Q";

    //    LIVE Key
    private static final String stripePublishableKey = "pk_live_V1cfcweFjmNawcYXh7CO0tq0";

    private DialogManager dialogManager;

    private GlobalMethods globalMethods;

    protected Context mContext;

    private ToastMessage toastMessage;

    protected OnFragmentBackPressedListener onFragmentBackPressedListener;

    protected OnVideoProcessingListener onVideoProcessingListener;

    protected ImageLoader imageLoaderNostra = ImageLoader.getInstance();

    private LocalBroadcastManager localBroadcastManager;

    private SessionManager sessionManager;

    public static final int PICKFILE_RESULT_CODE = 1;

    public static final int PICKIMAGES_RESULT_CODE = 2;

    public static final int REQUEST_CAMERA = 3;


    private final static String OPR_BIND = "bind";
    private final static String OPR_WATERMARK_ADD = "watermark";
    private final static String OPR_CONCATENATE = "concatenate";
    private final static String OPR_VERSION = "version";
    private final static String OPR_TS = "ts";
    private final static String OPR_ALBUM = "album";
    public static Boolean IsSkipPassword = false;

    protected DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    protected int mStatusBarHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogManager = new DialogManager(this);
        globalMethods = new GlobalMethods();
        toastMessage = new ToastMessage(this);

        mContext = this;

        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        localBroadcastManager.registerReceiver(mRandomNumberReceiver, new IntentFilter("BROADCAST_RANDOM_NUMBER"));
        sessionManager = new SessionManager(BaseActivity.this);

        //WindowUtil.hideWindowStatusBar(getWindow());
        setGlobalLayoutListener();
        getDisplayMetrics();
        initStatusBarHeight();
    }

    private void setGlobalLayoutListener() {
        final View layout = findViewById(Window.ID_ANDROID_CONTENT);
        ViewTreeObserver observer = layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                onGlobalLayoutCompleted();
            }
        });
    }

    /**
     * Give a chance to obtain view layout attributes when the
     * content view layout process is completed.
     * Some layout attributes will be available here but not
     * in onCreate(), like measured width/height.
     * This callback will be called ONLY ONCE before the whole
     * window content is ready to be displayed for first time.
     */
    protected void onGlobalLayoutCompleted() {

    }

    private void getDisplayMetrics() {
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    private void initStatusBarHeight() {
        mStatusBarHeight = WindowUtil.getSystemStatusBarHeight(this);
    }

    protected ContentBrixApp application() {
        return (ContentBrixApp) getApplication();
    }

    protected RtcEngine rtcEngine() {
        return application().rtcEngine();
    }

    protected EngineConfig config() {
        return application().engineConfig();
    }

    protected StatsManager statsManager() { return application().statsManager(); }

    protected void registerRtcEventHandler(EventHandler handler) {
        application().registerEventHandler(handler);
    }

    protected void removeRtcEventHandler(EventHandler handler) {
        application().removeEventHandler(handler);
    }

    /**
     * Occurs when the first remote video frame is received and decoded.
     * This callback is triggered in either of the following scenarios:
     *
     *     The remote user joins the channel and sends the video stream.
     *     The remote user stops sending the video stream and re-sends it after 15 seconds. Possible reasons include:
     *         The remote user leaves channel.
     *         The remote user drops offline.
     *         The remote user calls the muteLocalVideoStream method.
     *         The remote user calls the disableVideo method.
     *
     * @param uid User ID of the remote user sending the video streams.
     * @param width Width (pixels) of the video stream.
     * @param height Height (pixels) of the video stream.
     * @param elapsed Time elapsed (ms) from the local user calling the joinChannel method until this callback is triggered.
     */
    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {

    }

    /**
     * Occurs when the local user joins a specified channel.
     *
     * The channel name assignment is based on channelName specified in the joinChannel method.
     *
     * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
     *
     * @param channel Channel name.
     * @param uid User ID.
     * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered.
     */
    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

    }

    /**
     *Occurs when a user leaves the channel.
     *
     * When the app calls the leaveChannel method, the SDK uses this callback to notify the app when the user leaves the channel.
     *
     * With this callback, the application retrieves the channel information, such as the call duration and statistics.
     *
     * @param stats Statistics of the call: RtcStats
     */
    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {

    }

    /**
     * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
     *
     * There are two reasons for users to become offline:
     *
     *     Leave the channel: When the user/host leaves the channel, the user/host sends a goodbye message. When this message is received, the SDK determines that the user/host leaves the channel.
     *     Drop offline: When no data packet of the user or host is received for a certain period of time (20 seconds for the communication profile, and more for the live broadcast profile), the SDK assumes that the user/host drops offline. A poor network connection may lead to false detections, so we recommend using the Agora RTM SDK for reliable offline detection.
     *
     * @param uid ID of the user or host who leaves the channel or goes offline.
     * @param reason Reason why the user goes offline:
     *
     *     USER_OFFLINE_QUIT(0): The user left the current channel.
     *     USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
     *     USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
     */
    @Override
    public void onUserOffline(int uid, int reason) {

    }

    /**
     * Occurs when a remote user (Communication)/host (Live Broadcast) joins the channel.
     *
     *     Communication profile: This callback notifies the app when another user joins the channel. If other users are already in the channel, the SDK also reports to the app on the existing users.
     *     Live Broadcast profile: This callback notifies the app when the host joins the channel. If other hosts are already in the channel, the SDK also reports to the app on the existing hosts. We recommend having at most 17 hosts in a channel
     *
     * The SDK triggers this callback under one of the following circumstances:
     *
     *     A remote user/host joins the channel by calling the joinChannel method.
     *     A remote user switches the user role to the host by calling the setClientRole method after joining the channel.
     *     A remote user/host rejoins the channel after a network interruption.
     *     The host injects an online media stream into the channel by calling the addInjectStreamUrl method.
     *
     * @param uid ID of the user or host who joins the channel.
     * @param elapsed Time delay (ms) from the local user calling joinChannel/setClientRole until this callback is triggered.
     */
    @Override
    public void onUserJoined(int uid, int elapsed) {

    }

    /**
     * Reports the last mile network quality of the local user once every two seconds before the user joins the channel. Last mile refers to the connection between the local device and Agora's edge server. After the application calls the enableLastmileTest method, this callback reports once every two seconds the uplink and downlink last mile network conditions of the local user before the user joins the channel.
     * @param quality The last mile network quality based on the uplink and dowlink packet loss rate and jitter:
     *
     *     QUALITY_UNKNOWN(0): The quality is unknown.
     *     QUALITY_EXCELLENT(1): The quality is excellent.
     *     QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
     *     QUALITY_POOR(3): Users can feel the communication slightly impaired.
     *     QUALITY_BAD(4): Users can communicate not very smoothly.
     *     QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
     *     QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
     *     QUALITY_DETECTING(8): The SDK is detecting the network quality.
     */
    @Override
    public void onLastmileQuality(final int quality) {

    }

    /**
     * Reports the last-mile network probe result.
     * The SDK triggers this callback within 30 seconds after the app calls the startLastmileProbeTest method.
     * @param result The uplink and downlink last-mile network probe test result. For details, see LastmileProbeResult.
     */
    @Override
    public void onLastmileProbeResult(final IRtcEngineEventHandler.LastmileProbeResult result) {

    }

    /**
     * Reports the statistics of the local video streams.
     *
     * The SDK triggers this callback once every two seconds for each user/host. If there are multiple users/hosts in the channel, the SDK triggers this callback as many times.
     *
     * @param stats The statistics of the local video stream. See LocalVideoStats.
     */
    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {

    }

    /**
     * Reports the statistics of the RtcEngine once every two seconds.
     * @param stats RTC engine statistics: RtcStats.
     */
    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {

    }

    /**
     * Reports the last mile network quality of each user in the channel once every two seconds.
     *
     * Last mile refers to the connection between the local device and Agora's edge server. This callback reports once every two seconds the last mile network conditions of each user in the channel. If a channel includes multiple users, then this callback will be triggered as many times.
     *
     * @param uid User ID. The network quality of the user with this uid is reported. If uid is 0, the local network quality is reported.
     * @param txQuality Uplink transmission quality of the user in terms of the transmission bitrate, packet loss rate, average RTT (Round-Trip Time) and jitter of the uplink network. txQuality is a quality rating helping you understand how well the current uplink network conditions can support the selected VideoEncoderConfiguration. For example, a 1000 Kbps uplink network may be adequate for video frames with a resolution of 680 × 480 and a frame rate of 30 fps, but may be inadequate for resolutions higher than 1280 × 720.
     *
     *     QUALITY_UNKNOWN(0): The quality is unknown.
     *     QUALITY_EXCELLENT(1): The quality is excellent.
     *     QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
     *     QUALITY_POOR(3): Users can feel the communication slightly impaired.
     *     QUALITY_BAD(4): Users can communicate not very smoothly.
     *     QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
     *     QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
     *     QUALITY_DETECTING(8): The SDK is detecting the network quality.
     *
     * @param rxQuality Downlink network quality rating of the user in terms of packet loss rate, average RTT, and jitter of the downlink network.
     *
     *     QUALITY_UNKNOWN(0): The quality is unknown.
     *     QUALITY_EXCELLENT(1): The quality is excellent.
     *     QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
     *     QUALITY_POOR(3): Users can feel the communication slightly impaired.
     *     QUALITY_BAD(4): Users can communicate not very smoothly.
     *     QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
     *     QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
     *     QUALITY_DETECTING(8): The SDK is detecting the network quality.
     */
    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {

    }

    /**
     * Reports the statistics of the video stream from each remote user/host. The SDK triggers this callback once every two seconds for each remote user/host. If a channel includes multiple remote users, the SDK triggers this callback as many times.
     * @param stats Statistics of the received remote video streams: RemoteVideoStats.
     */
    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {

    }

    /**
     * Reports the statistics of the audio stream from each remote user/host.
     * The SDK triggers this callback once every two seconds for each remote user/host. If a channel includes multiple remote users, the SDK triggers this callback as many times.
     *
     * Schemes such as FEC (Forward Error Correction) or retransmission counter the frame loss rate. Hence, users may find the overall audio quality acceptable even when the packet loss rate is high.
     *
     * @param stats Statistics of the received remote audio streams: RemoteAudioStats.
     */
    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {

    }

    @Override
    public void onError(int errorCode) {
        Toast.makeText(getApplicationContext(),"error code = "+errorCode,Toast.LENGTH_LONG).show();
    }


    /* agora code end*/

    @Override
    protected void onResume() {
        super.onResume();
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        //temporary code added for Limited edition build for session time out after 7 days
        compareCurrentDateWithInstalledDate();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    protected void showToast(String msg, int timeDuration) {
        toastMessage.showToastMsg(msg, timeDuration);
    }

    protected void showToast(String msg) {
        toastMessage.showToastMsg(msg, Toast.LENGTH_LONG);
    }

    protected void cancelToast() {
        toastMessage.cancelToast();
    }

    protected AlertDialogManager getAlertDialogManager() {
        return dialogManager.getAlertDialogManager();
    }

    protected GlobalMethods getGlobalMethods() {
        return globalMethods;
    }

    protected void showBusyProgress() {
        dialogManager.showBusyProgress();
    }

    protected void showBusyProgress(String message) {
        dialogManager.showBusyProgress(message);
    }

    protected void hideBusyProgress() {
        dialogManager.hideBusyProgress();
    }

    public interface OnVideoProcessingListener {
        public void onVideoProcessCompleted();
        public void onVideoProcessFailed();
    }

    public interface OnFragmentBackPressedListener {
        public void doBack();
    }

    public void setOnVideoProcessingListener(OnVideoProcessingListener onVideoProcessingListener) {
        this.onVideoProcessingListener = onVideoProcessingListener;
    }

    public void setOnFragmentBackPressedListener(OnFragmentBackPressedListener onFragmentBackPressedListener) {
        this.onFragmentBackPressedListener = onFragmentBackPressedListener;
    }

    protected GoogleApiClient startGoogleClientAuthentication(GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();

        return googleApiClient;
    }

    protected SessionManager getSessionManager() {
        if (sessionManager != null) {
            return sessionManager;
        } else {
            sessionManager = new SessionManager(BaseActivity.this);
            return sessionManager;
        }
    }

    // Initialize a new BroadcastReceiver instance
    protected BroadcastReceiver mRandomNumberReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the received random number

            // Display a notification that the broadcast received
            showToast("Video processed successfully...", Toast.LENGTH_SHORT);
        }
    };

    protected void forceCrash(View view) {
        throw new RuntimeException("This is a test crash for Fabric");
    }

    protected boolean isTablet() {
        boolean xlarge = ((this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_XLARGE);
        boolean large = ((this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    protected boolean isChromebook() {
        boolean isChromebook = this.getPackageManager().hasSystemFeature("org.chromium.arc.device_management");
        if (isChromebook) {
            return true;
        }
        return false;
    }

    protected int getScreenOrientation() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getRotation();
    }

    protected String getStripePublishableKey() {
        return stripePublishableKey;
    }

    protected BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // checking for type intent filter
            if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                // gcm successfully registered
                // now subscribe to `global` topic to receive app wide notifications
                FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                displayFirebaseRegId();

            } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                // new push notification is received

                String message = intent.getStringExtra("message");

//                showToast("Push notification: " + message+", Reg Id: "+sessionManager.getSessionFCMToken());

            }
        }
    };

    // Fetches reg id from shared preferences
    // and displays on the screen
    private void displayFirebaseRegId() {

        String regId =sessionManager.getSessionFCMToken();

//        Log.e("BaseActivity", "Firebase reg id: " + regId);

//        if (!TextUtils.isEmpty(regId))
//            showToast("Firebase Reg Id: " + regId);
//        else
//            showToast("Firebase Reg Id is not received yet!");

    }

    protected void openDocumentFolder() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.google.android.apps.docs/files/");
        intent.setDataAndType(uri, "application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Open folder"), PICKFILE_RESULT_CODE);
    }

    protected void openImagesFolder() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.google.android.apps.docs/files/");
        intent.setDataAndType(uri, "image/jpeg");
        startActivityForResult(Intent.createChooser(intent, "Open folder"), PICKIMAGES_RESULT_CODE);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        //ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //inImage.compress(Bitmap.CompressFormat.JPEG, 200, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    protected boolean copyInputStreamToFile(Uri uri, File file) {
        OutputStream out = null;
        InputStream in = null;

        try {
            in = getContentResolver().openInputStream(uri);
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            if (out != null) {
                out.close();
            }

            in.close();
            return true;

        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            return false;
        }
    }

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public class MergeVideosTask extends AsyncTask<String, Void, Boolean>
    {
        private String targetFile;
        private String tempSourceFiles[];

        @Override
        protected void onPreExecute() {
            showBusyProgress("Processing file...");
            //long mills = System.currentTimeMillis();
            //targetFile = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name).toString() + "/VD_" + mills + ".mp4";
            Date date = new Date();

            targetFile = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name).toString() + "/CB_" + date.getTime() + ".mp4";
        }

        @Override
        protected Boolean doInBackground(String... sourceFiles) {
            try {
                if(sourceFiles !=null && sourceFiles.length >0) {
                    tempSourceFiles = sourceFiles;
                    File tFile = new File(targetFile);
                    tFile.createNewFile();

                    List<Movie> listMovies = new ArrayList<>();
                    for (String filename : sourceFiles) {
                        listMovies.add(MovieCreator.build(filename));
                    }
                    List<Track> listVDTracks = new LinkedList<>();
                    List<Track> listADTracks = new LinkedList<>();
                    for (Movie movie : listMovies) {
                        for (Track track : movie.getTracks()) {
                            if (track.getHandler().equals("vide")) {
                                listVDTracks.add(track);
                            }
                            if (track.getHandler().equals("soun")) {
                                listADTracks.add(track);
                            }
                        }
                    }
                    Movie outputMovie = new Movie();
                    if (!listVDTracks.isEmpty()) {
                        outputMovie.addTrack(new AppendTrack(listVDTracks.toArray(new Track[listVDTracks.size()])));
                    }

                    if (!listADTracks.isEmpty()) {
                        outputMovie.addTrack(new AppendTrack(listADTracks.toArray(new Track[listADTracks.size()])));
                    }

                    Container container = new DefaultMp4Builder().build(outputMovie);
                    FileChannel fileChannel = new RandomAccessFile(String.format(targetFile), "rw").getChannel();
                    container.writeContainer(fileChannel);
                    fileChannel.close();
                    return true;
                }else{
                    Log.e("", "No files found in merging media files ");
                    return false;
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
                Log.e("", "Error merging media files. exception: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isVideoMerged) {
            hideBusyProgress();
            FileData fileData = null;
            if (isVideoMerged) {
//                showToast("Merged successfully...");
                if (tempSourceFiles != null && tempSourceFiles.length > 0) {
                    for (String fpath : tempSourceFiles) {
                        File file = new File(fpath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    onVideoProcessingListener.onVideoProcessCompleted();
                    fileData = new FileData(new File(targetFile));
                    buildNotification("Video Processing Done...",fileData);
                    //startActivity(new Intent(getApplicationContext(),MainActivity.class));

                    File sourceDir = new File(Environment.getExternalStorageDirectory(), "/CBtemp/");
                    if (sourceDir != null) {
                        File[] filenames = sourceDir.listFiles();
                        for (File tmpf : filenames) {
                            tmpf.delete();
                        }
                    }
                }
            }else{
                onVideoProcessingListener.onVideoProcessFailed();
                fileData = new FileData(new File(targetFile));
                buildNotification("Video Processing Failed...",fileData);
            }
        }
    }
    public void buildNotification(String msg, FileData fileData)
    {
        //Intent myIntent = new Intent(mContext, MainActivity.class);
        Intent myIntent = new Intent(mContext, VideoPlayerActivity.class);
        myIntent.putExtra("FileData", fileData);
        @SuppressLint("WrongConstant")
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, myIntent,Intent.FLAG_ACTIVITY_NEW_TASK);

        if(isTablet()){
            Notification notification = new Notification.Builder(mContext)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(msg)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setSmallIcon(R.drawable.app_logo_round_new24)
                    .build();

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Config.NOTIFICATION_ID, notification);
        }
        else {
            Notification notification = new Notification.Builder(mContext)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(msg)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setSmallIcon(R.drawable.app_logo_round_new24)
                    .build();

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Config.NOTIFICATION_ID, notification);
        }

    }

    public void compareCurrentDateWithInstalledDate(){
        try {
            if (!sessionManager.getSessionAppInstalledDateString().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date installedDate = dateFormat.parse(sessionManager.getSessionAppInstalledDateString());
                Date finishedDate = new Date(installedDate.getTime() + 604800000L); // 7 * 24 * 60 * 60 * 1000
                Date currentDate = Calendar.getInstance().getTime();
                boolean isSessionOut = currentDate.after(finishedDate);
                if(isSessionOut){
                    sessionManager.clearSessionVizippCredentials();
                    sessionManager.clearSessionCredentials();
                    sessionManager.updateSessionDropBoxToken(null);
                    sessionManager.updateSessionDropBoxDisplayName(null);
                    sessionManager.updateSessionAppInstalledDateTime("");
                    sessionManager.updateSessionAppIsActiveUser(false);
                    callLoginActivity();
                }
            }
        }catch (Exception e){
            Crashlytics.logException(e);
            Log.e("BaseActivity", "Exception found: " + e.getMessage());
        }
    }

    private void callLoginActivity() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(BaseActivity.this, VizippDrawLogin.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    protected void getProductVersion() {
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("ProductId", "9");
        requestMap.put("OSType","A");

        try {
            showBusyProgress();
            JsonObjectRequest getProductVersionRequest = new JsonObjectRequest(Request.Method.POST,  "http://services.edbrix.net/auth/getappversion", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v(BaseActivity.class.getName(), response.toString());
                    try {
                        if (response != null && response.has("Success")) {
                            if (response.getString("Success").equals("1")) {
                                float productVersion = Float.parseFloat(response.getString("ProductVersion"));
                                float currentAppVersion = Float.parseFloat(BuildConfig.VERSION_NAME);
                                if(currentAppVersion<productVersion){
                                    getAlertDialogManager().Dialog(getResources().getString(R.string.app_name),
                                            "New version is available on Google Play Store",
                                            "Update",
                                            "Cancel",
                                            true,
                                            new AlertDialogManager.onTwoButtonClickListner() {
                                                @Override
                                                public void onNegativeClick() {

                                                }

                                                @Override
                                                public void onPositiveClick() {
                                                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                                    try {
                                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                    } catch (android.content.ActivityNotFoundException anfe) {
                                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                    }
                                                }
                                            }).show();
                                }

                            } else { //{"Error":{"ErrorCode":"E1015","ErrorMessage":"You are not authorized user to use this service."}}

                                showToast("Something went wrong. Please try again later.");
                            }
                        }else{
                            showToast("Something went wrong. Please try again later.");
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        Log.v("Volley Excep", e.getMessage());
                        showToast("Exceptions : "+e.getMessage());
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideBusyProgress();
                    Log.v("Volley VError", VolleySingleton.getErrorMessage(error));
                    showToast(VolleySingleton.getErrorMessage(error));
                }

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

            };

            VolleySingleton.getInstance().addToRequestQueue(getProductVersionRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }

    }

    protected String doAudioVideoMuxing(String VideoSavePathInDevice, String AudioSavePathInDevice){
        return doAudioVideoMuxing(VideoSavePathInDevice,  AudioSavePathInDevice, false);
    }

    protected String doAudioVideoMuxing(String VideoSavePathInDevice, String AudioSavePathInDevice,boolean isTemp) {
        String mOutputPath = "";
        try {
                if(isTemp) {
                    File f = new File(Environment.getExternalStorageDirectory() + "/CBtemp");
                    if (!f.isDirectory()) {
                        f.mkdirs();
                    }
                    mOutputPath = Environment.getExternalStorageDirectory() + "/CBtemp/CB_" + new Date().getTime() + ".mp4";
                }else{
                    mOutputPath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name).toString() + "/CB_" + new Date().getTime() + ".mp4";
                }
            MediaExtractor videoExtractor = new MediaExtractor();
//            AssetFileDescriptor afdd = getAssets().openFd(VideoSavePathInDevice);
//            videoExtractor.setDataSource(afdd.getFileDescriptor(), afdd.getStartOffset(), afdd.getLength());
            videoExtractor.setDataSource(VideoSavePathInDevice);

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(AudioSavePathInDevice);

            Log.d("ContentBrix", "Video Extractor Track Count " + videoExtractor.getTrackCount());
            Log.d("ContentBrix", "Audio Extractor Track Count " + audioExtractor.getTrackCount());

            MediaMuxer muxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            int videoTrack = muxer.addTrack(videoFormat);

            audioExtractor.selectTrack(0);
            MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
            int audioTrack = muxer.addTrack(audioFormat);

            Log.d("ContentBrix", "Video Format " + videoFormat.toString());
            Log.d("ContentBrix", "Audio Format " + audioFormat.toString());

            boolean sawEOS = false;
            int frameCount = 0;
            int offset = 100;
            int sampleSize = 256 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            while (!sawEOS) {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    Log.d("ContentBrix", "saw input EOS.");
                    sawEOS = true;
                    videoBufferInfo.size = 0;

                } else {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();


                    frameCount++;
                    Log.d("ContentBrix", "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d("ContentBrix", "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

//            Toast.makeText(getApplicationContext(), "frame:" + frameCount, Toast.LENGTH_SHORT).show();


            boolean sawEOS2 = false;
            int frameCount2 = 0;
            while (!sawEOS2) {
                frameCount2++;

                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    Log.d("ContentBrix", "saw input EOS.");
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                } else {
                    audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                    audioBufferInfo.flags = audioExtractor.getSampleFlags();
                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                    audioExtractor.advance();


                    Log.d("ContentBrix", "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d("ContentBrix", "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

//            Toast.makeText(getApplicationContext(), "frame:" + frameCount2, Toast.LENGTH_SHORT).show();

            muxer.stop();
            muxer.release();

        } catch (IOException e) {
            Log.d("ContentBrix", "Mixer Error 1 " + e.getMessage());
            return null;
        } catch (Exception e) {
            Log.d("ContentBrix", "Mixer Error 2 " + e.getMessage());
            return null;
        }finally {
            //Delete Video file
            File file = new File(VideoSavePathInDevice);
            boolean deleted = file.delete();
            //Delete Audio file
            File files = new File(AudioSavePathInDevice);
            boolean deleted1 = files.delete();
        }
        return mOutputPath;

    }
}
