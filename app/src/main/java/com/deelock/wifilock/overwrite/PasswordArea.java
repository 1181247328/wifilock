package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.deelock.wifilock.utils.DensityUtil;

/**
 * Created by binChuan on 2017\9\28 0028.
 */

public class PasswordArea extends View {

    Paint textPaint;
    Paint linePaint;
    Context context;
    Rect targetRect;
    char[] text = {'\0', '\0', '\0', '\0', '\0', '\0'};

    int width;
    int lineWidth = 36;
    int lineHeight = 6;
    int spacing = 46;
    int textSize = 60;
    final int standardWidth = 750;
    int leftPadding;
    int baseline;

    public PasswordArea(Context context) {
        this(context, null);
    }

    public PasswordArea(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordArea(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        final int screenWidth = DensityUtil.getScreenWidth(context);
        textSize = screenWidth * textSize / standardWidth;
        spacing = screenWidth * spacing / standardWidth;
        lineHeight = screenWidth * lineHeight / standardWidth;
        lineWidth = screenWidth * lineWidth / standardWidth;

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(0xff333333);
        textPaint.setTextSize(textSize);

//        得到AssetManager
        AssetManager mgr = context.getAssets();

//        根据路径得到Typeface
        Typeface tf = Typeface.createFromAsset(mgr, "fonts/BAUHAUS_0.TTF");
        textPaint.setTypeface(tf);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(0xffd9d9d9);
        linePaint.setTextSize(textSize);
        linePaint.setStrokeWidth(lineHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        leftPadding = width / 2 - 3 * lineWidth - 5 * spacing / 2;
        targetRect = new Rect(0, 100 - textSize / 2, 100, 100 + textSize / 2);
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int left;
        for (int i = 0; i < 6; i++){
            left = leftPadding + i * (lineWidth + spacing);
            if (text[i] != '\0'){
                canvas.drawText(String.valueOf(text[i]), left, baseline, textPaint);
            } else {
                canvas.drawLine(left, 100, left + lineWidth, 100, linePaint);
            }
        }
    }

    public void setText(char[] text){
        this.text = text;
        invalidate();
    }
}
