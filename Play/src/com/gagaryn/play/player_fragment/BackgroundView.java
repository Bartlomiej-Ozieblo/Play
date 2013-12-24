package com.gagaryn.play.player_fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;

import com.gagaryn.play.utils.BitmapEffectFactory;

public class BackgroundView extends View {

    private final Paint paint = new Paint();
    private String albumArt;
    private LruCache<String, Bitmap> memoryCache;

    public BackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 10;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                oldValue.recycle();
            }
        };
    }

    public void setAlbumArt(String albumArt) {
        if (this.albumArt == null) {
            this.albumArt = albumArt;
            invalidate();
        } else {
            if (!this.albumArt.equals(albumArt)) {
                memoryCache.remove("background");
                this.albumArt = albumArt;
                invalidate();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (albumArt != null) {
            Bitmap bitmap = memoryCache.get("background");
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeFile(albumArt);
                bitmap = Bitmap.createScaledBitmap(bitmap, getHeight(), getHeight(), false);
//                Bitmap blurredBitmap = BitmapEffectFactory.createdBlurredBitmap(bitmap, 7);
//                Bitmap saturatedBitmap = BitmapEffectFactory.createDarkerBitmap(blurredBitmap);
                Bitmap saturatedBitmap = BitmapEffectFactory.createDarkerBitmap(bitmap);
                memoryCache.put("background", saturatedBitmap);
                canvas.drawBitmap(saturatedBitmap, getHeight() / 2 - bitmap.getWidth() / 2, 0, paint);
                bitmap.recycle();
            } else {
                canvas.drawBitmap(bitmap, getHeight() / 2 - bitmap.getWidth() / 2, 0, paint);
            }
        }
    }
}
