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
    private Map<String, List<Photo>> mAlbumIdIndexedPhotos;

    public MemCache() {
        mAlbumList = new LinkedList<>();
        mIdIndexedPhotos = new HashMap<>();
        mAlbumIdIndexedPhotos = new HashMap<>();
    }

    public synchronized void deletePhoto(String photoId) {
        Photo photo = mIdIndexedPhotos.get(photoId);
        mIdIndexedPhotos.remove(photoId);
        if (photo != null) {
            String albumId = photo.albumId;
            mAlbumIdIndexedPhotos.get(albumId).remove(photo);
        }
    }

    public synchronized void deleteAlbum(String albumId) {
        if (!albumExists(albumId)) {
            return;
        }
        Album album = null;
        for (Album a : mAlbumList) {
            if (a.id.equals(albumId)) {
                album = a;
            }
        }
        if (album == null) {
            return;
        }
        mAlbumList.remove(album);
        List<Photo> photos = mAlbumIdIndexedPhotos.get(albumId);
        if (photos == null) {
            return;
        }
        for (Photo p : photos) {
            mIdIndexedPhotos.remove(p.photoId);
        }
        mAlbumIdIndexedPhotos.remove(albumId);
    }

    public synchronized void addPhoto(Photo photo) {
        mIdIndexedPhotos.put(photo.photoId, photo);
        mAlbumIdIndexedPhotos.get(photo.albumId).remove(photo);
    }

    public synchronized void addAlbum(Album album) {
        if (albumExists(album.id)) {
            return;
        }
        mAlbumList.add(album);
        mAlbumIdIndexedPhotos.put(album.id, new LinkedList<Photo>());
    }

    public synchronized boolean albumExists(String albumName) {
        return mAlbumIdIndexedPhotos.containsKey(albumName);
    }
}
