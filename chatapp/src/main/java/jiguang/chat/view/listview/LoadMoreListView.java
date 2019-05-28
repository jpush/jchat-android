package jiguang.chat.view.listview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import jiguang.chat.R;

public class LoadMoreListView extends ListView implements AbsListView.OnScrollListener {

    //是否加载中或已加载所有数据
    private boolean mIsLoadingOrComplete;
    //是否所有条目都可见
    private boolean mIsAllVisible;

    private OnLoadMoreListener mOnLoadMoreListener;
    private RelativeLayout loadLayout;
    private View mLoadCompleteView;

    public LoadMoreListView(Context context) {
        super(context);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    //加载更多回调接口
    public interface OnLoadMoreListener {
        void loadMore();
    }

    //初始化
    private void init(Context context) {
        loadLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.jmui_drop_down_list_header, null);
        mLoadCompleteView = LayoutInflater.from(context).inflate(R.layout.load_complete, null);
//        mLoadCompleteView.setOnClickListener();
        setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //(最后一条可见item==最后一条item)&&(停止滑动)&&(!加载数据中)&&(!所有条目都可见)
        if (view.getLastVisiblePosition() == getAdapter().getCount() - 1 && scrollState == SCROLL_STATE_IDLE && !mIsLoadingOrComplete && !mIsAllVisible) {
            if (null != mOnLoadMoreListener) {
                //加载更多
                mIsLoadingOrComplete = true;
                mOnLoadMoreListener.loadMore();
            }
        }
        if (getFooterViewsCount() == 0 && !mIsAllVisible) {
            loadLayout.findViewById(R.id.loading_view).setVisibility(VISIBLE);
            ImageView loading = loadLayout.findViewById(R.id.jmui_loading_img);
            AnimationDrawable drawable = (AnimationDrawable) loading.getDrawable();
            drawable.start();
            addFooterView(loadLayout);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mIsAllVisible = totalItemCount == visibleItemCount;
        if (mIsAllVisible && !mIsLoadingOrComplete && totalItemCount > ((ListView) view).getFooterViewsCount() + ((ListView) view).getHeaderViewsCount()) {
            if (null != mOnLoadMoreListener) {
                mIsLoadingOrComplete = true;
                mOnLoadMoreListener.loadMore();
            }
        }
    }

    /**
     * 加载更多回调
     *
     * @param onLoadMoreListener 加载更多回调接口
     */
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    /**
     * 通知此次加载完成,remove footerView
     *
     * @param allComplete 是否已加载全部数据
     */
    public void setLoadCompleted(final boolean allComplete) {
        if (allComplete && getFooterViewsCount() != 0) {
            removeFooterView(loadLayout);
            removeFooterView(mLoadCompleteView);
            addFooterView(mLoadCompleteView);
        } else {
            mIsLoadingOrComplete = false;
        }
    }

    public void updateData() {
        mIsLoadingOrComplete = false;
        removeFooterView(loadLayout);
        removeFooterView(mLoadCompleteView);
    }
}

