package app.gotway.euc.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class ViewUtil {
    public static int getColorBetweenAB(int colorA, int colorB, float degree, int progress) {
        return Color.rgb((int) (((((float) (((colorB & 16711680) >> 16) - ((colorA & 16711680) >> 16))) / degree) * ((float) progress)) + ((float) ((colorA & 16711680) >> 16))), (int) (((((float) (((colorB & 65280) - (colorA & 65280)) >> 8)) / degree) * ((float) progress)) + ((float) ((colorA & 65280) >> 8))), (int) (((((float) ((colorB & 255) - (colorA & 255))) / degree) * ((float) progress)) + ((float) (colorA & 255))));
    }

    public static Bitmap blur(Bitmap b, Context context) {
        RenderScript rs = RenderScript.create(context);
        Allocation overlayAlloc = Allocation.createFromBitmap(rs, b);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());
        blur.setInput(overlayAlloc);
        blur.setRadius(25.0f);
        blur.forEach(overlayAlloc);
        overlayAlloc.copyTo(b);
        return b;
    }

    public static float getTextHeight(Paint paint) {
        FontMetrics m = paint.getFontMetrics();
        return m.bottom - m.top;
    }

    public static float getTextRectWidth(Paint paint, String content) {
        Rect rect = new Rect();
        paint.getTextBounds(content, 0, content.length(), rect);
        return (float) rect.width();
    }

    public static float getTextRectHeight(Paint paint, String content) {
        Rect rect = new Rect();
        paint.getTextBounds(content, 0, content.length(), rect);
        return (float) rect.height();
    }

    public static float px2Dp(int px, Context context) {
        return ((float) px) * context.getResources().getDisplayMetrics().density;
    }
}
