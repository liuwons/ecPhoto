package com.lwons.ecphoto.util;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by liuwons on 2018/10/19
 */
public class FileUtils {
    private static long mLastGeneratedId = System.currentTimeMillis();

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String generateId(String albumName) {
        String md5 = FileUtils.md5(albumName);
        if (!TextUtils.isEmpty(md5)) {
            return md5;
        } else {
            long id = System.currentTimeMillis();
            if (id == mLastGeneratedId) {
                id += 1;
            }
            mLastGeneratedId = id;
            return "" + id;
        }
    }
}
