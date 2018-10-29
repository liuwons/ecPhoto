package com.lwons.ecphoto.ui.activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.imnjh.imagepicker.widget.subsamplingview.ImageSource;
import com.imnjh.imagepicker.widget.subsamplingview.OnImageEventListener;
import com.lwons.ecphoto.R;
import com.lwons.ecphoto.ui.PhotoBrowseViewPager;
import com.lwons.ecphoto.ui.widget.PhotoBrowsePageView;

import java.io.File;
import java.util.List;

public class PhotoBrowseActivity extends AppCompatActivity {
    public static final String KEY_PHOTO_URIS = "photo_uris";
    public static final String KEY_INIT_POS = "init_pos";

    private PhotoBrowseViewPager mViewPager;
    private Toolbar mToolbar;
    private ImageView mBackIcon;
    private PhotoViewAdapter mPhotoViewAdapter;

    private ImageView mPlaceHolderImage;

    private View.OnClickListener mPhotoTapListener;

    private List<String> mPhotoUris;
    private int mInitPos;
    private boolean mFirstImageLoaded;
    private boolean mEntering = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPhotoUris = getIntent().getStringArrayListExtra(KEY_PHOTO_URIS);
        mInitPos = getIntent().getIntExtra(KEY_INIT_POS, 0);

        initUI();
    }

    private void initUI() {
        setContentView(R.layout.activity_photo_browse);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        mPhotoTapListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleState();
            }
        };

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mPlaceHolderImage = findViewById(R.id.img_place_holder);
        mViewPager = findViewById(R.id.viewpager);
        mBackIcon = findViewById(R.id.nav_icon);
        mBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mPhotoViewAdapter = new PhotoViewAdapter();
        mViewPager.setAdapter(mPhotoViewAdapter);
        mViewPager.addOnPageChangeListener(null);
        mViewPager.setCurrentItem(mInitPos);
    }


    private void toggleState() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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
            File file = new File(curUriInfo.getPath());
            pageView.setOriginImage(ImageSource.uri(file.getAbsolutePath()));
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
