package com.gradientbrightnessviews.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.View;

import com.gradientbrightnessviews.R;

/**
 * SEEKBAR QUE ADMITE DEGRADADO Y BRILLO
 * Created by SECTOR WEB on 23/04/2018.
 */

public class GradientSeekBar extends View {

    private final int FACTOR = 15;
    private final int PADDING_NORMAL_CORNEL_DP = 2;

    private Paint mProgressPaint;
    private Paint mBorderPaint;
    private Paint mTextPaint;

    private int mArcWidth;
    private int mArcRadius;
    private int mArcInnerRadius;
    private int mBorderOffset;
    private boolean mBorderRound;
    private boolean mHasBrighness;

    private float mProgress;
    private float mMaxValue;
    private float mMinValue;

    private int mWidth;
    private int mHeight;
    private int mPaddingVertical;
    private int mPaddingHorizontal;
    private int mCx, mCy;

    private int mStartAngle;
    private int mSwipeAngle;

    private Xfermode mProgressXferMode;
    private Xfermode mBrightnessXfermode;

    private SweepGradient mProgressGradient;
    private SweepGradient mBrighnessGradient;

    private int mBackgroundColor;
    private int mProgressStartColor;
    private int mProgressEndColor;
    private int mProgressBrightnessFilter;
    private int[] mProgressColors;

    private float mBorderGradientOffset;

    private RectF mSeekBarRect;
    private RectF mBrightnessRect;

    private Drawable mThumb;
    private boolean mHasThumb;

    private float mTextSize;
    private int mTextColor;

    public GradientSeekBar(Context context) {
        super(context);
        init(null);
    }

