package com.edbrix.contentbrix.customview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edbrix.contentbrix.R;


/**
 * Created by rajk.
 */

public class CustomDialogSlideFromBottom extends Dialog {

    public static final String OPT_EDBRIX = "Edbrix";
    public static final String OPT_DRIVE = "Drive";
    public static final String OPT_DROPBOX = "DropBox";
    public static final String OPT_ONE_DRIVE = "OneDrive";

    private LinearLayout btnEdbrix;
    private LinearLayout btnDrive;
    private LinearLayout btnDropBox;
    private LinearLayout btnCancel;
    private LinearLayout btnOneDrive;
    private TextView txtTitle;
    private OnActionButtonListener onActionButtonListener;
    private String strTitile;

    public interface OnActionButtonListener {
        abstract void onOptionPressed(String optionType);
    }

    public CustomDialogSlideFromBottom(@NonNull Context context, @StyleRes int themeResId, String strTitile) {
        super(context, themeResId);
        setContentView(R.layout.show_upload_option);
        setCancelable(true);
        this.strTitile = strTitile;
    }

    public void setOnActionButtonListener(OnActionButtonListener listener) {
        this.onActionButtonListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        txtTitle = (TextView) findViewById(R.id.customtitle);
        btnEdbrix = (LinearLayout) findViewById(R.id.btnEdbrix);
        btnDrive = (LinearLayout) findViewById(R.id.btnDrive);
        btnDropBox = (LinearLayout) findViewById(R.id.btnDropBox);
        btnCancel = (LinearLayout) findViewById(R.id.btnCancel);
        btnOneDrive = (LinearLayout) findViewById(R.id.btnOneDrive);

        txtTitle.setText(strTitile);

        btnEdbrix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonListener.onOptionPressed(OPT_EDBRIX);
                dismiss();
            }
        });

        btnDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonListener.onOptionPressed(OPT_DRIVE);
                dismiss();
            }
        });

        btnDropBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonListener.onOptionPressed(OPT_DROPBOX);
                dismiss();
            }
        });
        btnOneDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonListener.onOptionPressed(OPT_ONE_DRIVE);
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        getWindow().setBackgroundDrawableResource(R.color.ColorBlackTransparent);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        super.onAttachedToWindow();
    }


    public void showMe() {
        this.show();
    }

}
