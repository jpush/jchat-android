package io.jchat.android.controller;


import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.jchat.android.R;
import io.jchat.android.activity.AudioFragment;
import io.jchat.android.activity.DocumentFragment;
import io.jchat.android.activity.ImageFragment;
import io.jchat.android.activity.OtherFragment;
import io.jchat.android.activity.SendFileActivity;
import io.jchat.android.activity.VideoFragment;
import io.jchat.android.adapter.ViewPagerAdapter;
import io.jchat.android.view.SendFileView;

public class SendFileController implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private DocumentFragment mDocumentFragment;
    private VideoFragment mVideoFragment;
    private ImageFragment mImgFragment;
    private AudioFragment mAudioFragment;
    private OtherFragment mOtherFragment;
    private SendFileActivity mContext;
    private SendFileView mSFView;


    public SendFileController(SendFileActivity context, SendFileView view) {
        this.mContext = context;
        this.mSFView = view;
        List<Fragment> fragments = new ArrayList<Fragment>();
        // init Fragment
        mDocumentFragment = new DocumentFragment();
        mVideoFragment = new VideoFragment();
        mImgFragment = new ImageFragment();
        mAudioFragment = new AudioFragment();
        mOtherFragment = new OtherFragment();
        fragments.add(mDocumentFragment);
        fragments.add(mVideoFragment);
        fragments.add(mImgFragment);
        fragments.add(mAudioFragment);
        fragments.add(mOtherFragment);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(mContext.getSupportFragmentManger(),
                fragments);
        mSFView.setViewPagerAdapter(viewPagerAdapter);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        mSFView.setCurrentItem(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.actionbar_file_btn:
                mSFView.setCurrentItem(0);
                break;
            case R.id.actionbar_video_btn:
                mSFView.setCurrentItem(1);
                break;
            case R.id.actionbar_album_btn:
                mSFView.setCurrentItem(2);
                break;
            case R.id.actionbar_audio_btn:
                mSFView.setCurrentItem(3);
                break;
            case R.id.actionbar_other_btn:
                mSFView.setCurrentItem(4);
                break;
            case R.id.return_btn:
                mContext.finish();
                break;
            case R.id.jmui_commit_btn:
                break;
        }
    }

}

