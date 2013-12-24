package com.gagaryn.play;

import java.util.List;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.gagaryn.play.audio_list.AlbumListFragment;
import com.gagaryn.play.containers.Album;
import com.gagaryn.play.containers.Song;
import com.gagaryn.play.player_fragment.PlayerFragment;
import com.gagaryn.play.player_fragment.PlayerSongList;
import com.gagaryn.play.utils.MusicUtils;

public class MainActivity extends FragmentActivity {

    public static final String SHARED_PREFERENCES =
            "com.gagaryn.play.MainActivity.SHARED_PREFERENCES";

    private DrawerLayout drawerLayout;
    private MenuItem switcher;
    private AudioService audioService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AudioService.LocalBinder localBinder = (AudioService.LocalBinder) iBinder;
            audioService = localBinder.getInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            audioService = null;
        }
    };

    ////////////////////////////////////////

    public void notifyPlaylistChanged() {
        if (audioService != null) {
            audioService.onPlaylistChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#33949287")));
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        createMainPlaylistIfNotExists();
        initLayout();

        Intent serviceIntent = new Intent(this, AudioService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);
    }

    private void initLayout() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, new AlbumListFragment())
                .commit();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_launcher,
                R.string.app_name,
                R.string.app_name) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    if (slideOffset > 0) {
                        actionBar.hide();
                    } else {
                        actionBar.show();
                    }
                }
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
    }

    private void createMainPlaylistIfNotExists() {
        long id = MusicUtils.getPlaylistId(getContentResolver(), "PlayNow");           

        if (id == -1) {
            id = MusicUtils.createNewPlaylist(getContentResolver(), "PlayNow");
        }

        if (id != -1) {
            SharedPreferences sharedPreferences =
                    getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE);
            if (!sharedPreferences.contains("ID")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("ID", id).commit();
            }

            if (!sharedPreferences.contains("INDEX")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("INDEX", 0).commit();
            }

            ContentResolver contentResolver = getContentResolver();
            List<Song> songList = MusicUtils.getAllSongsFromPlaylist(contentResolver, id);
            if (songList == null || songList.size() == 0) {
                List<Album> albums = MusicUtils.getAllAlbums(contentResolver);
                if (albums != null) {
                    int albumId = (int) albums.get(0).getId();
                    MusicUtils.clearPlaylist(contentResolver, id);
                    MusicUtils.addAlbumToPlaylist(contentResolver, id, albumId);
                }
            }
        }
    }

    public void play() {
        if (audioService != null) {
            audioService.play();
        }
    }

    public void pause() {
        if (audioService != null) {
            audioService.pause();
        }
    }

    public void next() {
        if (audioService != null) {
            audioService.next();
        }
    }

    public void previous() {
        if (audioService != null) {
            audioService.previous();
        }
    }

    public void startUpdateCallback() {
        if (audioService != null) {
            audioService.startUpdateCallback();
        }
    }

    public void clearUpdateCallback() {
        if (audioService != null) {
            audioService.clearUpdateCallback();
        }
    }

    public void seekTo(int position) {
        if (audioService != null) {
            audioService.seekTo(position);
        }
    }

    public void changeSongToIndex(int index) {
        if (audioService != null) {
            audioService.changeSongToIndex(index);
        }
    }

    public Song getCurrentSong() {
        if (audioService != null) {
            return audioService.getCurrentSong();
        }

        return null;
    }

    public boolean isPlaying() {
        return audioService != null && audioService.isPlaying();
    }

    public void shuffle() {
        if (audioService != null) {
            audioService.setShuffle(!audioService.isShuffle());
        }
    }

    public void showPlayerSongList() {
        audioService.setOnAudioStateChangeListener(null);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, new PlayerSongList())
                .commit();
    }

    public void showPlayerSongList(long albumId) {
        audioService.setOnAudioStateChangeListener(null);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, new PlayerSongList(albumId))
                .commit();
    }

    public void showPlayerFragment() {
        PlayerFragment playerFragment = new PlayerFragment();
        audioService.setOnAudioStateChangeListener(playerFragment);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, playerFragment)
                .commit();
    }

    public void showAudioList() {
        audioService.setOnAudioStateChangeListener(null);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, new AlbumListFragment())
                .commit();
    }

    public void setSwitcherIcon(int iconId) {
        if (switcher != null) {
            switcher.setIcon(iconId);
        }
    }

    public void closeNavDrawer() {
        drawerLayout.closeDrawers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (audioService != null) {
            Intent serviceIntent = new Intent(this, AudioService.class);
            stopService(serviceIntent);
            unbindService(connection);

            audioService = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        switcher = menu.findItem(R.id.switcher);
        assert switcher != null;
        switcher.setIcon(R.drawable.av_play);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        int current = ((CustomApplication) getApplication()).getCurrent();

        switch (id) {
            case android.R.id.home:
                switch (current) {
                    case CustomApplication.PLAYER:
                        showAudioList();
                        break;
                    case CustomApplication.SONG_LIST:
                        showAudioList();
                }

                return true;
            case R.id.switcher:
                if (current == CustomApplication.PLAYER) {
                    showPlayerSongList();
                } else {
                    showPlayerFragment();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int current = ((CustomApplication) getApplication()).getCurrent();
        switch (current) {
            case CustomApplication.AUDIO_LIST:
                super.onBackPressed();
                break;
            default:
                showAudioList();
        }
    }
}
