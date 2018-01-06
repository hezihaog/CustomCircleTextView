package com.hzh.custom.circle.textview.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Package: com.hzh.custom.circle.textview.widget
 * FileName: CircleTextView
 * Date: on 2018/1/6  下午11:48
 * Auther: zihe
 * Descirbe:
 * Email: hezihao@linghit.com
 */

public class CircleTextView extends View {
    /**
     * 默认值
     */
    private static int DEFAULT_CIRCLE_SIZE;
    /**
     * 绘制相关
     */
    private Paint mCirclePaint;
    private Paint mTextPaint;
    /**
     * 控件的宽高
     */
    private int mWidth;
    private int mHeight;
    /**
     * 配置参数
     */
    private int mRadius;
    private int mCenterX;
    private int mCenterY;
    private float mAngle;
    /**
     * 素材
     */
    private String mNeedDrawTextStr;

    public CircleTextView(Context context) {
        super(context);
        init();
    }

    public CircleTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //初始化默认值
        DEFAULT_CIRCLE_SIZE = dip2px(getContext(), 55);
        //外圆的画笔
        mCirclePaint = new Paint();
        mCirclePaint.setColor(0xFFF44336);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setAntiAlias(true);
        //中间文字的画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.parseColor("#FFFFFF"));
        mTextPaint.setTextSize(sp2px(getContext(), 15));
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(dip2px(getContext(), 4));
        mTextPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取View的总宽高
        mWidth = w;
        mHeight = h;
        //获取padding值
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        //减去padding，得出总显示范围
        int width = mWidth - paddingLeft - paddingRight;
        int height = mHeight - paddingTop - paddingBottom;
        //计算圆心坐标
        mCenterX = width / 2 + paddingLeft;
        mCenterY = height / 2 + paddingTop;
        //宽或者高，最小的作为基准，除以2得出半径
        mRadius = Math.min(width, height) / 2;
        //旋转角度
        mAngle = 0;
        //要画的文字
        mNeedDrawTextStr = "简书";
        //使用动画改变，旋转、缩放、渐变
        ValueAnimator animator = ValueAnimator.ofFloat(0, 360);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(1500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //更新旋转角度
                mAngle = (Float) animation.getAnimatedValue();
                //缩放
                float scale = (mAngle / 360) * 1;
                CircleTextView.this.setScaleX(scale + 1);
                CircleTextView.this.setScaleY(scale + 1);
                //渐变
                float alpha = (mAngle / 360) * 1;
                CircleTextView.this.setAlpha(alpha);
                postInvalidate();
            }
        });
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureSpec(widthMeasureSpec);
        int height = measureSpec(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    /**
     * 决定测量大小
     */
    private int measureSpec(int measureSpec) {
        int result;
        //取出模式和大小
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        //如果已经指定宽高了，则直接使用设置的值
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else if (mode == MeasureSpec.AT_MOST) {
            //如果没有指定，就是wrap_content的，则取默认值和大小中的最小值
            result = Math.min(DEFAULT_CIRCLE_SIZE, size);
        } else {
            result = DEFAULT_CIRCLE_SIZE;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1、画外圆
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mCirclePaint);
        //先旋转画布一定角度，再画文字，文字就会按一定角度旋转，画文字前必须先旋转，否则无效
        canvas.rotate(mAngle, mCenterX, mCenterY);
        //2、画文字
        //画文字，需要确定文字最左边的文字的X值和baseLine，文字的Ascent上降和Descent下降相交的位置
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        //这里的baseLine，上降加上下降就是文字的总高度，再除以2就是文字一半的高度，我们的文字的中心是画在圆心的，所以baseLine先选择是文字中心
        float baseLine = -(fontMetrics.ascent + fontMetrics.descent) / 2;
        //确定X坐标，其实就是中心点的X坐标减去，文字的长度的一半，为什么要减去呢？因为坐标系是从View的左上角开始算
        //，如果文字的中心要在圆心，就要以文字中心向左偏移一半的文字长度
        float textWidth = mTextPaint.measureText(mNeedDrawTextStr);
        int startX = (int) (mCenterX - (textWidth / 2));
        //最终确定文字的baseLine，其实就是圆心加上文字的高度的一半
        int endY = (int) (mCenterY + baseLine);
        canvas.drawText(mNeedDrawTextStr, startX, endY, mTextPaint);
    }

    /**
     * -------------------- 转换方法 --------------------
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    private int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }
}
