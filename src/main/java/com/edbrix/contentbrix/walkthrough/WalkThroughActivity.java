package com.edbrix.contentbrix.walkthrough;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.MainActivity;
import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.StartRecordActivity;
import com.edbrix.contentbrix.VizippDrawLogin;
import com.edbrix.contentbrix.utils.SessionManager;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;


/**
 * Created by rajk
 */
public class WalkThroughActivity extends FragmentActivity {

    private ViewPager mPager;
    private PageIndicator mIndicator;

    private WalkthroughAdapter mAdapter;
    private SessionManager managerSession;

    private RelativeLayout indicatorLayout;

    private TextView gotItText;
    private TextView nextText;
    private TextView skipText;

    private int itemCount;

    private Intent intent;

    private boolean isMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.walkthrough_layout);

            gotItText = (TextView) findViewById(R.id.gotidTextView);
            nextText = (TextView) findViewById(R.id.nextTextView);
            skipText = (TextView) findViewById(R.id.skipTextView);
            indicatorLayout = (RelativeLayout) findViewById(R.id.indicatorLayout);

            gotItText.setVisibility(View.GONE);

            intent = WalkThroughActivity.this.getIntent();
            if (intent != null)
                isMain = intent.getBooleanExtra("ismain", false);

            managerSession = new SessionManager(this);
            mPager = (ViewPager) findViewById(R.id.pager);
            mPager.setClipToPadding(false);
            mPager.setPadding(0, 100, 0, 0);
            mPager.setPageMargin(0);

            mAdapter = new WalkthroughAdapter(this);
            mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
            mPager.setAdapter(mAdapter);
            mIndicator.setViewPager(mPager);

            itemCount = mAdapter.getCount();


            mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if ((position + 1) == itemCount) {
                        indicatorLayout.setVisibility(View.INVISIBLE);
                        skipText.setVisibility(View.GONE);
                        nextText.setVisibility(View.GONE);
                        gotItText.setVisibility(View.VISIBLE);
                    } else {
                        indicatorLayout.setVisibility(View.VISIBLE);
                        skipText.setVisibility(View.VISIBLE);
                        nextText.setVisibility(View.VISIBLE);
                        gotItText.setVisibility(View.GONE);
                    }
                    Log.v("Walkthrough", "onPageScrolled position : " + position);
                }

                @Override
                public void onPageSelected(int position) {
                    Log.v("Walkthrough", "onPageSelected position : ");
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    Log.v("Walkthrough", "onPageScrollStateChanged position : ");
                }
            });


            if (isMain) {
                gotItText.setText("Get Started");
            } else {
                gotItText.setText("Done");
            }

            gotItText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (intent != null && intent.getBooleanExtra("ismain", false)) {

                        if (!managerSession.getWalkthroughSkipValue()) {
                            managerSession.addUpdateSkipWalkthroughPref(true);
                        }

                        if (WalkThroughActivity.this.managerSession.hasSessionVizippCredentials()) {
                            startRecordActivity();
                        } else {
                            callLoginActivity();
                        }
                    } else {
                        finish();
                    }
                }
            });

            nextText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPager.setCurrentItem(getItem(+1), true);
                    if (getItem(+1) == itemCount) {
                        indicatorLayout.setVisibility(View.INVISIBLE);
                        skipText.setVisibility(View.GONE);
                        nextText.setVisibility(View.GONE);
                        gotItText.setVisibility(View.VISIBLE);
                    }
                }
            });
            skipText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (intent != null && intent.getBooleanExtra("ismain", false)) {

                        if (!managerSession.getWalkthroughSkipValue()) {
                            managerSession.addUpdateSkipWalkthroughPref(true);
                        }

                        if (WalkThroughActivity.this.managerSession.hasSessionVizippCredentials()) {
                            startRecordActivity();
                        } else {
                            callLoginActivity();
                        }
                    } else {
                        finish();
                    }
                }
            });
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            Log.v("Walkthrough :", "Exception" + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (isMain) {

        } else {
            finish();
        }

    }

    private int getItem(int i) {
        return mPager.getCurrentItem() + i;
    }

    private void callLoginActivity() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(WalkThroughActivity.this, VizippDrawLogin.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void startRecordActivity() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(WalkThroughActivity.this, StartRecordActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(WalkThroughActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }
}





