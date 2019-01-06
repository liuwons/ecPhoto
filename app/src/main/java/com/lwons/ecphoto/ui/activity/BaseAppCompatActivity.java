package com.lwons.ecphoto.ui.activity;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.lwons.ecphoto.R;

/**
 * Created by liuwons on 19-1-6.
 */
public class BaseAppCompatActivity extends AppCompatActivity {
    private ViewGroup mActionBar;
    private View mActionBack;
    private TextView mActionTitle;
    private LinearLayout mContentHolder;

    private volatile boolean mFullScreen = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        super.setContentView(R.layout.base_activity);
        mActionBar = findViewById(R.id.action_bar);
        mActionBack = findViewById(R.id.action_back);
        mActionBack.setOnClickListener(v -> onBackPressed());
        mActionTitle = findViewById(R.id.action_title);
        mContentHolder = findViewById(R.id.content_holder);
        setActionBarTopMargin(BarUtils.getStatusBarHeight());

        onInitStatusBar();
    }

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater.from(this).inflate(layoutResID, mContentHolder);
    }

    protected void setTitle(String title) {
        mActionTitle.setText(title);
    }

    protected void setActionBarVisibility(int visibility) {
        mActionBar.setVisibility(visibility);
    }

    protected void hideActionBar() {
        mActionBar.setVisibility(View.GONE);
    }

    protected void showActionBar() {
        mActionBar.setVisibility(View.VISIBLE);
    }

    protected void onInitStatusBar() {
        setStatusBar(false, Color.WHITE);
    }

    public synchronized boolean isFullScreen() {
        return mFullScreen;
    }

    public synchronized void requestFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mFullScreen = true;
    }

    public synchronized void cancelFullScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mFullScreen = false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void setStatusBar(boolean setStatusBarColor, @ColorInt int statusBarColor) {
        View decorView = getWindow().getDecorView();
        int flag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (decorView.getSystemUiVisibility() != flag) {
            decorView.setSystemUiVisibility(flag);
        }
        if (!setStatusBarColor) {
            statusBarColor = Color.WHITE;
        }
        getWindow().setStatusBarColor(statusBarColor);
    }

    private void setActionBarTopMargin(int topMargin) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mActionBar.getLayoutParams();
        params.setMargins(params.leftMargin, topMargin, params.rightMargin, params.bottomMargin);
        mActionBar.setLayoutParams(params);
    }
}
