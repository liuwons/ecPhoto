package com.lwons.ecphoto.db;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.lwons.ecphoto.model.Album;
import com.lwons.ecphoto.model.Photo;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * Created by liuwons on 2018/10/19
 */
public class DatabaseManager {
    private static final String DATABASE_NAME = "app_database";

    private static DatabaseManager mInstance;

    private boolean mInited = false;
    private AppDatabase mDatabase;

    public static DatabaseManager getInstance() {
        if (mInstance == null) {
            synchronized (DatabaseManager.class) {
                if (mInstance == null) {
                    mInstance = new DatabaseManager();
                }
            }
        }
        return mInstance;
    }

    public synchronized void init(Context context) {
        if (mInited) {
            return;
        }
        mDatabase = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).build();
        mInited = true;
    }

    private AppDAO getDAO() {
        return mDatabase.getDao();
    }

    public void addPhoto(Photo... photos) {
        getDAO().insertPhoto(photos);
    }

    public void deletePhoto(String photoId) {
        getDAO().deletePhoto(photoId);
    }

    public void addAlbum(Album... albums) {
        getDAO().insertAlbum(albums);
    }

    public void deleteAlbum(String album) {
        getDAO().deleteAlbum(album);
    }

    public Observable<List<Album>> asyncLoadAlbums() {
        return getDAO().asyncLoadAlbums().toObservable();
    }

    public Observable<List<Photo>> asyncLoadPhotos(String album) {
        return getDAO().asyncLoadPhotos(album).toObservable();
    }
}
