package com.edbrix.contentbrix.customview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.edbrix.contentbrix.R;


/**
 * Created by rajk.
 */

public class CustomDialogSlideFromBottomAdvWhiteboard extends Dialog {

    public static final int OPT_DIAGRAM = 0;
    public static final int OPT_DRAWING= 1;
    public static final int OPT_MATH = 2;
    public static final int OPT_DOCUMENT = 5;
    public static final int OPT_TEXT = 4;
    public static final int OPT_RAW = 3;

    private LinearLayout btnDiagram;
    private LinearLayout btnDrawing;
    private LinearLayout btnMath;
    private LinearLayout btnTextDocument;
    private LinearLayout btnText;
    private LinearLayout btnRaw;
    private LinearLayout btnCancel;
    private TextView txtTitle;
    private OnActionButtonListener onActionButtonListener;
    private String strTitile;

    public interface OnActionButtonListener {
        abstract void onOptionPressed(int optionType);
        abstract void onCancelPressed();
    }

    public CustomDialogSlideFromBottomAdvWhiteboard(@NonNull Context context, @StyleRes int themeResId, String strTitile) {
        super(context, themeResId);
        setContentView(R.layout.select_pages_adv_whiteboard);
        setCancelable(false);
        this.strTitile = strTitile;
    }

    public void setOnActionButtonListener(OnActionButtonListener listener) {
        this.onActionButtonListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        txtTitle = (TextView) findViewById(R.id.customtitle);
        btnCancel = (LinearLayout) findViewById(R.id.btnCancel);
        btnDiagram = (LinearLayout) findViewById(R.id.btnDiagram);
        btnDrawing = (LinearLayout) findViewById(R.id.btnDrawing);
        btnMath = (LinearLayout) findViewById(R.id.btnMath);
        btnTextDocument = (LinearLayout) findViewById(R.id.btnTextDocument);
        btnText = (LinearLayout) findViewById(R.id.btnText);
        btnRaw = (LinearLayout) findViewById(R.id.btnRaw);

//        txtTitle.setText(strTitile);

        btnRaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonListener.onOptionPressed(OPT_RAW);
                dismiss();
            }
        });

        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonListener.onOptionPressed(OPT_TEXT);
                dismiss();
            }
        });

        btnTextDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonListener.onOptionPressed(OPT_DOCUMENT);
                dismiss();
            }
        });
        btnMath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonListener.onOptionPressed(OPT_MATH);
                dismiss();
            }
        });

        btnDrawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonListener.onOptionPressed(OPT_DRAWING);
                dismiss();
            }
        });

        btnDiagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonListener.onOptionPressed(OPT_DIAGRAM);
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onActionButtonListener.onCancelPressed();
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
