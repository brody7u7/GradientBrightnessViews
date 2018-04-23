package com.gradientbrightnessviews.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
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
import android.util.Log;
import android.view.View;

import com.gradientbrightnessviews.R;

/**
 * PROGRESS BAR QUE ADMITE DEGRADADO Y BRILLO
 * Created by Carlos on 18/04/2018.
 */

public class GradientBar extends View {

    private final int FACTOR = 10;
    private final int PADDING_NORMAL_CORNEL_DP = 2;

    private Paint mProgressPaint;
    private Paint mBorderPaint;

    private int mBorderOffset;
    private boolean mBorderRound;
    private boolean mHasBrighness;

    private float mCurrent;
    private float mTotal;

    private int mWidth;
    private int mHeight;
    private int mPaddingVertical;
    private int mPaddingHorizontal;

    private Xfermode mProgressXferMode;
    private Xfermode mBrightnessXfermode;


    private LinearGradient mProgressGradient;
    private LinearGradient mBrighnessGradient;

    private int mBackgroundColor;
    private int mProgressStartColor;
    private int mProgressEndColor;
    private int mProgressBrightnessFilter;
    private int[] mProgressColors;

    private float mBorderGradientOffset;

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
                mBackgroundColor = typedArray.getColor(R.styleable.GradientBar_progressBackgroundColor,
                        ContextCompat.getColor(context, R.color.colorBackground));
                mProgressStartColor = typedArray.getColor(R.styleable.GradientBar_progressStartColor,
                        ContextCompat.getColor(context, R.color.colorPrimary));
                mProgressEndColor = typedArray.getColor(R.styleable.GradientBar_progressEndColor,
                        ContextCompat.getColor(context, R.color.colorAccent));
                mHasBrighness = typedArray.getBoolean(R.styleable.GradientBar_hasBrightness, true);
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
            mBackgroundColor = ContextCompat.getColor(context, R.color.colorBackground);
            mProgressStartColor = ContextCompat.getColor(context, R.color.colorPrimary);
            mProgressEndColor = ContextCompat.getColor(context, R.color.colorAccent);
            mHasBrighness = true;
            mBorderRound = false;
        }

        mPaddingVertical = Math.round(PADDING_NORMAL_CORNEL_DP * context.getResources().getDisplayMetrics().density);
        mBorderGradientOffset =  0.3f * context.getResources().getDisplayMetrics().density;
        mProgressColors = new int[]{mProgressStartColor, mProgressEndColor};
        mProgressBrightnessFilter = ColorUtils.setAlphaComponent(Color.WHITE, 60);
        initPaints();
    }

    private void initPaints(){
        mProgressXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
        mBrightnessXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);

        updateGradient();

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mBackgroundColor);
        if(mBorderRound)
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setShader(mBrighnessGradient);
        if(mBorderRound)
            mBorderPaint.setStrokeCap(Paint.Cap.ROUND);

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

        int borderPlusPadding = mPaddingHorizontal; //mBorderOffset + mPaddingHorizontal;

        //SE REVISA SI HA LLEGADO AL VALOR MAXIMO Y DEBE PINTAR EL BRILLO
        if(mHasBrighness && mCurrent == mTotal){
            //SE OBTIENE EL NUMERO DE ITERACIONES QUE SE REALIZARAN
            float steps = mBorderOffset;

            for (float step = 1; step <= steps; step += mBorderGradientOffset){
                mBorderPaint.setStrokeWidth(mHeight - 2 * mPaddingVertical - 2 * step);
                mBorderPaint.setShader(mBrighnessGradient);
                canvas.drawLine(mPaddingHorizontal + step, mHeight / 2, mWidth - mPaddingHorizontal - step, mHeight / 2, mBorderPaint);
            }

            //BORDE ENTRE BRILLO Y PROGRESSBAR
            mProgressPaint.setStrokeWidth(mHeight - 2 * mBorderOffset - 2 * mPaddingVertical);
            mProgressPaint.setShader(mProgressGradient);
            mProgressPaint.setXfermode(mBrightnessXfermode);
            canvas.drawLine(borderPlusPadding, mHeight / 2, mWidth - borderPlusPadding, mHeight / 2, mProgressPaint);

            //RELLENO PROGRESSBAR
            mProgressPaint.setStrokeWidth(mHeight - 2 * mBorderOffset - 2 * mPaddingVertical - 3 * mBorderGradientOffset);
            mProgressPaint.setShader(null);
            mProgressPaint.setColor(mProgressBrightnessFilter);
            mProgressPaint.setXfermode(mBrightnessXfermode);
            canvas.drawLine(borderPlusPadding + 3 * mBorderGradientOffset, mHeight / 2,
                    mWidth - borderPlusPadding - 3 * mBorderGradientOffset, mHeight / 2, mProgressPaint);
            mProgressPaint.setColor(mBackgroundColor);
        }
        else{
            //BACKGROUND
            mProgressPaint.setStrokeWidth(mHeight - 2 * mBorderOffset - 2 * mPaddingVertical);
            mProgressPaint.setShader(null);
            mProgressPaint.setXfermode(null);
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
        mBrighnessGradient = new LinearGradient(0, 0, mWidth, 0,
                ColorUtils.setAlphaComponent(mProgressStartColor, FACTOR),
                ColorUtils.setAlphaComponent(mProgressEndColor, FACTOR),
                Shader.TileMode.CLAMP);
    }

    public void setProgress(int progress){
        mCurrent = progress;
        invalidate();
    }

    public float getMaxProgress(){
        return mTotal;
    }
}
