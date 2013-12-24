package com.gagaryn.play.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.gagaryn.play.containers.Album;
import com.gagaryn.play.containers.Song;

import java.util.ArrayList;
import java.util.List;

public class MusicUtils {

    public static List<Song> getSongsFromAlbum(ContentResolver contentResolver, long albumId) {
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.ALBUM_ID
        };

        String selection = MediaStore.Audio.Media.ALBUM_ID + "=" + albumId;

        Cursor cursor = contentResolver
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        null,
                        MediaStore.Audio.Media.TRACK);


        if (cursor != null) {
            List<Song> songs = new ArrayList<Song>();
            while (cursor.moveToNext()) {
                songs.add(new Song(cursor, 0));
            }

            return songs;
        }

        return null;
    }

    public static long getPlaylistId(ContentResolver contentResolver, String playlistName) {
        String[] projection = {
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME,
        };

        String selection = MediaStore.Audio.Playlists.NAME + " = \"" + playlistName + "\"";

        Cursor cursor = contentResolver
                .query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        null,
                        null);

        if (cursor != null) {
            int index = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
            while (cursor.moveToNext()) {
                String name = cursor.getString(index);
                if (name != null && name.equals(playlistName)) {
                    return cursor.getLong(0);
                }
            }
        }

        return -1;
    }

    public static Album getAlbum(ContentResolver contentResolver, long albumId) {
        String[] projection = {
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ALBUM_ART
        };

        String selection = MediaStore.Audio.Albums._ID + "=" + albumId;

        Cursor cursor = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Albums.ALBUM);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Album album = new Album();
                album.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums._ID)));
                album.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));
                album.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
                album.setThumbnail(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));

                return album;
            }
        }

        return null;
    }

    public static long createNewPlaylist(ContentResolver contentResolver, String playlistName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Playlists.NAME, playlistName);
        contentValues.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
        contentValues.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());

        Uri uri = contentResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, contentValues);

        String[] projection = {
                MediaStore.Audio.Playlists._ID
        };

        if (uri != null) {
            Cursor cursor = contentResolver.query(uri, projection, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                return cursor.getLong(0);
            }
        }

        return -1;
    }

    public static void addAlbumToPlaylist(ContentResolver contentResolver, long playlistId, long id) {
        String[] projection = {
                MediaStore.Audio.Media._ID,
        };

        String selection = MediaStore.Audio.Media.ALBUM_ID + "=" + id;

        Cursor cursor = contentResolver
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        null,
                        MediaStore.Audio.Media.TRACK);

        if (cursor != null) {
            int base = getLastTrackNumber(contentResolver, playlistId) + 1;

            while (cursor.moveToNext()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, cursor.getLong(0));
                contentValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base);
                base++;

                contentResolver.insert(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId), contentValues);
            }
        }
    }

    public static int getLastTrackNumber(ContentResolver contentResolver, long playlistId) {
        String[] projection = {
                MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER
        };

        String selection = MediaStore.Audio.Playlists.Members.PLAYLIST_ID + "=" + playlistId;

        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                projection,
                selection,
                null,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER
        );

        if (cursor != null && cursor.moveToLast()) {
            return cursor.getInt(1);
        } else {
            return -1;
        }
    }

    public static List<Song> getAllSongsFromPlaylist(ContentResolver contentResolver, long playlistId) {
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


        if (cursor != null) {
            List<Song> songs = new ArrayList<Song>();
            while (cursor.moveToNext()) {
                songs.add(new Song(cursor));
            }

            return songs;
        }

        return null;
    }

    public static void clearPlaylist(ContentResolver contentResolver, long playlistId) {
        contentResolver.delete(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId), null, null);
    }

    public static String getAlbumArtFromSong(ContentResolver contentResolver, Song song) {
        String[] projection = {
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM_ART
        };

        String selection = MediaStore.Audio.Albums.ALBUM + " = \"" + song.getAlbum() + "\"";

        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        if (cursor != null) if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }

        return null;
    }

    public static List<Album> getAllAlbums(ContentResolver contentResolver) {
        String[] projection = {
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ALBUM_ART
        };

        Cursor cursor = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Albums.ALBUM);


        List<Album> albums = null;
        if (cursor != null) {
            albums = new ArrayList<Album>();
            while (cursor.moveToNext()) {
                Album album = new Album();
                album.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums._ID)));
                album.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));
                album.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
                album.setThumbnail(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));

                albums.add(album);
            }
        }

        return albums;
    }

    public static List<Song> getAllSongs(ContentResolver contentResolver) {
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE
        );

        List<Song> songs = null;
        if (cursor != null) {
            songs = new ArrayList<Song>();
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                song.setData(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                song.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                song.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                song.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                song.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                song.setPlayOrder(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)));
                songs.add(song);
            }
        }

        return songs;
    }
}
