package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.deelock.wifilock.utils.DensityUtil;

/**
 * Created by binChuan on 2017\10\11 0011.
 */

public class ObliqueView extends View {

    int screenWidth;
    int screenHeight;

    int leftY;
    int rightY;
    Path path;
    Paint paint;

    public ObliqueView(Context context) {
        this(context, null);
    }

    public ObliqueView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ObliqueView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenWidth = DensityUtil.getScreenWidth(context);
        screenHeight = DensityUtil.getScreenHeight(context);
        leftY = screenHeight * 397 / 1334;
        rightY = screenHeight * 497 / 1334;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xffa1e5d0);
//        paint.setStyle(Paint.Style.FILL);

        path = new Path();
        path.moveTo(0, leftY);
        path.lineTo(screenWidth, rightY);
        path.lineTo(screenWidth, screenHeight);
        path.lineTo(0, screenHeight);
        path.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }
}
