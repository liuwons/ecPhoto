package com.lwons.ecphoto.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.lwons.ecphoto.model.Album;
import com.lwons.ecphoto.model.Photo;

/**
 * Created by liuwons on 2018/10/19
 */

@Database(entities = {Album.class, Photo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppDAO getDao();
}
