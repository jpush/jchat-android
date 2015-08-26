package io.jchat.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import io.jchat.android.R;


public class DropDownListView extends ListView implements OnScrollListener {

    private boolean isDropDownStyle = true;
    private boolean isOnBottomStyle = true;
    private Context context;

    /**
     * header layout view
     **/
    private RelativeLayout headerLayout;
//    private ImageView headerImage;
    private ProgressBar headerProgressBar;

    private OnDropDownListener onDropDownListener;
    private OnScrollListener onScrollListener;

    /**
     * rate about drop down distance and header padding top when drop down
     **/
    private float headerPaddingTopRate = 1.5f;
    /**
     * min distance which header can release to loading
     **/
    private int headerReleaseMinDistance;
    private int currentScrollState;
    private int currentHeaderStatus;

    /**
     * whether reached top, when has reached top, don't show header layout
     **/
    private boolean hasReachedTop = false;

    /**
     * image flip animation
     **/
    private RotateAnimation flipAnimation;
    /**
     * image reverse flip animation
     **/
    private RotateAnimation reverseFlipAnimation;

    /**
     * header layout original height
     **/
    private int headerOriginalHeight;
    /**
     * header layout original padding top
     **/
    private int headerOriginalTopPadding;
    /**
     * y of point which user touch down
     **/
    private float actionDownPointY;

    private float actionMovePointY;

    private int mListViewFirstItem = 0;
    //listView中第一项的在屏幕中的位置
    private int mScreenY = 0;
    //是否向上滚动
    private boolean mIsScrollToUp = false;

    public DropDownListView(Context context) {
        super(context);
        init(context);
    }

