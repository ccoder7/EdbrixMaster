package com.edbrix.contentbrix;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.data.FileData;

public class VideoPlayerActivity extends BaseActivity implements EasyVideoCallback {

    EasyVideoPlayer player;
    String fileName;
    FileData fData;
    String TAG = VideoPlayerActivity.this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // stop screen rotation on phones
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        setContentView(R.layout.activity_video_player);
        fData = (FileData) getIntent().getSerializableExtra("FileData");
        Log.d("fData : ",fData.getFileObject().getAbsolutePath());
        // Grabs a reference to the player view
        player = (EasyVideoPlayer) findViewById(R.id.player);

        // Sets the callback to this Activity, since it inherits EasyVideoCallback
        player.setCallback(this);

        // Sets the source to the HTTP URL held in the TEST_URL variable.
        // To play files, you can use Uri.fromFile(new File("..."))
//        player.setSource(Uri.parse("https://cdn.video.playwire.com/1010450/videos/5449631/video-sd.mp4"));
//        https://youtu.be/FWh8wtT7DX0
//        player.setSource(Uri.parse("https://youtu.be/FWh8wtT7DX0"));
        player.setSource(Uri.fromFile(fData.getFileObject()));
        Log.e("file url object : ",""+Uri.fromFile(fData.getFileObject()));
        Log.e("file url : ",""+Uri.fromFile(fData.getFileObject()).getPath().toString());

        player.setBottomLabelText(fData.getFileName());

        // From here, the player view will show a progress indicator until the player is prepared.
        // Once it's prepared, the progress indicator goes away and the controls become enabled for the user to begin playback.

    }

    @Override
    public void onStarted(EasyVideoPlayer player) {

    }

    @Override
    public void onPaused(EasyVideoPlayer player) {

    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {

    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {

    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {

    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {

    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {

    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {

    }
}
