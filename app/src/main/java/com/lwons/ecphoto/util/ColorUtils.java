package com.lwons.ecphoto.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.lwons.ecphoto.R;

import java.util.Random;

/**
 * Created by liuwons on 2018/10/21
 */
public class ColorUtils {

    public static int[] mCardColors;
    public static int randomCardColor(@NonNull Context context) {
        asureInited(context);

        Random random = new Random();
        return mCardColors[random.nextInt(mCardColors.length)];
    }

    public static int generateColor(@NonNull Context context, String text) {
        asureInited(context);

        byte[] bytes = text.getBytes();
        int sum = 0;
        for (byte b : bytes) {
            sum += b;
        }
        sum = Math.abs(sum);
        return mCardColors[sum % mCardColors.length];
    }

    private static void asureInited(Context context) {
        if (mCardColors == null) {
            synchronized (ColorUtils.class) {
                if (mCardColors == null) {
                    mCardColors = context.getResources().getIntArray(R.array.card_colors);
                }
            }
        }
    }
}
