package com.gagaryn.play;

import com.gagaryn.play.containers.Song;

public interface OnAudioStateChangeListener {
    public void onSongChange(Song song, int historicalPosition, int currentPosition);

    public void onAudioPlay();

    public void onAudioPause();

    public void onProgressChanged(long progress, long duration);
}
