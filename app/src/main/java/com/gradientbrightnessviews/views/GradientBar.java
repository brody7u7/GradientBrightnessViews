package com.gradientbrightnessviews.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.gradientbrightnessviews.R;

/**
 * PROGRESS BAR QUE ADMITE DEGRADADO Y BRILLO
 * Created by Carlos on 18/04/2018.
 */

public class GradientBar extends View {

    private final int FACTOR = 80;
    private final int PADDING_NORMAL_CORNEL_DP = 2;

    private Paint mProgressPaint;
    private Paint mBorderPaint;

    private int mBorderOffset;
    private boolean mBorderRound;

    private float mCurrent;
    private float mTotal;

    private int mWidth;
    private int mHeight;
    private int mPaddingVertical;
    private int mPaddingHorizontal;

    private float mCornerRadius = 0;

    private RectF mProgressRect;
    private RectF mBackgroundRect;
    private RectF mBorderRect;

    private Xfermode mProgressXferMode;
    private Xfermode mGradientXferMode;

    private LinearGradient mProgressGradient;
    private int mBackgroundColor;
    private int mProgressStartColor;
    private int mProgressEndColor;
    private int[] mProgressColors;

    private LinearGradient mBrighnessGradient;
    private int mBrighnessStartColor;
    private int mBrighnessEndColor;
    private int[] mBrighnessColors;
    private LinearGradient mBrighnessBorderGradient;
    private int mBrighnessBorderStartColor;
    private int mBrighnessBorderEndColor;
    private int[] mBrighnessBorderColors;
    private boolean mHasBrighness;


    public GradientBar(Context context) {
        super(context);
        init(null);
    }