    public DropDownListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs);
        init(context);
    }

    public DropDownListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttrs(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        initDropDownStyle();

        // should set, to run onScroll method and so on
        super.setOnScrollListener(this);
    }

    /**
     * init drop down style, only init once
     */
    private void initDropDownStyle() {
        if (headerLayout != null) {
            if (isDropDownStyle) {
                addHeaderView(headerLayout);
            } else {
                removeHeaderView(headerLayout);
            }
            return;
        }
        if (!isDropDownStyle) {
            return;
        }

        headerReleaseMinDistance = context.getResources().getDimensionPixelSize(
                R.dimen.drop_down_list_header_release_min_distance);
        flipAnimation = new RotateAnimation(0, 180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        flipAnimation.setInterpolator(new LinearInterpolator());
        flipAnimation.setDuration(250);
        flipAnimation.setFillAfter(true);
        reverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseFlipAnimation.setInterpolator(new LinearInterpolator());
        reverseFlipAnimation.setDuration(250);
        reverseFlipAnimation.setFillAfter(true);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        headerLayout = (RelativeLayout) inflater.inflate(R.layout.drop_down_list_header, this, false);
//        headerImage = (ImageView) headerLayout.findViewById(R.id.drop_down_list_header_image);
        headerProgressBar = (ProgressBar) headerLayout.findViewById(R.id.drop_down_list_header_progress_bar);
        addHeaderView(headerLayout);

        measureHeaderLayout(headerLayout);
        headerOriginalHeight = headerLayout.getMeasuredHeight();
        headerOriginalTopPadding = headerLayout.getPaddingTop();
        currentHeaderStatus = HEADER_STATUS_CLICK_TO_LOAD;
    }

    /**
     * @return isDropDownStyle
     */
    public boolean isDropDownStyle() {
        return isDropDownStyle;
    }

    /**
     * @param isDropDownStyle
     */
    public void setDropDownStyle(boolean isDropDownStyle) {
        if (this.isDropDownStyle != isDropDownStyle) {
            this.isDropDownStyle = isDropDownStyle;
            initDropDownStyle();
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        if (isDropDownStyle) {
            setSecondPositionVisible();
        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        onScrollListener = listener;
    }

    /**
     * @param onDropDownListener
     */
    public void setOnDropDownListener(OnDropDownListener onDropDownListener) {
        this.onDropDownListener = onDropDownListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDropDownStyle) {
            return super.onTouchEvent(event);
        }

        hasReachedTop = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDownPointY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                actionMovePointY = event.getY();
//                adjustHeaderPadding(event);
                break;
            case MotionEvent.ACTION_UP:
                if (!isVerticalScrollBarEnabled()) {
                    setVerticalScrollBarEnabled(true);
                }
                /**
                 * set status when finger leave screen if first item visible and header status is not
                 * HEADER_STATUS_LOADING
                 * <ul>
                 * <li>if current header status is HEADER_STATUS_RELEASE_TO_LOAD, call onDropDown.</li>
                 * <li>if current header status is HEADER_STATUS_DROP_DOWN_TO_LOAD, then set header status to
                 * HEADER_STATUS_CLICK_TO_LOAD and hide header layout.</li>
                 * </ul>
                 */
                if (getFirstVisiblePosition() == 0 && currentHeaderStatus != HEADER_STATUS_LOADING) {
                    switch (currentHeaderStatus) {
                        case HEADER_STATUS_RELEASE_TO_LOAD:
                            onDropDown();
                            break;
                        case HEADER_STATUS_DROP_DOWN_TO_LOAD:
//                            setHeaderStatusClickToLoad();
                            setSecondPositionVisible();
                            break;
                        case HEADER_STATUS_CLICK_TO_LOAD:
                        default:
                            break;
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (isDropDownStyle) {
            if (currentScrollState == SCROLL_STATE_TOUCH_SCROLL && currentHeaderStatus != HEADER_STATUS_LOADING) {
                if (firstVisibleItem == 0 && actionMovePointY - actionDownPointY > 0) {
                    onDropDown();
                }

//                if (firstVisibleItem == 0) {
//                    headerImage.setVisibility(View.VISIBLE);
//                    int pointBottom = headerOriginalHeight + headerReleaseMinDistance;
//                    if (headerLayout.getBottom() >= pointBottom) {
//                        setHeaderStatusReleaseToLoad();
//                    } else if (headerLayout.getBottom() < pointBottom) {
//                        setHeaderStatusDropDownToLoad();
//                    }
//                } else {
//                    setHeaderStatusClickToLoad();
//                }
            } else if (currentScrollState == SCROLL_STATE_FLING && firstVisibleItem == 0
                    && currentHeaderStatus != HEADER_STATUS_LOADING) {
                /**
                 * when state of ListView is SCROLL_STATE_FLING(ListView is scrolling but finger has leave screen) and
                 * first item(header layout) is visible and header status is not HEADER_STATUS_LOADING, then hide first
                 * item, set second item visible and set hasReachedTop true.
                 */
                setSecondPositionVisible();
                hasReachedTop = true;
            } else if (currentScrollState == SCROLL_STATE_FLING && hasReachedTop) {
                /**
                 * when state of ListView is SCROLL_STATE_FLING(ListView is scrolling but finger has leave screen) and
                 * hasReachedTop is true(it's because flick back), then hide first item, set second item visible.
                 */
                setSecondPositionVisible();
            }
        }

        if (onScrollListener != null) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isDropDownStyle) {
            currentScrollState = scrollState;

            if (currentScrollState == SCROLL_STATE_IDLE) {
                hasReachedTop = false;
            }
        }

        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * drop down begin, adjust view status
     */
    private void onDropDownBegin() {
        if (isDropDownStyle) {
            setHeaderStatusLoading();
        }
    }

    /**
     * on drop down loading, you can call it by manual, but you should manual call onBottomComplete at the same time.
     */
    public void onDropDown() {
        if (currentHeaderStatus != HEADER_STATUS_LOADING && isDropDownStyle && onDropDownListener != null) {
            onDropDownBegin();
            onDropDownListener.onDropDown();
        }
    }

    /**
     * drop down complete, restore view status
     *
     * @param secondText display below header text, if null, not display
     */
    public void onDropDownComplete(CharSequence secondText) {
        if (isDropDownStyle) {
            onDropDownComplete();
        }
    }


    /**
     * drop down complete, restore view status
     */
    public void onDropDownComplete() {
        if (isDropDownStyle) {
            resetHeader();

            if (headerLayout.getBottom() > 0) {
                invalidateViews();
//                setSecondPositionVisible();
            }
        }
    }

    private void resetHeader() {
        if (currentHeaderStatus != HEADER_STATUS_CLICK_TO_LOAD) {
            resetHeaderPadding();
//            headerImage.clearAnimation();
//            headerImage.setVisibility(View.GONE);
            headerProgressBar.setVisibility(View.GONE);
            currentHeaderStatus = HEADER_STATUS_DROP_DOWN_TO_LOAD;
        }
    }

    /**
     * OnDropDownListener, called when header released
     */
    public interface OnDropDownListener {

        /**
         * called when header released
         */
        public void onDropDown();
    }

    /**
     * set second position visible(index is 1), because first position is header layout
     */
    public void setSecondPositionVisible() {
        if (getAdapter() != null && getAdapter().getCount() > 0 && getFirstVisiblePosition() == 0) {
            setSelection(1);
        }
    }


    /**
     * get header layout view
     *
     * @return
     */
    public RelativeLayout getHeaderLayout() {
        return headerLayout;
    }


    /**
     * get rate about drop down distance and header padding top when drop down
     *
     * @return headerPaddingTopRate
     */
    public float getHeaderPaddingTopRate() {
        return headerPaddingTopRate;
    }

    /**
     * set rate about drop down distance and header padding top when drop down
     *
     * @param headerPaddingTopRate
     */
    public void setHeaderPaddingTopRate(float headerPaddingTopRate) {
        this.headerPaddingTopRate = headerPaddingTopRate;
    }

    /**
     * get min distance which header can release to loading
     *
     * @return headerReleaseMinDistance
     */
    public int getHeaderReleaseMinDistance() {
        return headerReleaseMinDistance;
    }

    /**
     * status which you can click to load, init satus
     **/
    public static final int HEADER_STATUS_CLICK_TO_LOAD = 1;
    /**
     * status which you can drop down and then release to excute onDropDownListener, when height of header layout lower
     * than a value
     **/
    public static final int HEADER_STATUS_DROP_DOWN_TO_LOAD = 2;
    /**
     * status which you can release to excute onDropDownListener, when height of header layout higher than a value
     **/
    public static final int HEADER_STATUS_RELEASE_TO_LOAD = 3;
    /**
     * status which is loading
     **/
    public static final int HEADER_STATUS_LOADING = 4;


    /**
     * set header status to {@link #HEADER_STATUS_DROP_DOWN_TO_LOAD}
     */
    private void setHeaderStatusDropDownToLoad() {
        if (currentHeaderStatus != HEADER_STATUS_DROP_DOWN_TO_LOAD) {
//            headerImage.setVisibility(View.VISIBLE);
//            if (currentHeaderStatus != HEADER_STATUS_CLICK_TO_LOAD) {
//                headerImage.clearAnimation();
//                headerImage.startAnimation(reverseFlipAnimation);
//            }
            headerProgressBar.setVisibility(View.GONE);

            if (isVerticalFadingEdgeEnabled()) {
                setVerticalScrollBarEnabled(false);
            }

            currentHeaderStatus = HEADER_STATUS_DROP_DOWN_TO_LOAD;
        }
    }

    /**
     * set header status to {@link #HEADER_STATUS_RELEASE_TO_LOAD}
     */
    private void setHeaderStatusReleaseToLoad() {
        if (currentHeaderStatus != HEADER_STATUS_RELEASE_TO_LOAD) {
//            headerImage.setVisibility(View.VISIBLE);
//            headerImage.clearAnimation();
//            headerImage.startAnimation(flipAnimation);
            headerProgressBar.setVisibility(View.GONE);

            currentHeaderStatus = HEADER_STATUS_RELEASE_TO_LOAD;
        }
    }

    /**
     * set header status to {@link #HEADER_STATUS_LOADING}
     */
    private void setHeaderStatusLoading() {
        if (currentHeaderStatus != HEADER_STATUS_LOADING) {
            resetHeaderPadding();

//            headerImage.setVisibility(View.GONE);
//            headerImage.clearAnimation();
            headerProgressBar.setVisibility(View.VISIBLE);
            currentHeaderStatus = HEADER_STATUS_LOADING;
            setSelection(0);
        }
    }

    /**
     * adjust header padding according to motion event
     *
     * @param ev
     */
    private void adjustHeaderPadding(MotionEvent ev) {
        // adjust header padding according to motion event history
        int pointerCount = ev.getHistorySize();
        if (isVerticalFadingEdgeEnabled()) {
            setVerticalScrollBarEnabled(false);
        }
        for (int i = 0; i < pointerCount; i++) {
            if (currentHeaderStatus == HEADER_STATUS_DROP_DOWN_TO_LOAD
                    || currentHeaderStatus == HEADER_STATUS_RELEASE_TO_LOAD) {
                headerLayout
                        .setPadding(
                                headerLayout.getPaddingLeft(),
                                (int) (((ev.getHistoricalY(i) - actionDownPointY) - headerOriginalHeight) / headerPaddingTopRate),
                                headerLayout.getPaddingRight(), headerLayout.getPaddingBottom());
            }
        }
    }

    /**
     * reset header padding
     */
    private void resetHeaderPadding() {
        headerLayout.setPadding(headerLayout.getPaddingLeft(), headerOriginalTopPadding,
                headerLayout.getPaddingRight(), headerLayout.getPaddingBottom());
    }

    /**
     * measure header layout
     *
     * @param child
     */
    private void measureHeaderLayout(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * get attrs
     *
     * @param context
     * @param attrs
     */
    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.drop_down_list_attr);
        isDropDownStyle = ta.getBoolean(R.styleable.drop_down_list_attr_isDropDownStyle, false);
        isOnBottomStyle = ta.getBoolean(R.styleable.drop_down_list_attr_isOnBottomStyle, false);
        ta.recycle();
    }
}
