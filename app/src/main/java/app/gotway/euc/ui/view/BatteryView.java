package app.gotway.euc.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

import app.gotway.euc.util.ViewUtil;

public class BatteryView extends View {
    // private final float STROKE_WIDTH_SCALE;
    private ObjectAnimator anim;
    private float mCenterX;
    private int mPower;
    private int mPowerColor;
    private Paint mPowerPaint;
    private int mRectColor;
    private RectF mRectF;
    private Paint mRectPaint;
    private int[] mShadeColor;
    private Paint mShadePaint;
    private float mStrokeWidth;
    private Paint mTextPaint;
    private float mTextSize;
    private float yScale;

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //this.STROKE_WIDTH_SCALE = 0.033333335f;
        this.mTextSize = 28.0f;
        this.mPower = 0;
        this.mRectF = new RectF();
        this.mRectColor = -1;
        this.mPowerColor = -14288878;
        int[] iArr = new int[3];
        iArr[0] = -12303292;
        iArr[2] = -12303292;
        this.mShadeColor = iArr;
        init();
    }

    private void init() {
        this.mRectPaint = new Paint(1);
        this.mRectPaint.setColor(this.mRectColor);
        this.mRectPaint.setStyle(Style.STROKE);
        this.mShadePaint = new Paint(1);
        this.mPowerPaint = new Paint(1);
        this.mPowerPaint.setColor(this.mPowerColor);
        this.mTextPaint = new Paint(1);
        this.mTextPaint.setColor(this.mRectColor);
        this.mTextPaint.setTextAlign(Align.CENTER);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mCenterX = (float) (w / 2);
        int newWidth = (int) (((float) h) / 2.0f);
        this.mStrokeWidth = ((float) newWidth) * 0.033333335f;
        this.mTextSize = ((float) h) / 13.0f;
        this.mTextPaint.setTextSize(this.mTextSize);
        this.mRectF.left = this.mCenterX - ((float) (newWidth / 2));
        this.mRectF.right = this.mCenterX + ((float) (newWidth / 2));
        this.mRectF.top = (this.mStrokeWidth * 2.0f) + ((float) getPaddingTop());
        this.mRectF.bottom = (((float) (h - getPaddingBottom())) - ViewUtil.getTextHeight(this.mTextPaint)) - (this.mStrokeWidth * 2.0f);
        this.mShadePaint.setShader(new LinearGradient(this.mRectF.left + this.mStrokeWidth, 0.0f, this.mRectF.right - this.mStrokeWidth, 0.0f, this.mShadeColor, null, TileMode.CLAMP));
        this.yScale = this.mRectF.height() / 100.0f;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(this.mPower + "%", this.mCenterX, (float) (getMeasuredHeight() - getPaddingBottom()), this.mTextPaint);
        canvas.drawRoundRect(this.mRectF, this.mStrokeWidth * 2.0f, this.mStrokeWidth * 2.0f, this.mShadePaint);
        canvas.save();
        RectF f = new RectF(this.mRectF);
        f.top = f.bottom - (((float) this.mPower) * this.yScale);
        canvas.drawRoundRect(f, this.mStrokeWidth * 2.0f, this.mStrokeWidth * 2.0f, this.mPowerPaint);
        canvas.restore();
        drawBg(canvas);
    }

    private void drawBg(Canvas canvas) {
        this.mRectPaint.setStrokeWidth(this.mStrokeWidth);
        canvas.drawRoundRect(this.mRectF, this.mStrokeWidth * 2.0f, this.mStrokeWidth * 2.0f, this.mRectPaint);
        this.mRectPaint.setStrokeWidth(this.mStrokeWidth * 2.0f);
        canvas.drawLine(this.mCenterX - (this.mStrokeWidth * 4.0f), this.mRectF.top - this.mStrokeWidth, (this.mStrokeWidth * 4.0f) + this.mCenterX, this.mRectF.top - this.mStrokeWidth, this.mRectPaint);
    }

    public void startAnim(int power, long duration) {
        if (this.anim != null) {
            this.anim.cancel();
        }
        power = Math.max(0, Math.min(100, power));
        this.anim = ObjectAnimator.ofInt(this, "power", new int[]{this.mPower, power}).setDuration(duration);
        this.anim.start();
    }

    protected void setPower(int power) {
        this.mPower = power;
        invalidate();
    }
}
