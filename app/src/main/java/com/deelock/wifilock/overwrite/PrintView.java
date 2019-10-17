package com.deelock.wifilock.overwrite;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.deelock.wifilock.utils.DensityUtil;

/**
 * Created by forgive for on 2018\1\4 0004.
 */

public class PrintView extends View {

    private Paint defaultPaint = new Paint();
    private Paint dstPaint = new Paint();
    private Path path11 = new Path();
    private Path path12 = new Path();
    private Path path13 = new Path();
    private Path path21 = new Path();
    private Path path22 = new Path();
    private Path path23 = new Path();
    private Path path31 = new Path();
    private Path path32 = new Path();
    private Path path33 = new Path();
    private Path path41 = new Path();
    private Path path42 = new Path();
    private Path path51 = new Path();
    private Path path52 = new Path();
    private Path path53 = new Path();
    
    private Path path11Dst = new Path();
    private Path path12Dst = new Path();
    private Path path13Dst = new Path();
    private Path path21Dst = new Path();
    private Path path22Dst = new Path();
    private Path path23Dst = new Path();
    private Path path31Dst = new Path();
    private Path path32Dst = new Path();
    private Path path33Dst = new Path();
    private Path path41Dst = new Path();
    private Path path42Dst = new Path();
    private Path path51Dst = new Path();
    private Path path52Dst = new Path();
    private Path path53Dst = new Path();

    private PathMeasure pathMeasure11 = new PathMeasure();
    private PathMeasure pathMeasure12 = new PathMeasure();
    private PathMeasure pathMeasure13 = new PathMeasure();
    private PathMeasure pathMeasure21 = new PathMeasure();
    private PathMeasure pathMeasure22 = new PathMeasure();
    private PathMeasure pathMeasure23 = new PathMeasure();
    private PathMeasure pathMeasure31 = new PathMeasure();
    private PathMeasure pathMeasure32 = new PathMeasure();
    private PathMeasure pathMeasure33 = new PathMeasure();
    private PathMeasure pathMeasure41 = new PathMeasure();
    private PathMeasure pathMeasure42 = new PathMeasure();
    private PathMeasure pathMeasure51 = new PathMeasure();
    private PathMeasure pathMeasure52 = new PathMeasure();
    private PathMeasure pathMeasure53 = new PathMeasure();
    
    private float pathValue;

    private float scaleRate;

    private int step;

    private int defaultColor = Color.WHITE;
    private int strokeColor = Color.argb(255, 0, 180, 255);

    private ValueAnimator valueAnimator;

    public PrintView(Context context) {
        this(context, null);
    }

    public PrintView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrintView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        scaleRate = DensityUtil.getScreenWidth(context) * 2 / 750f;

        defaultPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        defaultPaint.setStrokeWidth(4);
        defaultPaint.setStrokeJoin(Paint.Join.ROUND);
        defaultPaint.setStrokeCap(Paint.Cap.ROUND);
        defaultPaint.setStyle(Paint.Style.STROKE);
        defaultPaint.setAntiAlias(true);
        defaultPaint.setColor(defaultColor);

        dstPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        dstPaint.setStrokeWidth(4);
        dstPaint.setStrokeJoin(Paint.Join.ROUND);
        dstPaint.setStrokeCap(Paint.Cap.ROUND);
        dstPaint.setStyle(Paint.Style.STROKE);
        dstPaint.setAntiAlias(true);
        dstPaint.setColor(strokeColor);

        moveTo(path11, 7f, 59.14f);
        cubicTo(path11, 16.62f, 56.3f, 21.68f, 49.92f, 22.18f, 40f);

        moveTo(path12, 25f, 17.41f);
        cubicTo(path12, 37.89f, 11.31f, 48.73f, 11.54f, 57.54f, 18.11f);
        cubicTo(path12, 66.35f, 24.68f, 70.14f, 32.44f, 68.91f, 41.39f);

        moveTo(path13, 40.65f, 39f);
        cubicTo(path13, 40.5f, 48.03f, 38.61f, 54.76f, 35f, 59.19f);

        moveTo(path21, 23f, 36.36f);
        cubicTo(path21, 26.08f, 25.91f, 32.45f, 21.16f, 42.1f, 22.12f);
        cubicTo(path21, 56.58f, 23.57f, 63.3f, 35.48f, 57.5f, 56.61f);

