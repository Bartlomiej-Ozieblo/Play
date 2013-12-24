package com.gagaryn.play.containers;

import android.database.Cursor;
import android.provider.MediaStore;

public class Song {

    private long id;
    private String data;
    private String artist;
    private String title;
    private String album;
    private long duration;
    private int playOrder;

    public Song() {}

    public Song(Cursor cursor) {
        this.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members._ID));
        this.data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA));
        this.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
        this.album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM));
        this.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
        this.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DURATION));
        this.playOrder = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER));
    }

    public Song(Cursor cursor, int ignored) {
        this.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        this.data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        this.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        this.album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        this.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        this.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
        this.playOrder = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getPlayOrder() {
        return playOrder;
    }

    public void setPlayOrder(int playOrder) {
        this.playOrder = playOrder;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Song) {
            Song song = (Song) object;
            if (song.id == this.id) {
                return true;
            }
        }
        return false;
    }
}