    public GradientBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GradientBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attr){
        Context context = getContext();
        if(attr != null){
            //SE OBTIENEN LOS VALORES DEL RECURSO DE ESTILO
            TypedArray typedArray = null;
            try{
                typedArray = context.obtainStyledAttributes(attr, R.styleable.GradientBar);
                mTotal = typedArray.getFloat(R.styleable.GradientBar_maxProgress, 100f);
                mCurrent = typedArray.getFloat(R.styleable.GradientBar_progress, 0f);
                mBorderOffset = typedArray.getDimensionPixelSize(R.styleable.GradientBar_borderOffset,
                        getResources().getDimensionPixelOffset(R.dimen.border_offset));
                mCornerRadius = typedArray.getDimensionPixelSize(R.styleable.GradientBar_cornerRadius,
                        getResources().getDimensionPixelSize(R.dimen.corner_radius));
                mBackgroundColor = typedArray.getColor(R.styleable.GradientBar_progressBackgroundColor,
                        ContextCompat.getColor(context, R.color.colorBackground));
                mProgressStartColor = typedArray.getColor(R.styleable.GradientBar_progressStartColor,
                        ContextCompat.getColor(context, R.color.colorPrimary));
                mProgressEndColor = typedArray.getColor(R.styleable.GradientBar_progressEndColor,
                        ContextCompat.getColor(context, R.color.colorAccent));
                mHasBrighness = typedArray.getBoolean(R.styleable.GradientBar_hasBrightness, true);
                mBrighnessStartColor = typedArray.getColor(R.styleable.GradientBar_brightnessStartColor,
                        ColorUtils.setAlphaComponent(mProgressStartColor, FACTOR));
                mBrighnessEndColor = typedArray.getColor(R.styleable.GradientBar_brightnessEndColor,
                        ColorUtils.setAlphaComponent(mProgressEndColor, FACTOR));
                mProgressColors = new int[]{mProgressStartColor, mProgressEndColor};
                mBrighnessColors = new int[]{mBrighnessStartColor, mBrighnessEndColor};
                mBorderRound = typedArray.getBoolean(R.styleable.GradientBar_borderRound, false);
            }finally {
                if(typedArray != null)
                    typedArray.recycle();
            }
        }
        else{
            mTotal = 100f;
            mCurrent = 0f;
            mBorderOffset = getResources().getDimensionPixelOffset(R.dimen.border_offset);
            mCornerRadius = getResources().getDimensionPixelSize(R.dimen.corner_radius);
            mBackgroundColor = ContextCompat.getColor(context, R.color.colorBackground);
            mProgressStartColor = ContextCompat.getColor(context, R.color.colorPrimary);
            mProgressEndColor = ContextCompat.getColor(context, R.color.colorAccent);
            mProgressColors = new int[]{mProgressStartColor, mProgressEndColor};
            mHasBrighness = true;
            mBrighnessStartColor = ColorUtils.setAlphaComponent(mProgressStartColor, FACTOR);
            mBrighnessEndColor = ColorUtils.setAlphaComponent(mProgressEndColor, FACTOR);
            mBrighnessColors = new int[]{mBrighnessStartColor, mBrighnessEndColor};
            mBorderRound = false;
        }
        mPaddingVertical = Math.round(PADDING_NORMAL_CORNEL_DP * context.getResources().getDisplayMetrics().density);
        mBrighnessBorderStartColor = ColorUtils.setAlphaComponent(mProgressStartColor, FACTOR);
        mBrighnessBorderEndColor = ColorUtils.setAlphaComponent(mProgressEndColor, FACTOR);
        mBrighnessBorderColors = new int[]{mBrighnessBorderStartColor, mBrighnessBorderEndColor};
        initPaints();
    }

    private void initPaints(){
        mProgressRect = new RectF();
        mBackgroundRect = new RectF();
        mBorderRect = new RectF();

        mProgressXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
        mGradientXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mProgressPaint.setColor(mBackgroundColor);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        //mBorderPaint.setColor(mBackgroundColor);
        mBorderPaint.setStrokeWidth(mBorderOffset * 2);

        mProgressGradient = new LinearGradient(0, 0, mWidth, 0, mProgressColors, null, Shader.TileMode.CLAMP);
        mBrighnessGradient = new LinearGradient(0, 0, mWidth, 0, mBrighnessColors, null, Shader.TileMode.CLAMP);
        mBrighnessBorderGradient = new LinearGradient(0, 0, mWidth, 0 , mBrighnessBorderColors, null, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;
        updateGradient();

        if(mBorderRound)
            mPaddingHorizontal = Math.round(mHeight / 2.0f);
        else
            mPaddingHorizontal = mPaddingVertical;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float progress = mCurrent / mTotal;

        setLayerType(LAYER_TYPE_HARDWARE, mProgressPaint);

        mProgressRect.set(mBorderOffset, mBorderOffset, progress * getWidth() - mBorderOffset, getHeight() - mBorderOffset);
        mBackgroundRect.set(mBorderOffset, mBorderOffset, getWidth() - mBorderOffset, getHeight() - mBorderOffset);
        mBorderRect.set(0, 0, getWidth(), getHeight());

        /*if(mHasBrighness && progress == 1){
            canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mProgressPaint);

            mProgressPaint.setShader(mProgressGradient);
            mProgressPaint.setXfermode(mProgressXferMode);
            canvas.drawRoundRect(mProgressRect, mCornerRadius, mCornerRadius, mProgressPaint);
            mProgressPaint.setShader(null);

            mProgressPaint.setXfermode(null);

            mBorderPaint.setShader(mBrighnessGradient);//TODO inicializar esto solo una vez y no en onDraw()
            //mBorderPaint.setXfermode(mGradientXferMode);
            canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius, mBorderPaint);
            mBorderPaint.setShader(null);
            mBorderPaint.setXfermode(null);
        }else{
            canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mProgressPaint);

            mProgressPaint.setShader(mProgressGradient);
            mProgressPaint.setXfermode(mProgressXferMode);
            canvas.drawRoundRect(mProgressRect, mCornerRadius, mCornerRadius, mProgressPaint);
            mProgressPaint.setShader(null);
            mProgressPaint.setXfermode(null);

            //canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mBorderPaint);
        }*/

        int borderPlusPadding = mBorderOffset + mPaddingHorizontal;
        //BRILLO
        if(mHasBrighness && mCurrent == mTotal){
            mProgressPaint.setStrokeWidth(mHeight - 2 * mBorderOffset - 2 * mPaddingVertical);
            mProgressPaint.setShader(mBrighnessGradient);
            mProgressPaint.setXfermode(null);
            if(mBorderRound)
                mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(borderPlusPadding, mHeight / 2, mWidth - borderPlusPadding, mHeight / 2, mProgressPaint);

            mBorderPaint.setShader(mBrighnessBorderGradient);
            mBorderPaint.setStrokeWidth(mHeight - 2 * mPaddingVertical);
            mBorderPaint.setXfermode(mGradientXferMode);
            if(mBorderRound)
                mBorderPaint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(mPaddingHorizontal, mHeight / 2, mWidth - mPaddingHorizontal, mHeight / 2, mBorderPaint);
        }else{
            //BACKGROUND
            mProgressPaint.setStrokeWidth(mHeight - 2 * mBorderOffset - 2 * mPaddingVertical);
            mProgressPaint.setShader(null);
            mProgressPaint.setXfermode(null);
            mProgressPaint.setColor(mBackgroundColor);
            if(mBorderRound)
                mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(borderPlusPadding, mHeight / 2, mWidth - borderPlusPadding, mHeight / 2, mProgressPaint);

            //PROGRESS
            if(progress > 0){
                mProgressPaint.setShader(mProgressGradient);
                mProgressPaint.setXfermode(mProgressXferMode);
                float stopX = (mWidth - borderPlusPadding) / mTotal * mCurrent;
                canvas.drawLine(borderPlusPadding, mHeight / 2, stopX, mHeight / 2, mProgressPaint);
            }
        }
    }

    private void updateGradient(){
        mProgressGradient = new LinearGradient(0, 0, mWidth, 0, mProgressColors, null, Shader.TileMode.CLAMP);
        mBrighnessGradient = new LinearGradient(0, 0, mWidth, 0, mBrighnessColors, null, Shader.TileMode.CLAMP);
        mBrighnessBorderGradient = new LinearGradient(0, 0, mWidth, 0 , mBrighnessBorderColors, null, Shader.TileMode.CLAMP);
    }

    public void setProgress(int progress){
        mCurrent = progress;
        invalidate();
    }

    public float getMaxProgress(){
        return mTotal;
    }
}
