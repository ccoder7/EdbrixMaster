package com.edbrix.contentbrix.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.commons.CustomDialog;

/**
 * Created by rajk on 27/02/18.
 */

public class TextEditorDialog extends CustomDialog {

    private ImageView btnBold;
    private ImageView btnItalics;
    private ImageView btnUnderline;
    private Button btnDone;
    private EditText edtMessage;
    private EditText edtSize;
    private Context dContext;
    private int txtStyle;
    private int txtSize;
    private boolean isUnderlined;
    private SeekBar sizeSeekBar;

    private boolean isBold;
    private boolean isItalics;

    private String capturedText;

    private ActionButtonListener actionButtonListener;

    public interface ActionButtonListener {
        void onDoneText(String textString, int textStyle, int textSize, boolean isUnderlined);
    }

    public TextEditorDialog(Context context, ActionButtonListener listener) {
        super(context);
        this.dContext = context;
        this.actionButtonListener = listener;
    }

    public TextEditorDialog(Context context, String capturedText, ActionButtonListener listener) {
        super(context);
        this.dContext = context;
        this.actionButtonListener = listener;
        this.capturedText = capturedText;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.CENTER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.text_editor_view);
        btnBold =  findViewById(R.id.btnBold);
        btnItalics = findViewById(R.id.btnItalics);
        btnUnderline = findViewById(R.id.btnUnderline);
        btnDone = findViewById(R.id.btnDoneText);
        edtMessage =  findViewById(R.id.edtMessage);
        edtSize = findViewById(R.id.edtSize);
        sizeSeekBar = findViewById(R.id.sizeSeekBar);
        txtSize = 14;
        txtStyle = Typeface.NORMAL;
        isBold = false;
        isItalics = false;
        isUnderlined = false;

        edtMessage.setText(capturedText);

        btnBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBold){
                    btnBold.setBackgroundColor(getContext().getResources().getColor(R.color.colorWhiteSmoke));
                    txtStyle = Typeface.NORMAL;
                    if(isItalics){
                        txtStyle = Typeface.ITALIC;
                    }

                }else{
                    btnBold.setBackgroundColor(getContext().getResources().getColor(R.color.appColor));
                    txtStyle = Typeface.BOLD;
                    if(isItalics) {
                        txtStyle = Typeface.BOLD_ITALIC;
                    }
                }

                isBold=!isBold;
                if(isUnderlined) {
                    edtMessage.setText(Html.fromHtml("<u>" + edtMessage.getText().toString() + "</u>"));
                }

                edtMessage.setTextColor(dContext.getResources().getColor(R.color.ColorsBlack));
                edtMessage.setTypeface(null, txtStyle);
                edtMessage.setTextSize(Integer.parseInt(edtSize.getText().toString()));
            }
        });

        btnItalics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isItalics){
                    btnItalics.setBackgroundColor(getContext().getResources().getColor(R.color.colorWhiteSmoke));
                    txtStyle = Typeface.NORMAL;
                    if(isBold){
                        txtStyle = Typeface.BOLD;
                    }
                }else{
                    btnItalics.setBackgroundColor(getContext().getResources().getColor(R.color.appColor));
                    txtStyle = Typeface.ITALIC;
                    if(isBold){
                        txtStyle = Typeface.BOLD_ITALIC;
                    }
                }

                isItalics=!isItalics;
                if(isUnderlined) {
                    edtMessage.setText(Html.fromHtml("<u>" + edtMessage.getText().toString() + "</u>"));
                }
                edtMessage.setTypeface(null,txtStyle);
                edtMessage.setTextSize(Integer.parseInt(edtSize.getText().toString()));
            }
        });

        btnUnderline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = edtMessage.getText().toString();

                if(isUnderlined){
                    btnUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.colorWhiteSmoke));
                    edtMessage.setText(text);
                }else{
                    btnUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.appColor));
                    edtMessage.setText(Html.fromHtml("<u>" + text + "</u>"));
                }
                isUnderlined = !isUnderlined;
                edtMessage.setTextSize(Integer.parseInt(edtSize.getText().toString()));
            }
        });


        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                actionButtonListener.onDoneText(edtMessage.getText().toString(), txtStyle, Integer.parseInt(edtSize.getText().toString()), isUnderlined);
            }
        });

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int size, boolean b) {
                edtMessage.setTextSize(size);
                edtSize.setText(""+size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public void showMe() {
        super.showMe();
    }
}