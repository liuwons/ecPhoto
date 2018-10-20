package com.lwons.ecphoto.data;

import com.lwons.ecphoto.db.DatabaseManager;
import com.lwons.ecphoto.model.Album;
import com.lwons.ecphoto.model.Photo;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by liuwons on 2018/10/19
 */
public class DataManager {
    private DatabaseManager mDatabaseManager;

    public DataManager() {
        mDatabaseManager = DatabaseManager.getInstance();
    }

    public Observable<Boolean> addAlbum(final String albumName) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                Album album = new Album();
                album.name = albumName;
                album.mCreateTime = System.currentTimeMillis();
                try {
                    DatabaseManager.getInstance().addAlbum(album);
                } catch (Throwable throwable) {
                    e.onError(throwable);
                }
                e.onNext(true);
                e.onComplete();
            }
        });
    }

    public Observable<Boolean> deleteAlbum(final String albumName) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) {
                try {
                    DatabaseManager.getInstance().deleteAlbum(albumName);
                } catch (Throwable throwable) {
                    e.onError(throwable);
                }
                e.onNext(true);
                e.onComplete();
            }
        });
    }

    public Observable<Boolean> asyncAddPhoto(final Photo photo) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) {
                try {
                    DatabaseManager.getInstance().addPhoto(photo);
                } catch (Throwable throwable) {
                    e.onError(throwable);
                }
                e.onNext(true);
                e.onComplete();
            }
        });
    }

    public void addPhoto(final Photo photo) {
        DatabaseManager.getInstance().addPhoto(photo);
    }

    public Observable<Boolean> deletePhoto(final String photoId) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) {
                try {
                    DatabaseManager.getInstance().deletePhoto(photoId);
                } catch (Throwable throwable) {
                    e.onError(throwable);
                }
                e.onNext(true);
                e.onComplete();
            }
        });
    }

    public Observable<List<Album>> loadAlbums() {
        return DatabaseManager.getInstance().asyncLoadAlbums();
    }

    public Observable<List<Photo>> loadPhotos(String albumName) {
        return DatabaseManager.getInstance().asyncLoadPhotos(albumName);
    }

}
