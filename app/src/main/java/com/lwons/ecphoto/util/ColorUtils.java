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
        if (mCardColors == null) {
            synchronized (ColorUtils.class) {
                if (mCardColors == null) {
                    mCardColors = context.getResources().getIntArray(R.array.card_colors);
                }
            }
        }

        Random random = new Random();
        return mCardColors[random.nextInt(mCardColors.length)];
    }
}