    public GradientSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GradientSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attr){
        Context context = getContext();
        if(attr != null){
            //SE OBTIENEN LOS VALORES DEL RECURSO DE ESTILO
            TypedArray typedArray = null;
            try{
                typedArray = context.obtainStyledAttributes(attr, R.styleable.GradientSeekBar);
                mMaxValue = typedArray.getFloat(R.styleable.GradientSeekBar_maxProgress, 100f);
                mMinValue = typedArray.getFloat(R.styleable.GradientSeekBar_minProgress, 0f);
                mProgress = typedArray.getFloat(R.styleable.GradientSeekBar_progress, 0f);
                mBorderOffset = typedArray.getDimensionPixelSize(R.styleable.GradientSeekBar_borderOffset,
                        getResources().getDimensionPixelOffset(R.dimen.border_offset));
                mBackgroundColor = typedArray.getColor(R.styleable.GradientSeekBar_progressBackgroundColor,
                        ContextCompat.getColor(context, R.color.colorBackground));
                mProgressStartColor = typedArray.getColor(R.styleable.GradientSeekBar_progressStartColor,
                        ContextCompat.getColor(context, R.color.colorPrimary));
                mProgressEndColor = typedArray.getColor(R.styleable.GradientSeekBar_progressEndColor,
                        ContextCompat.getColor(context, R.color.colorAccent));
                mHasBrighness = typedArray.getBoolean(R.styleable.GradientSeekBar_hasBrightness, true);
                mBorderRound = typedArray.getBoolean(R.styleable.GradientSeekBar_borderRound, false);
                mStartAngle = typedArray.getInteger(R.styleable.GradientSeekBar_startAngle, 0);
                mSwipeAngle = typedArray.getInteger(R.styleable.GradientSeekBar_swipeAngle, 360);
                mArcWidth = typedArray.getDimensionPixelSize(R.styleable.GradientSeekBar_arcWidth,
                        getResources().getDimensionPixelSize(R.dimen.arc_width));
                mThumb = typedArray.getDrawable(R.styleable.GradientSeekBar_thumb);
                if(mThumb != null)
                    mHasThumb = true;
                mTextSize = typedArray.getDimensionPixelSize(R.styleable.GradientSeekBar_textProgressSize,
                        getResources().getDimensionPixelSize(R.dimen.text_size));
                mTextColor = typedArray.getColor(R.styleable.GradientSeekBar_textProgressColor,
                        ContextCompat.getColor(context, R.color.colorPrimaryDark));
            }finally {
                if(typedArray != null)
                    typedArray.recycle();
            }
        }
        else{
            mMaxValue = 100f;
            mMinValue = 0f;
            mProgress = 0f;
            mBorderOffset = getResources().getDimensionPixelOffset(R.dimen.border_offset);
            mBackgroundColor = ContextCompat.getColor(context, R.color.colorBackground);
            mProgressStartColor = ContextCompat.getColor(context, R.color.colorPrimary);
            mProgressEndColor = ContextCompat.getColor(context, R.color.colorAccent);
            mHasBrighness = true;
            mBorderRound = false;
            mStartAngle = 0;
            mSwipeAngle = 359;
            mArcWidth = getResources().getDimensionPixelSize(R.dimen.arc_width);
            mHasThumb = false;
            mTextSize = getResources().getDimensionPixelSize(R.dimen.text_size);
            mTextColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        }

        mBorderGradientOffset =  0.3f * context.getResources().getDisplayMetrics().density;
        mProgressColors = new int[]{mProgressStartColor, mProgressEndColor};
        mProgressBrightnessFilter = ColorUtils.setAlphaComponent(Color.WHITE, 80);
        mPaddingVertical = mArcWidth / 2 + mBorderOffset;
        mPaddingHorizontal = mPaddingVertical;

        initPaints();
    }

    private void initPaints(){
        mProgressXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
        mBrightnessXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);

        //updateGradient();

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mBackgroundColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mArcWidth);
        if(mBorderRound)
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mArcWidth + 2 * mBorderOffset);
        if(mBorderRound)
            mBorderPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        mSeekBarRect = new RectF();
        mBrightnessRect = new RectF();
    }

    private void updateGradient(){
        float[] positions = new float[]{0.01f, 1f / 360f * mSwipeAngle};
        Matrix matrix = new Matrix();
        matrix.preRotate(mStartAngle, mSeekBarRect.centerX(), mSeekBarRect.centerY());
        mProgressGradient = new SweepGradient(mSeekBarRect.centerX(), mSeekBarRect.centerY(), mProgressColors, positions);
        mProgressGradient.setLocalMatrix(matrix);

        int[] colors = {ColorUtils.setAlphaComponent(mProgressStartColor, FACTOR),
                ColorUtils.setAlphaComponent(mProgressEndColor, FACTOR)};
        mBrighnessGradient = new SweepGradient(mBrightnessRect.centerX(), mBrightnessRect.centerY(), colors, positions);
        mBrighnessGradient.setLocalMatrix(matrix);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        mBrightnessRect.set(mPaddingHorizontal, mPaddingVertical, mWidth * 2f, mHeight * 2f);
        mSeekBarRect.set(mPaddingHorizontal + mBorderOffset / 10f, mPaddingVertical + mBorderOffset / 10f,
                mWidth * 2f  - mBorderOffset / 10f, mHeight * 2f - mBorderOffset / 10f);

        updateGradient();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mHasBrighness && mProgress == mMaxValue){
            float strokeWidth = mBorderPaint.getStrokeWidth();
            //SE OBTIENE EL NUMERO DE ITERACIONES QUE SE REALIZARAN
            float steps = mBorderOffset;
            for (float step = 1; step <= steps; step += mBorderGradientOffset){
                mBorderPaint.setShader(mBrighnessGradient);
                mBorderPaint.setStrokeWidth(strokeWidth - 2 * step);
                canvas.drawArc(mBrightnessRect, mStartAngle, mSwipeAngle, false, mBorderPaint);
            }
            mBorderPaint.setStrokeWidth(strokeWidth);

            //BORDE ENTRE BRILLO Y PROGRESSBAR
            mProgressPaint.setShader(mProgressGradient);
            mProgressPaint.setXfermode(mBrightnessXfermode);
            canvas.drawArc(mSeekBarRect, mStartAngle, mSwipeAngle, false, mProgressPaint);

            //FILTRO
            strokeWidth = mProgressPaint.getStrokeWidth();
            mProgressPaint.setStrokeWidth(strokeWidth - 4 * mBorderGradientOffset);
            mProgressPaint.setShader(null);
            mProgressPaint.setColor(mProgressBrightnessFilter);
            canvas.drawArc(mSeekBarRect, mStartAngle, mSwipeAngle, false, mProgressPaint);
            mProgressPaint.setColor(mBackgroundColor);
            mProgressPaint.setStrokeWidth(strokeWidth);
        }else{
            //BACKGROUND
            mProgressPaint.setShader(null);
            mProgressPaint.setXfermode(null);
            canvas.drawArc(mSeekBarRect, mStartAngle, mSwipeAngle, false, mProgressPaint);

            //PROGRESO
            mProgressPaint.setShader(mProgressGradient);
            mProgressPaint.setXfermode(mProgressXferMode);
            canvas.drawArc(mSeekBarRect, mStartAngle, getSweepAngle(mMinValue, mProgress), false, mProgressPaint);
        }

        //SE MUESTRA EL VALOR DEL PROGRESO
        float x = mWidth - mPaddingHorizontal / 2 - mTextPaint.getTextSize() / 2;
        float y = mHeight - mPaddingVertical / 2;
        canvas.drawText(String.valueOf(Math.round(mProgress)), x, y, mTextPaint);
    }

    private float getSweepAngle(float from, float to){
        return (to - from) / (mMaxValue - mMinValue) * mSwipeAngle;
    }

    public void setProgress(int progress){
        mProgress = progress;
        invalidate();
    }

    public float getMaxProgress(){
        return mMaxValue;
    }
}
