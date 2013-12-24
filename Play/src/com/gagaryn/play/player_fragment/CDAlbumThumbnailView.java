package com.gagaryn.play.player_fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;

import com.gagaryn.play.utils.BitmapEffectFactory;

public class CDAlbumThumbnailView extends View {

    private final Paint paint = new Paint();
    private float radius;
    private int topOffset;
    private String albumArt = null;
    private LruCache<String, Bitmap> memoryCache;

    public CDAlbumThumbnailView(Context context, AttributeSet attrs) {
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

    public void setData(String albumArt, float radius, int topOffset) {
        if (this.albumArt == null) {
            this.albumArt = albumArt;
            this.radius = radius;
            this.topOffset = topOffset;
            invalidate();
        } else {
            if (!this.albumArt.equals(albumArt)) {
                memoryCache.remove("circle");
                memoryCache.remove("blurred");
                this.albumArt = albumArt;
                this.radius = radius;
                this.topOffset = topOffset;
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

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        if (albumArt != null) {
            Bitmap srcBitmap = memoryCache.get("blurred");
            if (srcBitmap == null) {
                srcBitmap = BitmapFactory.decodeFile(albumArt);
                Bitmap bitmap = Bitmap.createScaledBitmap(srcBitmap, getHeight(), getHeight(), false);
//                Bitmap blurredBitmap = BitmapEffectFactory.createdBlurredBitmap(bitmap, 7);
//                Bitmap monochromaticBitmap = BitmapEffectFactory.createDarkerBitmap(blurredBitmap);
                Bitmap monochromaticBitmap = BitmapEffectFactory.createDarkerBitmap(bitmap);
                memoryCache.put("blurred", monochromaticBitmap);
                canvas.drawBitmap(monochromaticBitmap, 0, 0, paint);
                bitmap.recycle();
            } else {
                canvas.drawBitmap(srcBitmap, 0, 0, paint);
            }

            Bitmap cdBitmap = memoryCache.get("circle");
            if (cdBitmap == null) {
                Bitmap bitmap = Bitmap.createScaledBitmap(srcBitmap, getWidth(), getWidth(), false);
                Bitmap circleBitmap = BitmapEffectFactory.createCircleBitmap(bitmap, radius);
                Bitmap ringBitmap = BitmapEffectFactory.drawCircleOnBitmap(circleBitmap, bitmap.getWidth() / 6);
                cdBitmap = BitmapEffectFactory.createCutCircleBitmap(ringBitmap, bitmap.getWidth() / 12);
                memoryCache.put("circle", cdBitmap);
                canvas.drawBitmap(cdBitmap, 0, topOffset, paint);
                bitmap.recycle();
                circleBitmap.recycle();
                ringBitmap.recycle();
            } else {
                canvas.drawBitmap(cdBitmap, 0, topOffset, paint);
            }
        }
    }

}
