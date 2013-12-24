package com.gagaryn.play;

import android.app.Application;

public class CustomApplication extends Application {

    public static final int AUDIO_LIST = 0;
    public static final int PLAYER = 1;
    public static final int SONG_LIST = 2;
    private int current;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }
}
