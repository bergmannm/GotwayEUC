package app.gotway.euc.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

import app.gotway.euc.util.ViewUtil;

public class TemperatureView extends View {
    // private final float LINE_CIRCLE_SCALE;
    // private final int MAX_TEMPER;
    // private final int MIN_TEMPER;
    // private final float STROKE_WIDTH_SCALE;
    private ObjectAnimator anim;
    private int f0h;
    private Paint mBgPaint;
    private float mCenterX;
    private float mCenterY;
    private LinearGradient mLinearGradient;
    private int mMainColor;
    private RadialGradient mRadialGradientBig;
    private RadialGradient mRadialGradientSmall;
    private float mRadius;
    private float mSmallRadius;
    // private float mStokeWidth;
    private int mTemper;
    private int mTemperColor;
    // private LinearGradient mTemperGradient;
    private Paint mTemperPaint;
    private Path mTemperPath;
    private float mTemperTop;
    private int mTextColor;
    private Paint mTextPaint;
    private float mTextSize;
    // private float mTextTop;
    private int f1w;
    private float y0;
    private float yScale;

    public TemperatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // float STROKE_WIDTH_SCALE = 0.125f;
        // float LINE_CIRCLE_SCALE = 0.8f;
        // float MAX_TEMPER = 80;
        // float MIN_TEMPER = -30;
        this.mTemper = 0;
        this.mTextSize = 28.0f;
        this.mMainColor = -1;
        this.mTextColor = -1;
        this.mTemperColor = -65536;
        init();
    }

    private void init() {
        this.mBgPaint = new Paint(1);
        this.mBgPaint.setColor(this.mMainColor);
        this.mBgPaint.setStrokeJoin(Join.ROUND);
        this.mTextPaint = new Paint(1);
        this.mTextPaint.setTextAlign(Align.CENTER);
        this.mTextPaint.setColor(this.mTextColor);
        this.mTemperPaint = new Paint(1);
        this.mTemperPaint.setColor(this.mTemperColor);
        this.mTemperPaint.setStyle(Style.FILL);
        this.mTemperPaint.setStrokeCap(Cap.ROUND);
        this.mTemperPath = new Path();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.f1w = w;
        this.f0h = h;
        float actualWidth = (float) ((int) (((float) h) / 4.0f));
        float mStokeWidth = 0.125f * actualWidth;
        this.mRadius = (actualWidth / 2.0f) - mStokeWidth;
        this.mSmallRadius = this.mRadius * 0.6f;
        this.mTextSize = ((float) h) / 13.0f;
        this.mTextPaint.setTextSize(this.mTextSize);
        float mTextTop = (((float) (h - getPaddingBottom())) - ViewUtil.getTextHeight(this.mTextPaint)) - (((float) h) / 19.0f);
        this.y0 = (mTextTop - this.mRadius) - this.mSmallRadius;
        this.mTemperTop = ((float) getPaddingTop()) + (this.mSmallRadius * 0.8f);
        this.yScale = (this.y0 - this.mTemperTop) / 110.0f;
        this.mCenterX = (float) (w / 2);
        this.mCenterY = mTextTop - this.mRadius;
        setPath();
        initGradient();
    }

    private void initGradient() {
        int c = this.mMainColor;
        float f = this.mCenterX;
        float f2 = this.mTemperTop;
        float f3 = this.mSmallRadius * 0.8f;
        int[] iArr = new int[3];
        iArr[2] = c;
        this.mRadialGradientSmall = new RadialGradient(f, f2, f3, iArr, new float[]{0.0f, 0.08f, 1.0f}, TileMode.CLAMP);
        f = this.mCenterX;
        f2 = this.mCenterY;
        f3 = this.mRadius;
        iArr = new int[3];
        iArr[2] = c;
        this.mRadialGradientBig = new RadialGradient(f, f2, f3, iArr, new float[]{0.2f, 0.3f, 1.0f}, TileMode.CLAMP);
        f = this.mCenterX - (this.mSmallRadius * 0.8f);
        f3 = (this.mSmallRadius * 0.8f) + this.mCenterX;
        int[] iArr2 = new int[3];
        iArr2[0] = c;
        iArr2[2] = c;
        this.mLinearGradient = new LinearGradient(f, 0.0f, f3, 0.0f, iArr2, null, TileMode.CLAMP);
        f = 0.0f;
        f3 = 0.0f;
        float[] fArr = null;
        LinearGradient mTemperGradient = new LinearGradient(f, this.mRadius + this.mCenterY, f3, this.mTemperTop, new int[]{-16698673, -14288878, -65536}, fArr, TileMode.CLAMP);
        this.mTemperPaint.setShader(mTemperGradient);
    }

    private void setPath() {
        float left = this.mCenterX - (this.mSmallRadius * 0.8f);
        float right = this.mCenterX + (this.mSmallRadius * 0.8f);
        this.mTemperPath.moveTo(left, this.y0 - (this.mRadius * 0.268f));
        this.mTemperPath.lineTo(left, this.mTemperTop);
        float radius = this.mSmallRadius * 0.8f;
        this.mTemperPath.addArc(new RectF(left, this.mTemperTop - radius, right, this.mTemperTop + radius), 180.0f, 180.0f);
        this.mTemperPath.lineTo(right, this.y0 - (this.mRadius * 0.268f));
        this.mTemperPath.addArc(new RectF(this.mCenterX - this.mRadius, this.mCenterY - this.mRadius, this.mCenterX + this.mRadius, this.mCenterY + this.mRadius), -90.0f + ((float) 30.0D), (float) (360.0d - (2.0d * 30.0d)));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(this.mTemper + "\u2103", this.mCenterX, (float) (this.f0h - getPaddingBottom()), this.mTextPaint);
        drawTemper(canvas);
        drawBg(canvas);
    }

    private void drawTemper(Canvas canvas) {
        this.mTemperPaint.setStrokeWidth(1.0f);
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mRadius, this.mTemperPaint);
        this.mTemperPaint.setStrokeWidth((this.mSmallRadius * 0.8f) * 2.0f);
        canvas.drawLine(this.mCenterX, this.mCenterY, this.mCenterX, (this.mCenterY - this.mSmallRadius) - (((float) (this.mTemper + 30)) * this.yScale), this.mTemperPaint);
    }

    private void drawBg(Canvas canvas) {
        this.mBgPaint.setStyle(Style.STROKE);
        canvas.drawPath(this.mTemperPath, this.mBgPaint);
        float step = (this.y0 - this.mTemperTop) / 5.0f;
        float startX = this.mCenterX + (this.mSmallRadius * 0.8f);
        float stopX = this.mCenterX - (this.mSmallRadius * 0.3f);
        for (int i = 1; i < 5; i++) {
            float y = this.y0 - (((float) i) * step);
            canvas.drawLine(startX, y, stopX, y, this.mBgPaint);
        }
        this.mBgPaint.setStyle(Style.FILL);
        this.mBgPaint.setAlpha(220);
        int sc = canvas.saveLayer(0.0f, 0.0f, (float) this.f1w, (float) this.f0h, this.mBgPaint, Canvas.ALL_SAVE_FLAG);
        this.mBgPaint.setShader(this.mRadialGradientBig);
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mRadius, this.mBgPaint);
        this.mBgPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        this.mBgPaint.setShader(this.mLinearGradient);
        canvas.drawRect(this.mCenterX - (this.mSmallRadius * 0.8f), this.mTemperTop, this.mCenterX + (this.mSmallRadius * 0.8f), this.y0 - (0.2f * this.mRadius), this.mBgPaint);
        canvas.restoreToCount(sc);
        this.mBgPaint.setXfermode(null);
        this.mBgPaint.setShader(this.mRadialGradientSmall);
        canvas.drawArc(new RectF(this.mCenterX - (this.mSmallRadius * 0.8f), this.mTemperTop - (this.mSmallRadius * 0.8f), this.mCenterX + (this.mSmallRadius * 0.8f), this.mTemperTop + (this.mSmallRadius * 0.8f)), 180.0f, 180.0f, false, this.mBgPaint);
        this.mBgPaint.setShader(null);
        this.mBgPaint.setAlpha(255);
    }

    public void startAnim(int temper, long duration) {
        if (this.anim != null) {
            this.anim.cancel();
        }
        temper = Math.max(-30, Math.min(80, temper));
        if (Math.abs(this.mTemper - temper) > 5) {
            this.anim = ObjectAnimator.ofInt(this, "temper", this.mTemper, temper).setDuration(duration);
            this.anim.start();
        } else {
            this.setTemper(temper);
        }
    }

    protected void setTemper(int temper) {
        if (this.mTemper != temper) {
            this.mTemper = temper;
            invalidate();
        }
    }
}
