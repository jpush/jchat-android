package io.jchat.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.LinearLayout;

import io.jchat.android.R;
import io.jchat.android.adapter.ImageAdapter;


public class SendImageView extends LinearLayout {

    private GridView mImageGV;

    public SendImageView(Context context) {
        super(context);
    }

    public SendImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initModule() {
        mImageGV = (GridView) findViewById(R.id.album_grid_view);
    }

    public void setAdapter(ImageAdapter adapter) {
        mImageGV.setAdapter(adapter);
    }


}
