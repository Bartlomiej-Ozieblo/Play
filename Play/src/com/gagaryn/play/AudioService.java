package com.gagaryn.play;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.gagaryn.play.containers.Playlist;
import com.gagaryn.play.containers.Song;
import com.gagaryn.play.utils.TimeUtils;

import java.io.IOException;
import java.util.Random;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {

    private IBinder binder = new LocalBinder();
    private SharedPreferences sharedPreferences;
    private MediaPlayer mediaPlayer;
    private Playlist playlist;
    private Song currentSong;
    private OnAudioStateChangeListener listener = null;
    private Handler handler = new Handler();
    private boolean paused = false;
    private boolean shuffle = false;

    public class LocalBinder extends Binder {
        public AudioService getInstance() {
            return AudioService.this;
        }
    }

    public class AudioNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pause();
            }
        }
    }

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            if (listener != null) {
                listener.onProgressChanged(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
            }

            handler.postDelayed(this, 50);
        }
    };

    ////////////////////////////

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        next();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;

        handler.removeCallbacks(updateTimeTask);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        registerReceiver(new AudioNoisyReceiver(), new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        playlist = Playlist.createPlaylist(getContentResolver(), sharedPreferences);
        currentSong = playlist.firstSong();
    }

    public void play() {
        if (!mediaPlayer.isPlaying() && !paused) {
            try {
                if (currentSong != null) {
                    playSong(currentSong);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!mediaPlayer.isPlaying() && paused) {
            mediaPlayer.start();
        }

        if (listener != null) {
            listener.onAudioPlay();
        }

        paused = false;
    }

    public void pause() {
        mediaPlayer.pause();
        if (listener != null) {
            listener.onAudioPause();
        }
        paused = true;
    }

    public void next() {
        if (shuffle) {
            Random random = new Random();
            playlist.setIndex(random.nextInt(playlist.getSize()));
            currentSong = playlist.getCurrentSong();
        } else {
            if (playlist.hasNextSong()) {
                currentSong = playlist.nextSong();
            } else {
                return;
            }
        }

        saveIndexToSharedPreferences(playlist.getIndex());

        if (listener != null) {
            listener.onSongChange(currentSong, playlist.getHistoricalIndex(), playlist.getIndex());
            listener.onAudioPlay();
        }

        try {
            playSong(currentSong);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void previous() {
        if (shuffle) {
            Random random = new Random();
            playlist.setIndex(random.nextInt(playlist.getSize()));
            currentSong = playlist.getCurrentSong();
        } else {
            if (playlist.hasPreviousSong()) {
                currentSong = playlist.previousSong();
            } else {
                return;
            }
        }

        saveIndexToSharedPreferences(playlist.getIndex());

        if (listener != null) {
            listener.onSongChange(currentSong, playlist.getHistoricalIndex(), playlist.getIndex());
            listener.onAudioPlay();
        }

        try {
            playSong(currentSong);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void onPlaylistChanged() {
        sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE);
        playlist.refresh(getContentResolver(), sharedPreferences);
        int index = sharedPreferences.getInt("INDEX", 0);
        playlist.setIndex(index);

        currentSong = playlist.getCurrentSong();

        try {
            playSong(currentSong);
        } catch (IOException e) {
            e.printStackTrace();
        }

        paused = false;
    }

    public void changeSongToIndex(int index) {
        if (playlist.hasIndex(index)) {
            saveIndexToSharedPreferences(index);
            playlist.setIndex(index);

            currentSong = playlist.getCurrentSong();
            if (listener != null) {
                listener.onSongChange(currentSong, playlist.getHistoricalIndex(), index);
                listener.onAudioPlay();
            }

            try {
                playSong(currentSong);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setOnAudioStateChangeListener(OnAudioStateChangeListener listener) {
        this.listener = listener;
    }

    public void startUpdateCallback() {
        handler.postDelayed(updateTimeTask, 50);
    }

    public void clearUpdateCallback() {
        handler.removeCallbacks(updateTimeTask);
    }

    public void updateProgress() {
        if (listener != null) {
            listener.onProgressChanged(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
        }
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(TimeUtils.progressToTimer(position, mediaPlayer.getDuration()));
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    private void playSong(Song song) throws IOException {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(song.getData()));
        mediaPlayer.prepare();
        mediaPlayer.start();

        paused = false;
    }

    private void saveIndexToSharedPreferences(int index) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("INDEX", index).commit();
    }
}
