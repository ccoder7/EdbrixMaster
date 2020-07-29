package com.edbrix.contentbrix.customview;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.commons.CustomDialog;


/**
 * Created by rajk on 24/10/17.
 */

public class InformatoryDialog extends CustomDialog {

    private Context dContext;
    private int viewId;
    private OnActionButtonListener onActionButtonListener;
    private String posBtnText;
    private String negBtnText;
    private String dialogTitle;
    private String dialogMessage;
    private boolean showNegBtn;
    private Button btnOK;
    private Button btnCancel;

    public interface OnActionButtonListener {
        abstract void onPositiveButtonPressed();

        abstract void onNegativeButtonPressed();
    }

    public InformatoryDialog(Context context, int view, String title, String message) {
        super(context);
        this.dContext = context;
        this.viewId = view;
        this.dialogTitle = title;
        this.dialogMessage = message;
    }

    public InformatoryDialog(Context context, int view,String title, String message, boolean showNegBtn, String posBtnText, String negBtnText, OnActionButtonListener onActionButtonListener) {
        super(context);
        this.dContext = context;
        this.viewId = view;
        this.dialogTitle = title;
        this.dialogMessage = message;
        this.posBtnText = posBtnText;
        this.negBtnText = negBtnText;
        this.showNegBtn = showNegBtn;
        this.onActionButtonListener = onActionButtonListener;
    }

    public void setOnActionButtonListener(OnActionButtonListener onActionButtonListener) {
        this.onActionButtonListener = onActionButtonListener;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(viewId);
        if (dialogTitle != null && dialogTitle.length() > 0)
            ((TextView) findViewById(R.id.dialogTitleText)).setText(dialogTitle);

        if (dialogMessage != null && dialogMessage.length() > 0)
            ((TextView) findViewById(R.id.dialogMsgText)).setText(dialogMessage);

        btnOK = (Button) findViewById(R.id.btnOK);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        if (posBtnText != null && posBtnText.length() > 0) {
            btnOK.setText(posBtnText);
        }

        if (negBtnText != null && negBtnText.length() > 0) {
            btnCancel.setText(negBtnText);
        }

        if (showNegBtn) {
            btnCancel.setVisibility(View.VISIBLE);
        } else {
            btnCancel.setVisibility(View.GONE);
        }
        setListeners();
    }

    private void setListeners() {
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onActionButtonListener != null) {
                    onActionButtonListener.onPositiveButtonPressed();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onActionButtonListener != null) {
                    onActionButtonListener.onNegativeButtonPressed();
                }
            }
        });
    }

    @Override
    public void showMe() {
        super.showMe();
    }
}
