package com.lwons.ecphoto.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.lwons.ecphoto.model.Album;
import com.lwons.ecphoto.model.Photo;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by liuwons on 2018/10/19
 */

@Dao
public interface AppDAO {
    @Query("SELECT * FROM album")
    List<Album> loadAllAlbum();

    @Query("SELECT * FROM photo WHERE  album = :album")
    List<Photo> loadAllPhotos(String album);

    @Insert
    void insertPhoto(Photo... photos);

    @Query("DELETE FROM photo WHERE  photoId = :photoId")
    void deletePhoto(String photoId);

    @Insert
    void insertAlbum(Album... albums);

    @Query("DELETE FROM album WHERE  name = :albumName")
    void deleteAlbum(String albumName);

    @Query("SELECT * FROM album")
    Flowable<List<Album>> asyncLoadAlbums();

    @Query("SELECT * FROM photo WHERE  album = :album")
    Flowable<List<Photo>> asyncLoadPhotos(String album);
}
