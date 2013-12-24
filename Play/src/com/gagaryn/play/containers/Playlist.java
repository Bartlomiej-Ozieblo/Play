package com.gagaryn.play.containers;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private List<Song> songs = new ArrayList<Song>();
    private int index = 0;
    private int historicalIndex = 0;

    public static Playlist createPlaylist(ContentResolver contentResolver, SharedPreferences sharedPreferences) {
        long playlistId = sharedPreferences.getLong("ID", -1);

        if (playlistId != -1) {
            String[] projection = {
                    MediaStore.Audio.Playlists.Members._ID,
                    MediaStore.Audio.Playlists.Members.DATA,
                    MediaStore.Audio.Playlists.Members.ARTIST,
                    MediaStore.Audio.Playlists.Members.TITLE,
                    MediaStore.Audio.Playlists.Members.ALBUM,
                    MediaStore.Audio.Playlists.Members.DURATION,
                    MediaStore.Audio.Playlists.Members.PLAY_ORDER
            };

            Cursor cursor = contentResolver
                    .query(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                            projection,
                            null,
                            null,
                            MediaStore.Audio.Playlists.Members.PLAY_ORDER);

            return new Playlist(cursor);
        }
        return null;
    }

    public Playlist(Cursor cursor) {
        while (cursor.moveToNext()) {
            songs.add(new Song(cursor));
        }
    }

    public int getSize() {
        return songs.size();
    }

    public void refresh(ContentResolver contentResolver, SharedPreferences sharedPreferences) {
        long playlistId = sharedPreferences.getLong("ID", -1);

        if (playlistId != -1) {
            String[] projection = {
                    MediaStore.Audio.Playlists.Members._ID,
                    MediaStore.Audio.Playlists.Members.DATA,
                    MediaStore.Audio.Playlists.Members.ARTIST,
                    MediaStore.Audio.Playlists.Members.TITLE,
                    MediaStore.Audio.Playlists.Members.ALBUM,
                    MediaStore.Audio.Playlists.Members.DURATION,
                    MediaStore.Audio.Playlists.Members.PLAY_ORDER
            };

            Cursor cursor = contentResolver
                    .query(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                            projection,
                            null,
                            null,
                            MediaStore.Audio.Playlists.Members.PLAY_ORDER);

            songs = null;
            songs = new ArrayList<Song>();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    songs.add(new Song(cursor));
                }
            }
        }
    }

    public boolean hasIndex(int index) {
        return index < songs.size();
    }

    public Song getCurrentSong() {
        return songs.get(index);
    }

    public boolean hasPreviousSong() {
        int prvIndex = index - 1;

        return prvIndex >= 0 && prvIndex < songs.size();
    }

    public boolean hasNextSong() {
        return index + 1 < songs.size();
    }

    public Song firstSong() {
        index = 0;
        return songs.get(0);
    }

    public Song nextSong() {
        historicalIndex = index;
        index++;

        return songs.get(index);
    }

    public Song previousSong() {
        historicalIndex = index;
        index--;

        return songs.get(index);
    }

    public void setIndex(int index) {
        this.historicalIndex = this.index;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int getHistoricalIndex() {
        return historicalIndex;
    }
}
