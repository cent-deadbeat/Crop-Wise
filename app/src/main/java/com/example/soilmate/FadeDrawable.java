package com.example.soilmate;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class FadeDrawable extends Drawable {
    private Bitmap bitmap;
    private Paint paint;
    private int fadeWidth; // Width of the fade effect

    public FadeDrawable(Bitmap bitmap, int fadeWidth) {
        this.bitmap = bitmap;
        this.fadeWidth = fadeWidth;
        this.paint = new Paint();
    }

    @Override
    public void draw(Canvas canvas) {
        // Draw the image
        canvas.drawBitmap(bitmap, 0, 0, null);

        // Apply a fade effect to the left side
        LinearGradient gradient = new LinearGradient(
                0, 0, fadeWidth, 0, // Fade from left to fadeWidth
                Color.TRANSPARENT, Color.BLACK,
                Shader.TileMode.CLAMP
        );
        Paint fadePaint = new Paint();
        fadePaint.setShader(gradient);
        fadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        // Save the canvas layer
        int saved = canvas.saveLayer(0, 0, getBounds().width(), getBounds().height(), null, Canvas.ALL_SAVE_FLAG);

        // Draw the gradient
        canvas.drawRect(0, 0, fadeWidth, getBounds().height(), fadePaint);

        // Restore the canvas layer
        canvas.restoreToCount(saved);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
}