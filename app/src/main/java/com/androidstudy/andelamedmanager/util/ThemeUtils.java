package com.androidstudy.andelamedmanager.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class ThemeUtils {
    private ThemeUtils() {
    }

    public static int getThemeAttrColor(@NonNull Context context, @AttrRes int attributeColor) {
        int[] attrs = new int[]{attributeColor};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.TRANSPARENT);
        ta.recycle();
        return color;
    }

    public static int getDarkerColor(@ColorInt int color, float darkerAmount) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= darkerAmount;
        return Color.HSVToColor(hsv);
    }
}
