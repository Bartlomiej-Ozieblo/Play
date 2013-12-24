package com.gagaryn.play.audio_list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.gagaryn.play.R;

public class ThumbnailLoader extends AsyncTask<String, Void, Drawable> {

    private ImageView thumbnail;
    private Context context;
    private int size;

    public ThumbnailLoader(ImageView thumbnail, Context context, int size) {
        this.thumbnail = thumbnail;
        this.context = context;
        this.size = size;
    }

    @Override
    protected Drawable doInBackground(String... strings) {
        Drawable img = null;
        if (strings[0] != null) {
            Drawable drawable = Drawable.createFromPath(strings[0]);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            img = new BitmapDrawable(context.getResources(),
                    Bitmap.createScaledBitmap(bitmap, size, size, true));
        }
        return img;
    }

    @Override
    protected void onPostExecute(Drawable img) {
        if (img != null) {
            Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
            thumbnail.setAnimation(fadeInAnimation);
            thumbnail.setImageDrawable(img);
            thumbnail.animate();
        }
    }
}
