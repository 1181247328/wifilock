package com.deelock.wifilock.overwrite;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.deelock.wifilock.utils.DensityUtil;

/**
 * Created by forgive for on 2018\1\9 0009.
 */

public class WaterView extends View {

    private float viewWidth;
    private float viewHeight;
    private float cornerSize;

    private float waveHeight;
    private Matrix matrix;
    private Matrix backMatrix;
    private int offX = 0;
    private int backOffX = 0;

    private Path bottomLeftPath;
    private Path bottomRightPath;
    private Path backPath;
    private Path frontPath;
    private Paint backPaint;
    private Paint frontPaint;
    private Paint bottomPaint;
    private boolean isNeedInit;
    private Context context;
    private ValueAnimator animator;

    public WaterView(Context context) {
        this(context, null);
    }

    public WaterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        bottomLeftPath = new Path();
        bottomRightPath = new Path();
        backPath = new Path();
        frontPath = new Path();
        frontPaint = new Paint();
        backPaint = new Paint();
        bottomPaint = new Paint();
        matrix = new Matrix();
        backMatrix = new Matrix();

        matrix.postTranslate(5, 0);
        backMatrix.postTranslate(3, 0);
        frontPaint.setColor(0x9a12c6e5);
        frontPaint.setAntiAlias(true);
        backPaint.setColor(0x4012c6e5);
        backPaint.setAntiAlias(true);
        bottomPaint.setColor(0xfff0f0f0);
        bottomPaint.setAntiAlias(true);

        viewWidth = 630 * DensityUtil.getScreenWidth(context) / 750;
        viewHeight = 340 * DensityUtil.getScreenHeight(context) / 1334;
        waveHeight = viewHeight / 5f;
        cornerSize = viewHeight / 10f;

        bottomLeftPath.moveTo(-2, viewHeight - cornerSize + 1);
        bottomLeftPath.cubicTo(-2, viewHeight - cornerSize, -2, viewHeight + 1, cornerSize, viewHeight + 1);
        bottomLeftPath.lineTo(-2, viewHeight + 1);
        bottomLeftPath.lineTo(-2, viewHeight - cornerSize);
        bottomLeftPath.close();

        bottomRightPath.moveTo(viewWidth - cornerSize - 2, viewHeight + 2);
        bottomRightPath.cubicTo(viewWidth - cornerSize - 2, viewHeight + 2, viewWidth+2, viewHeight+2, viewWidth + 2, viewHeight - cornerSize - 2);
        bottomRightPath.lineTo(viewWidth + 2, viewHeight + 2);
        bottomRightPath.lineTo(viewWidth - cornerSize - 2, viewHeight + 2);
        bottomRightPath.close();

        backPath.moveTo(-viewWidth * 2, waveHeight);
        backPath.cubicTo(-viewWidth * 2, waveHeight, -viewWidth * 3 / 2, waveHeight * 2, -viewWidth, waveHeight);
        backPath.cubicTo(-viewWidth, waveHeight, -viewWidth / 2, 0, 0, waveHeight);
        backPath.cubicTo(0, waveHeight, viewWidth / 2 , waveHeight * 2, viewWidth, waveHeight);
        backPath.lineTo(viewWidth, viewHeight);
        backPath.lineTo(-viewWidth * 2, viewHeight);
        backPath.lineTo(-viewWidth * 2, waveHeight);
        backPath.close();

        frontPath = new Path(backPath);

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                offX += 5;
                backOffX += 3;
                if (offX >= viewWidth * 2){
                    offX -= viewWidth * 2;
                    matrix.postTranslate(-viewWidth * 2, 0);
                    frontPath.transform(matrix);
                    matrix.postTranslate(viewWidth * 2, 0);
                } else {
                    frontPath.transform(matrix);
                }

                if (backOffX >= viewWidth * 2){
                    backOffX -= viewWidth * 2;
                    backMatrix.postTranslate(-viewWidth * 2, 0);
                    backPath.transform(backMatrix);
                    backMatrix.postTranslate(viewWidth * 2, 0);
                } else {
                    backPath.transform(backMatrix);
                }
                invalidate();
            }
        });
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
    }

    public void setNetState(boolean isOnline){
        if (isOnline){
            frontPaint.setColor(0x9a12c6e5);
            backPaint.setColor(0x4042dabe);
        } else {
            frontPaint.setColor(0x9aff8239);
            backPaint.setColor(0x40ff8239);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(backPath, backPaint);
        canvas.drawPath(frontPath, frontPaint);
        canvas.drawPath(bottomLeftPath, bottomPaint);
        canvas.drawPath(bottomRightPath, bottomPaint);
    }

    public void setCloseView() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

}
