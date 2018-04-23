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
import android.view.View;

import com.gradientbrightnessviews.R;

/**
 * SEEKBAR QUE ADMITE DEGRADADO Y BRILLO
 * Created by SECTOR WEB on 23/04/2018.
 */

public class GradientSeekBar extends View {

    private final int FACTOR = 10;
    private final int PADDING_NORMAL_CORNEL_DP = 2;

    private Paint mProgressPaint;
    private Paint mBorderPaint;

    private int mArcWidth;
    private int mBorderOffset;
    private boolean mBorderRound;
    private boolean mHasBrighness;

    private float mCurrent;
    private float mTotal;

    private int mWidth;
    private int mHeight;
    private int mPaddingVertical;
    private int mPaddingHorizontal;

    private int mStartAngle;
    private int mEndAngle;

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

    private RectF mSeekBarRect;

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
                mTotal = typedArray.getFloat(R.styleable.GradientSeekBar_maxProgress, 100f);
                mCurrent = typedArray.getFloat(R.styleable.GradientSeekBar_progress, 0f);
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
                mEndAngle = typedArray.getInteger(R.styleable.GradientSeekBar_endAngle, 359);
                mArcWidth = typedArray.getDimensionPixelSize(R.styleable.GradientSeekBar_arcWidth,
                        getResources().getDimensionPixelSize(R.dimen.arc_width));
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
            mStartAngle = 0;
            mEndAngle = 359;
            mArcWidth = getResources().getDimensionPixelSize(R.dimen.arc_width);
        }

        mPaddingVertical = Math.round(PADDING_NORMAL_CORNEL_DP * context.getResources().getDisplayMetrics().density);
        mPaddingHorizontal = mPaddingVertical;
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
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mArcWidth);
        if(mBorderRound)
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if(mBorderRound)
            mBorderPaint.setStrokeCap(Paint.Cap.ROUND);

        mSeekBarRect = new RectF();
    }

    private void updateGradient(){
        mProgressGradient = new LinearGradient(0, 0, mWidth, 0, mProgressColors, null, Shader.TileMode.CLAMP);
        mBrighnessGradient = new LinearGradient(0, 0, mWidth, 0,
                ColorUtils.setAlphaComponent(mProgressStartColor, FACTOR),
                ColorUtils.setAlphaComponent(mProgressEndColor, FACTOR),
                Shader.TileMode.CLAMP);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;
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

        mSeekBarRect.set(mPaddingHorizontal, mPaddingVertical, mWidth - mPaddingHorizontal, mHeight - mPaddingVertical);

        mProgressPaint.setShader(mProgressGradient);
        canvas.drawArc(mSeekBarRect, mStartAngle, mEndAngle, false, mProgressPaint);
    }
}
