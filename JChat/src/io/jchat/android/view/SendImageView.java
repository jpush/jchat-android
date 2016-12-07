package io.jchat.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;

import io.jchat.android.R;
import io.jchat.android.adapter.AlbumListAdapter;


public class SendImageView extends LinearLayout {

    private ListView mAlbumLV;

    public SendImageView(Context context) {
        super(context);
    }

    public SendImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initModule() {
        mAlbumLV = (ListView) findViewById(R.id.album_list_view);
    }

    public void setAdapter(AlbumListAdapter adapter) {
        mAlbumLV.setAdapter(adapter);
    }


}
