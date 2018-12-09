package com.lwons.ecphoto.util;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by liuwons on 2018/10/19
 */
public class FileUtils {
    private static long mLastGeneratedId = System.currentTimeMillis();

    public static String getAssetContent(Context context, String assetPath) {
        StringBuilder buf = new StringBuilder();
        BufferedReader in = null;
        try {
            InputStream json= context.getAssets().open(assetPath);
            in = new BufferedReader(new InputStreamReader(json));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }
        return buf.toString();
    }

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
