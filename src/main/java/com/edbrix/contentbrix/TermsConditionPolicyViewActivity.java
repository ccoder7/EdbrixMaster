package com.edbrix.contentbrix;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.edbrix.contentbrix.baseclasses.BaseActivity;


public class TermsConditionPolicyViewActivity extends BaseActivity {
    private WebView webView;
    private TextView btnOK;
    private String pageLinkString;
//"file:///android_asset/hello.html"
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_condition_policy_view);
        webView = (WebView) findViewById(R.id.webView);
        btnOK = (TextView) findViewById(R.id.btnOK);
        pageLinkString = getIntent().getStringExtra("pagelink");

        if(pageLinkString==null || pageLinkString.length()==0){
            pageLinkString = "file:///android_asset/offline.html";
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(pageLinkString);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
