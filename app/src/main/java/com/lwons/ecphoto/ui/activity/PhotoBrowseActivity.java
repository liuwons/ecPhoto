package com.lwons.ecphoto.ui.activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.BarUtils;
import com.imnjh.imagepicker.widget.subsamplingview.OnImageEventListener;
import com.lwons.ecphoto.R;
import com.lwons.ecphoto.ui.PhotoBrowseViewPager;
import com.lwons.ecphoto.ui.widget.PhotoBrowsePageView;

import java.util.List;

public class PhotoBrowseActivity extends BaseAppCompatActivity {
    public static final String KEY_PHOTO_URIS = "photo_uris";
    public static final String KEY_INIT_POS = "init_pos";
    public static final String KEY_INIT_URI = "init_uri";

    private PhotoBrowseViewPager mViewPager;
    private PhotoViewAdapter mPhotoViewAdapter;

    private ViewGroup mToolBar;

    private ImageView mPlaceHolderImage;

    private View.OnClickListener mPhotoTapListener;

    private List<String> mPhotoUris;
    private int mInitPos;
    private String mInitUri;

    private boolean mFirstImageLoaded;
    private boolean mEntering = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPhotoUris = getIntent().getStringArrayListExtra(KEY_PHOTO_URIS);
        mInitPos = getIntent().getIntExtra(KEY_INIT_POS, -1);
        if (mInitPos < 0) {
            mInitUri = getIntent().getStringExtra(KEY_INIT_URI);
            for (int i = 0; i < mPhotoUris.size(); i ++) {
                if (mPhotoUris.get(i).equals(mInitUri)) {
                    mInitPos = i;
                }
            }
        }
        if (mInitPos < 0) {
            mInitPos = 0;
        }

        initUI();
    }

    private void initUI() {
        setContentView(R.layout.activity_photo_browse);
        setActionBarVisibility(View.GONE);

        mToolBar = findViewById(R.id.toolbar);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mToolBar.getLayoutParams();
        params.setMargins(params.leftMargin, BarUtils.getStatusBarHeight(), params.rightMargin, params.bottomMargin);
        mToolBar.setLayoutParams(params);

        mPhotoTapListener = v -> toggleState();

        mPlaceHolderImage = findViewById(R.id.img_place_holder);
        mViewPager = findViewById(R.id.viewpager);

        mPhotoViewAdapter = new PhotoViewAdapter();
        mViewPager.setAdapter(mPhotoViewAdapter);
        mViewPager.addOnPageChangeListener(null);
        mViewPager.setCurrentItem(mInitPos);
    }

    private void showToolBar() {
        mToolBar.setVisibility(View.VISIBLE);
    }

    private void hideToolBar() {
        mToolBar.setVisibility(View.GONE);
    }


    private void toggleState() {
        if (isFullScreen()) {
            cancelFullScreen();
            showToolBar();
        } else {
            requestFullScreen();
            hideToolBar();
        }
    }

    class PhotoViewAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final PhotoBrowsePageView pageView = new PhotoBrowsePageView(container.getContext());
            pageView.setMaxScale(15);
            pageView.setOnClickListener(mPhotoTapListener);
            pageView.getOriginImageView().setOnImageEventListener(new OnImageEventListener() {

                @Override
                public void onImageLoaded(int width, int height) {
                    if (isFinishing()) {
                        return;
                    }
                    if (position == mInitPos) {
                        mFirstImageLoaded = true;
                        if (!mEntering && mPlaceHolderImage.getVisibility() == View.VISIBLE) {
                            mPlaceHolderImage.setVisibility(View.GONE);
                            mViewPager.setScrollEnabled(true);
                        }
                    }
                }
            });
            Uri curUriInfo = Uri.parse(mPhotoUris.get(position));
            pageView.setEncryptedImageUri(curUriInfo);
            pageView.setBackgroundColor(Color.TRANSPARENT);
            container.addView(pageView,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            pageView.setTag(position);
            return pageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mPhotoUris.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

}
