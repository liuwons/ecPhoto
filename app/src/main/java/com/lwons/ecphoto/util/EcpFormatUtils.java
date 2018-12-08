package com.lwons.ecphoto.util;

import android.net.Uri;

import com.lwons.ecphoto.neo.Neo;

/**
 * Created by liuwons on 18-12-7.
 */
public class EcpFormatUtils {
    public static String getPhotoId(Uri ecpUri) {
        return ecpUri.getPath();
    }

    public static String getAlbumId(Uri ecpUri) {
        return ecpUri.getHost();
    }

    public static String getCacheFilePath(Uri ecpUri) {
        return Neo.getInstance().getDecryptPathInCache(getAlbumId(ecpUri), getPhotoId(ecpUri));
    }
}
