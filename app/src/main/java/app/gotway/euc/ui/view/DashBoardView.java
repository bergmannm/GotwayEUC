package app.gotway.euc.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import app.gotway.euc.data.Data0x00;
import app.gotway.euc.util.ViewUtil;

public class DashBoardView extends View {
    private static final float START_ANGLE = 135.0f;
    private static final float STROKE_WIDTH_SCALE = 0.006666667f;
    private static final float SWEEP_ANGEL = 270.0f;
    private ObjectAnimator anim;
    private float mCenterX;
    private float mCenterY;
    private int mDistance;
    private Paint mLinePaint;
    private Paint mPointPaint;
    private float mRadius;
    private float mSpeed;
    private float mStrokeWidth;
    private Paint mTextPaint;
    private float mTextSize;
    private float mTotalDistance;

    public DashBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSpeed = 0.0f;
        this.mTotalDistance = 0.0f;
        this.mDistance = 0;
        this.mLinePaint = new Paint(1);
        this.mLinePaint.setColor(-1);
        this.mLinePaint.setStyle(Style.STROKE);
        this.mTextPaint = new Paint(33);
        this.mTextPaint.setTypeface(Typeface.defaultFromStyle(2));
        this.mTextPaint.setTextAlign(Align.CENTER);
        this.mTextPaint.setColor(-1);
        this.mTextPaint.setTextSize(28.0f);
        this.mPointPaint = new Paint(1);
        this.mPointPaint.setColor(-65536);
        this.mPointPaint.setMaskFilter(new BlurMaskFilter(10.0f, Blur.SOLID));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mStrokeWidth = ((float) w) * STROKE_WIDTH_SCALE;
        if (((float) (w / 2)) > ((float) h) / 1.98f) {
            this.mRadius = ((((float) h) / 0.98f) / 2.0f) - (this.mStrokeWidth / 2.0f);
        } else {
            this.mRadius = ((float) (w / 2)) - (this.mStrokeWidth / 2.0f);
        }
        this.mCenterX = (float) (w / 2);
        this.mCenterY = this.mRadius + (this.mStrokeWidth / 2.0f);
        this.mLinePaint.setStrokeWidth(this.mStrokeWidth);
        this.mPointPaint.setStrokeWidth(this.mStrokeWidth);
        this.mTextSize = this.mStrokeWidth * 6.5f;
        this.mTextPaint.setTextSize(this.mTextSize);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRuler(canvas);
        drawText(canvas);
        drawPoint(canvas);
    }

    private void drawPoint(Canvas canvas) {
        setLayerType(1, this.mPointPaint);
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mStrokeWidth * 3.0f, this.mPointPaint);
        canvas.save();
        canvas.rotate(START_ANGLE + (5.4f * this.mSpeed), this.mCenterX, this.mCenterY);
        canvas.drawLine(this.mCenterX, this.mCenterY, (this.mCenterX + this.mRadius) - (this.mStrokeWidth * 25.0f), this.mCenterY, this.mPointPaint);
        canvas.restore();
    }

    private void drawText(Canvas canvas) {
        float y = this.mCenterY - (10.0f * this.mStrokeWidth);
        canvas.drawText(String.format("%.1f", new Object[]{Float.valueOf(this.mSpeed)}), this.mCenterX, y, this.mTextPaint);
        canvas.drawText("km/h", this.mCenterX, y - (ViewUtil.getTextHeight(this.mTextPaint) + (this.mStrokeWidth * 3.0f)), this.mTextPaint);
        y = this.mCenterY + (this.mRadius * 0.5f);
        canvas.drawText(this.mDistance + " m", this.mCenterX, y, this.mTextPaint);
        canvas.drawText(String.format("%.1f km", new Object[]{Float.valueOf(this.mTotalDistance)}), this.mCenterX, y + (ViewUtil.getTextHeight(this.mTextPaint) + (this.mStrokeWidth * 3.0f)), this.mTextPaint);
    }

    private void drawRuler(Canvas canvas) {
        RectF rectF = new RectF();
        rectF.left = this.mCenterX - this.mRadius;
        rectF.top = this.mCenterY - this.mRadius;
        rectF.right = this.mCenterX + this.mRadius;
        rectF.bottom = this.mCenterY + this.mRadius;
        canvas.drawArc(rectF, START_ANGLE, SWEEP_ANGEL, false, this.mLinePaint);
        float longLineLength = this.mStrokeWidth * 13.0f;
        float shortLinteLength = this.mStrokeWidth * 8.0f;
        float x = (this.mCenterX + this.mRadius) + (this.mStrokeWidth / 2.0f);
        for (int i = 0; i < 51; i++) {
            int r = canvas.save();
            float degree = START_ANGLE + ((((float) i) * SWEEP_ANGEL) / 50.0f);
            canvas.rotate(degree, this.mCenterX, this.mCenterY);
            if (i % 5 == 0) {
                canvas.drawLine(x, this.mCenterY, x - longLineLength, this.mCenterY, this.mLinePaint);
                int textS = canvas.save();
                canvas.translate(x - (1.6f * longLineLength), this.mCenterY);
                canvas.rotate(-degree);
                canvas.drawText(new StringBuilder(String.valueOf(i)).toString(), 0.0f, 0.0f, this.mTextPaint);
                canvas.restoreToCount(textS);
            } else {
                canvas.drawLine(x, this.mCenterY, x - shortLinteLength, this.mCenterY, this.mLinePaint);
            }
            canvas.restoreToCount(r);
        }
    }

    public void setData(Data0x00 data, long duration) {
        this.mDistance = data.distance;
        this.mTotalDistance = data.totalDistance;
        startSpeedAnim(data.speed, duration);
        invalidate();
    }

    protected void setSpeed(float speed) {
        this.mSpeed = Math.max(0.0f, Math.min(50.0f, speed));
        invalidate();
    }

    private void startSpeedAnim(float finalSpeed, long duration) {
        if (this.anim != null) {
            this.anim.cancel();
        }
        this.anim = ObjectAnimator.ofFloat(this, "speed", new float[]{this.mSpeed, finalSpeed}).setDuration(duration);
        this.anim.start();
    }
}
