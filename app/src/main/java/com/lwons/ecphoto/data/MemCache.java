package com.lwons.ecphoto.data;

import com.lwons.ecphoto.model.Album;
import com.lwons.ecphoto.model.Photo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuwons on 2018/10/20
 */
public class MemCache {
    // album list
    private List<Album> mAlbumList;
    // photo id to photo
    private Map<String, Photo> mIdIndexedPhotos;
    // album name to photos
    private Map<String, List<Photo>> mAlbumNameIndexedPhotos;

    public MemCache() {
        mAlbumList = new LinkedList<>();
        mIdIndexedPhotos = new HashMap<>();
        mAlbumNameIndexedPhotos = new HashMap<>();
    }

    public synchronized void deletePhoto(String photoId) {
        Photo photo = mIdIndexedPhotos.get(photoId);
        mIdIndexedPhotos.remove(photoId);
        if (photo != null) {
            String albumName = photo.album;
            mAlbumNameIndexedPhotos.get(albumName).remove(photo);
        }
    }

    public synchronized void deleteAlbum(String albumName) {
        if (!albumExists(albumName)) {
            return;
        }
        Album album = null;
        for (Album a : mAlbumList) {
            if (a.name.equals(albumName)) {
                album = a;
            }
        }
        if (album == null) {
            return;
        }
        mAlbumList.remove(album);
        List<Photo> photos = mAlbumNameIndexedPhotos.get(albumName);
        if (photos == null) {
            return;
        }
        for (Photo p : photos) {
            mIdIndexedPhotos.remove(p.photoId);
        }
        mAlbumNameIndexedPhotos.remove(albumName);
    }

    public synchronized void addPhoto(Photo photo) {
        mIdIndexedPhotos.put(photo.photoId, photo);
        mAlbumNameIndexedPhotos.get(photo.album).remove(photo);
    }

    public synchronized void addAlbum(Album album) {
        if (albumExists(album.name)) {
            return;
        }
        mAlbumList.add(album);
        mAlbumNameIndexedPhotos.put(album.name, new LinkedList<Photo>());
    }

    public synchronized boolean albumExists(String albumName) {
        return mAlbumNameIndexedPhotos.containsKey(albumName);
    }
}
