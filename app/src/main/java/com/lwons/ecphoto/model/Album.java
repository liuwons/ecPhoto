package com.lwons.ecphoto.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by liuwons on 2018/10/19
 */

@Entity (tableName = "album")
public class Album {
    @PrimaryKey
    @NonNull
    public String name;

    public long mCreateTime;

    public String mExtras;
}
