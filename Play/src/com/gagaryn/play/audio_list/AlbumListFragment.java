package com.gagaryn.play.audio_list;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.gagaryn.play.CustomApplication;
import com.gagaryn.play.MainActivity;
import com.gagaryn.play.R;
import com.gagaryn.play.containers.Album;
import com.gagaryn.play.containers.Song;
import com.gagaryn.play.player_fragment.BackgroundView;
import com.gagaryn.play.utils.MusicUtils;

import java.util.List;

public class AlbumListFragment extends Fragment {

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            ((MainActivity) getActivity()).showPlayerSongList(id);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.album_gridview_fragment, container, false);
        assert rootView != null;
        BackgroundView backgroundView = (BackgroundView) rootView.findViewById(R.id.album_gridview_fragment_background);

        ContentResolver contentResolver = getActivity().getContentResolver();
        long id = MusicUtils.getPlaylistId(contentResolver, "PlayNow");
        Song song = MusicUtils.getAllSongsFromPlaylist(contentResolver, id).get(0);

        if (song != null) {
            String albumArt = MusicUtils.getAlbumArtFromSong(contentResolver, song);
            if (albumArt != null) {
                backgroundView.setAlbumArt(albumArt);
            }
        }

        GridView gridView = (GridView) rootView.findViewById(R.id.album_gridview);

        ArrayAdapter<Album> adapter = new CustomAdapter(getActivity(), 0);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(itemClickListener);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        ((CustomApplication) activity.getApplication()).setCurrent(CustomApplication.AUDIO_LIST);
        activity.setSwitcherIcon(R.drawable.av_play);
        activity.getActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private class CustomAdapter extends ArrayAdapter<Album> {

        private List<Album> albums;

        public CustomAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);

            albums = MusicUtils.getAllAlbums(getActivity().getContentResolver());
        }

        @Override
        public int getCount() {
            return albums.size();
        }

        @Override
        public Album getItem(int position) {
            return albums.get(position);
        }

        @Override
        public long getItemId(int position) {
            return albums.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rootView = layoutInflater.inflate(R.layout.album_grid, null, false);
            assert rootView != null;
            ImageView thumbnail = (ImageView) rootView.findViewById(R.id.album_grid_thumbnail);
            Album album = albums.get(position);
            new ThumbnailLoader(thumbnail, getContext(), 300).execute(album.getThumbnail());

            return rootView;
        }
    }
}