        moveTo(path22, 3.12f, 44.76f);
        cubicTo(path22, 2.69f, 37.97f, 3.42f, 32.39f, 5.31f, 28f);

        moveTo(path23, 73.8f, 21f);
        cubicTo(path23, 80.85f, 35f, 79.92f, 48.88f, 71f, 62.63f);

        moveTo(path31, 30f, 50.58f);
        cubicTo(path31, 31.07f, 47.27f, 31.6f, 44.63f, 31.58f, 42.65f);
        cubicTo(path31, 31.56f, 39.67f, 31.59f, 31f, 41.07f, 31f);
        cubicTo(path31, 47.4f, 31f, 50.46f, 35.12f, 50.27f, 43.36f);

        moveTo(path32, 8f, 22.3f);
        cubicTo(path32, 15.27f, 10.34f, 24.77f, 3.66f, 36.52f, 2.26f);
        cubicTo(path32, 54.14f, 0.15f, 67.86f, 11.55f, 70.46f, 16.12f);

        moveTo(path33, 49.53f, 50f);
        cubicTo(path33, 47.25f, 61.37f, 41.4f, 70.53f, 32f, 77.48f);

        moveTo(path41, 56.63f, 60f);
        cubicTo(path41, 52.59f, 69.28f, 48.71f, 75.23f, 45f, 77.84f);

        moveTo(path42, 4f, 50.09f);
        cubicTo(path42, 9.43f, 49.1f, 12.27f, 46.13f, 12.51f, 41.17f);
        cubicTo(path42, 12.88f, 33.73f, 15.74f, 24.05f, 21.75f, 20f);

        moveTo(path51, 33.33f, 63f);
        cubicTo(path51, 27.32f, 69.56f, 23.21f, 73.12f, 21f, 73.69f);

        moveTo(path52, 13f, 66.58f);
        cubicTo(path52, 18.5f, 65.74f, 23.55f, 61.55f, 28.15f, 54f);

        moveTo(path53, 68.63f, 46f);
        cubicTo(path53, 67.3f, 59.08f, 64.43f, 67.98f, 60f, 72.71f);
        
        pathMeasure11.setPath(path11, false);
        pathMeasure12.setPath(path12, false);
        pathMeasure13.setPath(path13, false);
        pathMeasure21.setPath(path21, false);
        pathMeasure22.setPath(path22, false);
        pathMeasure23.setPath(path23, false);
        pathMeasure31.setPath(path31, false);
        pathMeasure32.setPath(path32, false);
        pathMeasure33.setPath(path33, false);
        pathMeasure41.setPath(path41, false);
        pathMeasure42.setPath(path42, false);
        pathMeasure51.setPath(path51, false);
        pathMeasure52.setPath(path52, false);
        pathMeasure53.setPath(path53, false);

        valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                pathValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setDuration(1000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (step){
            case 1:
                drawFirstPath(canvas, defaultPaint);
                drawSecondPath(canvas, defaultPaint);
                drawThirdPath(canvas, defaultPaint);
                drawFourthPath(canvas, defaultPaint);
                drawFifthPath(canvas, defaultPaint);
                pathMeasure11.getSegment(0, pathValue * pathMeasure11.getLength(), path11Dst, true);
                canvas.drawPath(path11Dst, dstPaint);
                pathMeasure12.getSegment(0, pathValue * pathMeasure12.getLength(), path12Dst, true);
                canvas.drawPath(path12Dst, dstPaint);
                pathMeasure13.getSegment(0, pathValue * pathMeasure13.getLength(), path13Dst, true);
                canvas.drawPath(path13Dst, dstPaint);
                break;
            case 2:
                drawFirstPath(canvas, dstPaint);
                drawSecondPath(canvas, defaultPaint);
                drawThirdPath(canvas, defaultPaint);
                drawFourthPath(canvas, defaultPaint);
                drawFifthPath(canvas, defaultPaint);
                pathMeasure21.getSegment(0, pathValue * pathMeasure21.getLength(), path21Dst, true);
                canvas.drawPath(path21Dst, dstPaint);
                pathMeasure22.getSegment(0, pathValue * pathMeasure22.getLength(), path22Dst, true);
                canvas.drawPath(path22Dst, dstPaint);
                pathMeasure23.getSegment(0, pathValue * pathMeasure23.getLength(), path23Dst, true);
                canvas.drawPath(path23Dst, dstPaint);
                break;
            case 3:
                drawFirstPath(canvas, dstPaint);
                drawSecondPath(canvas, dstPaint);
                drawThirdPath(canvas, defaultPaint);
                drawFourthPath(canvas, defaultPaint);
                drawFifthPath(canvas, defaultPaint);
                pathMeasure31.getSegment(0, pathValue * pathMeasure31.getLength(), path31Dst, true);
                canvas.drawPath(path31Dst, dstPaint);
                pathMeasure32.getSegment(0, pathValue * pathMeasure32.getLength(), path32Dst, true);
                canvas.drawPath(path32Dst, dstPaint);
                pathMeasure33.getSegment(0, pathValue * pathMeasure33.getLength(), path33Dst, true);
                canvas.drawPath(path33Dst, dstPaint);
                break;
            case 4:
                drawFirstPath(canvas, dstPaint);
                drawSecondPath(canvas, dstPaint);
                drawThirdPath(canvas, dstPaint);
                drawFourthPath(canvas, defaultPaint);
                drawFifthPath(canvas, defaultPaint);
                pathMeasure41.getSegment(0, pathValue * pathMeasure41.getLength(), path41Dst, true);
                canvas.drawPath(path41Dst, dstPaint);
                pathMeasure42.getSegment(0, pathValue * pathMeasure42.getLength(), path42Dst, true);
                canvas.drawPath(path42Dst, dstPaint);
                break;
            case 5:
                drawFirstPath(canvas, dstPaint);
                drawSecondPath(canvas, dstPaint);
                drawThirdPath(canvas, dstPaint);
                drawFourthPath(canvas, dstPaint);
                drawFifthPath(canvas, defaultPaint);
                pathMeasure51.getSegment(0, pathValue * pathMeasure51.getLength(), path51Dst, true);
                canvas.drawPath(path51Dst, dstPaint);
                pathMeasure52.getSegment(0, pathValue * pathMeasure52.getLength(), path52Dst, true);
                canvas.drawPath(path52Dst, dstPaint);
                pathMeasure53.getSegment(0, pathValue * pathMeasure53.getLength(), path53Dst, true);
                canvas.drawPath(path53Dst, dstPaint);
                break;
            default:
                drawFirstPath(canvas, defaultPaint);
                drawSecondPath(canvas, defaultPaint);
                drawThirdPath(canvas, defaultPaint);
                drawFourthPath(canvas, defaultPaint);
                drawFifthPath(canvas, defaultPaint);
                break;
        }
    }

    private void drawFirstPath(Canvas canvas, Paint paint){
        canvas.drawPath(path11, paint);
        canvas.drawPath(path12, paint);
        canvas.drawPath(path13, paint);
    }

    private void drawSecondPath(Canvas canvas, Paint paint){
        canvas.drawPath(path21, paint);
        canvas.drawPath(path22, paint);
        canvas.drawPath(path23, paint);
    }

    private void drawThirdPath(Canvas canvas, Paint paint){
        canvas.drawPath(path31, paint);
        canvas.drawPath(path32, paint);
        canvas.drawPath(path33, paint);
    }

    private void drawFourthPath(Canvas canvas, Paint paint){
        canvas.drawPath(path41, paint);
        canvas.drawPath(path42, paint);
    }
    
    private void drawFifthPath(Canvas canvas, Paint paint){
        canvas.drawPath(path51, paint);
        canvas.drawPath(path52, paint);
        canvas.drawPath(path53, paint);
    }

    private void moveTo(Path path, float x, float y){
        path.moveTo(x * scaleRate, y * scaleRate);
    }

    private void cubicTo(Path path, float a, float b, float c, float d, float e, float f){
        path.cubicTo(a * scaleRate, b * scaleRate, c * scaleRate, d * scaleRate, e * scaleRate, f * scaleRate);
    }

    public void setStep(int step){
        if (this.step != step){
            this.step = step;
            pathValue = 0;
            valueAnimator.start();
        }
    }
}
