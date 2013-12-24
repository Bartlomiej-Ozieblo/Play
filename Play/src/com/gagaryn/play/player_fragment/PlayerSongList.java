package com.gagaryn.play.player_fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gagaryn.play.CustomApplication;
import com.gagaryn.play.MainActivity;
import com.gagaryn.play.R;
import com.gagaryn.play.containers.Album;
import com.gagaryn.play.containers.Song;
import com.gagaryn.play.utils.MusicUtils;

import java.util.List;

public class PlayerSongList extends Fragment {

    private long albumId;
    private List<Song> songs;

    public PlayerSongList(long albumId) {
        this.albumId = albumId;
    }

    public PlayerSongList() {
        this.albumId = -1;
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            MainActivity mainActivity = (MainActivity) getActivity();

            SharedPreferences preferences =
                    mainActivity.getSharedPreferences(MainActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            edit.putInt("INDEX", i).commit();
            if (albumId != -1) {
                ContentResolver contentResolver = getActivity().getContentResolver();
                long playlistId = preferences.getLong("ID", -1);

                MusicUtils.clearPlaylist(contentResolver, playlistId);
                MusicUtils.addAlbumToPlaylist(contentResolver, playlistId, albumId);
                mainActivity.notifyPlaylistChanged();
                mainActivity.showPlayerFragment();
            } else {
                mainActivity.changeSongToIndex(i);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_songlist_fragment, container, false);

        assert rootView != null;
        BackgroundView background =
                (BackgroundView) rootView.findViewById(R.id.player_songlist_background);
        ListView songListView = (ListView) rootView.findViewById(R.id.player_songlistview);
        songListView.setDivider(new ColorDrawable(0x00000000));
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), 0);
        songListView.setAdapter(customAdapter);
        songListView.setOnItemClickListener(itemClickListener);

        MainActivity activity = (MainActivity) getActivity();
        if (albumId == -1) {
            ContentResolver contentResolver = activity.getContentResolver();
            long playlistId = MusicUtils.getPlaylistId(contentResolver, "PlayNow");
            SharedPreferences sharedPreferences =
                    activity.getSharedPreferences(MainActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            int currentSongIndex = sharedPreferences.getInt("INDEX", 0);
            Song song = MusicUtils.getAllSongsFromPlaylist(contentResolver, playlistId).get(currentSongIndex);
            String albumArt = MusicUtils.getAlbumArtFromSong(contentResolver, song);

            if (albumArt != null) {
                background.setAlbumArt(albumArt);
            }
        } else {
            Album album = MusicUtils.getAlbum(getActivity().getContentResolver(), albumId);
            String albumArt = album.getThumbnail();
            songs = MusicUtils.getSongsFromAlbum(getActivity().getContentResolver(), albumId);

            if (albumArt != null) {
                background.setAlbumArt(albumArt);
            }
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((CustomApplication) getActivity().getApplication()).setCurrent(CustomApplication.SONG_LIST);
        MainActivity activity = (MainActivity) getActivity();
        activity.getActionBar().setDisplayHomeAsUpEnabled(true);
        activity.setSwitcherIcon(R.drawable.av_play);
    }

    private class CustomAdapter extends ArrayAdapter<Song> {

        private Typeface typeface;

        public CustomAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);

            typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Raleway Thin.ttf");

            ContentResolver contentResolver = getActivity().getContentResolver();
            SharedPreferences sharedPreferences =
                    getActivity().getSharedPreferences(MainActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            long playlistId = sharedPreferences.getLong("ID", -1);
            songs = MusicUtils.getAllSongsFromPlaylist(contentResolver, playlistId);
        }

        @Override
        public int getCount() {
            return songs.size();
        }

        @Override
        public Song getItem(int position) {
            return songs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return songs.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.player_song_row, null, false);
            }

            assert convertView != null;
            TextView titleLabel = (TextView) convertView.findViewById(R.id.player_song_row_title_label);
            titleLabel.setText(songs.get(position).getTitle());
            titleLabel.setTypeface(typeface);

            return convertView;
        }
    }
}
