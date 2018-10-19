package com.lwons.ecphoto.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

/**
 * Created by liuwons on 2018/10/19
 */
public class FloatAppendButton extends FloatingActionButton {

    private static final float ALPHA_ACTIVE = 1f;
    private static final float ALPHA_INACTIVE = 0.5f;

    private boolean mActive = false;
    private ValueAnimator mAnimator;

    public FloatAppendButton(Context context) {
        super(context);
        init();
    }

    public FloatAppendButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatAppendButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mAnimator = ValueAnimator.ofFloat(ALPHA_INACTIVE, ALPHA_ACTIVE);
        mAnimator.setDuration(500);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float cur = (float) valueAnimator.getAnimatedValue();
                setAlpha(cur);
            }
        });
    }

    public void activate() {
        if (mActive) {
            return;
        }
        mActive = !mActive;
        mAnimator.end();
        mAnimator.start();
    }

    public void inactivate() {
        if (!mActive) {
            return;
        }
        mActive = !mActive;
        mAnimator.end();
        setAlpha(ALPHA_INACTIVE);
    }
}
