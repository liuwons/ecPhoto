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
    public String photoId;

    @NonNull
    public String albumId;

    public String originUri;
    public String encryptedUri;
    public String encryptedFilePath;
    public long size;

    public long createTime;

    public String mExtras;
}
