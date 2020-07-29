package com.edbrix.contentbrix;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.utils.SessionManager;

public class ChooseAccountActivity extends BaseActivity {

    private LinearLayout loggedUserLayout;
    private LinearLayout changeUserLayout;
    private ImageView loggedUserImage;
    private TextView loggedUserName;
    private SessionManager sessionManager;
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTablet()) {
            // stop screen rotation on phones
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_choose_account);
        loggedUserLayout = (LinearLayout) findViewById(R.id.loggedUserLayout);
        changeUserLayout = (LinearLayout) findViewById(R.id.changeUserLayout);
        loggedUserImage = (ImageView) findViewById(R.id.loggedUserImage);
        loggedUserName = (TextView) findViewById(R.id.loggedUserName);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        sessionManager = new SessionManager(ChooseAccountActivity.this);

//        getGlobalMethods().setImageBase64(ChooseAccountActivity.this, sessionManager.getPrefsSessionProfileImageBase64(), loggedUserImage, R.drawable.default_user);

        loggedUserName.setText(sessionManager.getSessionUsername());

        loggedUserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        changeUserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLoginActivity();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * call Login Page for Edbrix Enterprise Login
     */
    private void callLoginActivity() {
        Intent loginEdbrixIntent = new Intent(ChooseAccountActivity.this, EdbrixLoginActivity.class);
        startActivityForResult(loginEdbrixIntent, RecordPreviewActivity.REQUEST_CODE_EDBRIX_LOGIN);
    }

    /**
     * Receiving activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == RecordPreviewActivity.REQUEST_CODE_EDBRIX_LOGIN) {
            if (resultCode == RESULT_OK) {
                if (sessionManager.hasSessionCredentials()) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        }
    }

}
