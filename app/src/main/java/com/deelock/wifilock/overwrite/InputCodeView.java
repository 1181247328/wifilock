package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public class InputCodeView extends View {


    private int passwordLength = 8;
    private int passwordPadding = 15;
    private int passwordSize = dp2px(20);
    private int borderWidth = 5;
    private String[] password;
    private InputMethodManager inputManager;
    private Paint paint;
    private StringBuffer inputContent = new StringBuffer();

    public InputCodeView(Context context) {
        super(context);
    }

    public InputCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        password = new String[8];
        setFocusableInTouchMode(true);
        setOnKeyListener(new MyOnKeyListener());
        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = 0;
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                width = passwordSize * passwordLength + passwordPadding * (passwordLength - 1);
                break;
            case MeasureSpec.EXACTLY:
                width = MeasureSpec.getSize(widthMeasureSpec);
                passwordSize = (width - (passwordPadding * passwordLength)) / (passwordLength + 1);
                break;
        }
        setMeasuredDimension(width, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawUnderLine(canvas, paint);
        drawCipherText(canvas, paint);
    }

    private void drawUnderLine(Canvas canvas, Paint paint) {
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(borderWidth);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i <= passwordLength; i++) {
            if (i == 4) {
                paint.setColor(Color.TRANSPARENT);
            } else {
                paint.setColor(Color.GRAY);
            }
            canvas.drawLine(getPaddingLeft() + (passwordSize + passwordPadding) * i,
                    getPaddingTop() + passwordSize + 20,
                    getPaddingLeft() + (passwordSize + passwordPadding) * i + passwordSize,
                    getPaddingTop() + passwordSize + 20,
                    paint);
        }
    }

    private void drawCipherText(Canvas canvas, Paint paint) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < inputContent.length(); i++) {
            if (i == 3) {
                canvas.drawText("", getPaddingLeft() + 25 + (passwordSize + passwordPadding) * i,
                        getPaddingTop() + passwordSize + 5, paint);
            }
            if (i > 3) {
                canvas.drawText(password[i], getPaddingLeft() + 25 + (passwordSize + passwordPadding) * (i + 1),
                        getPaddingTop() + passwordSize + 5, paint);
            } else {
                canvas.drawText(password[i], getPaddingLeft() + 25 + (passwordSize + passwordPadding) * i,
                        getPaddingTop() + passwordSize + 5, paint);
            }
        }
    }

    public String getInputCode() {
        StringBuffer stringBuffer = new StringBuffer();
        for (String c : password) {
            if (TextUtils.isEmpty(c)) {
                continue;
            }
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    public void clearInputCode() {
        inputContent.setLength(0);
        for (int i = 0; i < password.length; i++) {
            if (password[i] != null) {
                password[i] = null;
            }
        }
        postInvalidate();
    }

    class MyOnKeyListener implements OnKeyListener {

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            int action = keyEvent.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (inputContent.length() == 0) {
                        return true;
                    }
                    inputContent.deleteCharAt(inputContent.length() - 1);
                    for (int i = 0; i < password.length; i++) {
                        if (i < inputContent.length()) {
                            password[i] = String.valueOf(inputContent.charAt(i));
                        } else {
                            password[i] = null;
                        }
                    }
                    postInvalidate();
                    return true;
                }
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    if (inputContent.length() == 8) {
                        return true;
                    }
                    inputContent.append(String.valueOf(keyCode - 7));
                    for (int i = 0; i < inputContent.length(); i++) {
                        password[i] = String.valueOf(inputContent.charAt(i));
                    }
                    postInvalidate();
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            inputManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }

    private int dp2px(float dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
        return super.onCreateInputConnection(outAttrs);
    }
}