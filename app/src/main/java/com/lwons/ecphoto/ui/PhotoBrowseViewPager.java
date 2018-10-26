package com.lwons.ecphoto.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by liuwons on 2018/10/23
 */
public class PhotoBrowseViewPager extends ViewPager {
    private int touchSlop;
    private boolean scrollEnabled = true;
    private boolean isDisallowIntercept = false;

    public PhotoBrowseViewPager(@NonNull Context context) {
        this(context, null);
    }

    public PhotoBrowseViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        final ViewConfiguration vc = ViewConfiguration.get(getContext());
        touchSlop = vc.getScaledTouchSlop();
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        this.scrollEnabled = scrollEnabled;
    }

    public boolean isDisallowIntercept() {
        return isDisallowIntercept;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!scrollEnabled || event.getPointerCount() > 1) {
            return false;
        }
        try {
            return super.onInterceptTouchEvent(event);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        isDisallowIntercept = disallowIntercept;
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    // fix index out of range in event dispatch
    // see https://code.google.com/p/android/issues/detail?id=60464
    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if (ev.getPointerCount() > 1 && isDisallowIntercept) {
            requestDisallowInterceptTouchEvent(false);
            boolean handled = super.dispatchTouchEvent(ev);
            requestDisallowInterceptTouchEvent(true);
            return handled;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }
}
