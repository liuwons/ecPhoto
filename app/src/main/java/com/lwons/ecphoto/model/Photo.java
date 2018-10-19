package com.lwons.ecphoto.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by liuwons on 2018/10/19
 */

@Entity (tableName = "photo")
public class Photo {
    @PrimaryKey
    @NonNull
    public String mId;

    @NonNull
    public String mAlbum;

    public String mOriginFilePath;
    public String mEncryptedPFileath;
    public long mSize;

    public long mCreateTime;
}
