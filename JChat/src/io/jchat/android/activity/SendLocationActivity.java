package io.jchat.android.activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.UUID;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.LocationContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.BitmapLoader;

public class SendLocationActivity extends BaseActivity {

    private MapView mMapView;
    private ImageButton mReturnBtn;
    private ImageButton mLocateBtn;
    private TextView mTitle;
    private Button mSendBtn;
    private BaiduMap mMap;
    public LocationClient mLocationClient = null;
    private MyLocationListener mListener = new MyLocationListener();
    private boolean mIsFirstLoc = true;
    private boolean mShowInfo = false;
    private double mLatitude;
    private double mLongitude;
    private String mDescribe;
    private View mPopupView;
    private InfoWindow mInfoWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        final Context context = this;
        setContentView(R.layout.activity_send_location);
        mMapView = (MapView) findViewById(R.id.map_view);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitle = (TextView) findViewById(R.id.jmui_title_tv);
        mSendBtn = (Button) findViewById(R.id.jmui_commit_btn);
        mLocateBtn = (ImageButton) findViewById(R.id.locate_btn);
        mPopupView = LayoutInflater.from(this).inflate(R.layout.location_popup_layout, null);
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mPopupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.hideInfoWindow();
                mShowInfo = false;
            }
        });

        Intent intent = getIntent();
        boolean sendLocation = intent.getBooleanExtra("sendLocation", false);
        mMap = mMapView.getMap();
        mMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(18).build()));
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(mListener);
        initLocation();
        mLocationClient.start();
        if (sendLocation) {
            mTitle.setText(this.getString(R.string.send_location));
            mSendBtn.setText(this.getString(R.string.jmui_send));
        } else {
            mTitle.setVisibility(View.GONE);
            mSendBtn.setVisibility(View.GONE);
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            LatLng ll = new LatLng(latitude, longitude);
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.oval);
            OverlayOptions options = new MarkerOptions().position(ll).icon(descriptor).zIndex(10);
            mMap.addOverlay(options);
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mMap.setMapStatus(update);

            TextView location = (TextView) mPopupView.findViewById(R.id.location_tips);
            location.setText(intent.getStringExtra("locDesc"));
        }


        mMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
                mMap.animateMapStatus(update);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        mLocateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng ll = new LatLng(mLatitude, mLongitude);
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                mMap.animateMapStatus(update);
            }
        });

        mMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (mShowInfo) {
                    mMap.hideInfoWindow();
                    mShowInfo = false;
                } else {
                    if (null == mInfoWindow) {
                        LatLng ll = marker.getPosition();
                        Point p = mMap.getProjection().toScreenLocation(ll);
                        p.y -= 47;
                        p.x -= 20;
                        LatLng llInfo = mMap.getProjection().fromScreenLocation(p);
                        mInfoWindow = new InfoWindow(mPopupView, llInfo, 0);
                    }
                    mMap.showInfoWindow(mInfoWindow);
                    mShowInfo = true;
                }
                return true;
            }
        });


        String targetId = getIntent().getStringExtra(JChatDemoApplication.TARGET_ID);
        String targetAppKey = getIntent().getStringExtra(JChatDemoApplication.TARGET_APP_KEY);
        long groupId = getIntent().getLongExtra(JChatDemoApplication.GROUP_ID, 0);
        final Conversation conv;
        if (groupId != 0) {
            conv = JMessageClient.getGroupConversation(groupId);
        } else {
            conv = JMessageClient.getSingleConversation(targetId, targetAppKey);
        }
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int left = mWidth / 4;
                int top = (int) (mHeight - 1.1 * mWidth);
                Rect rect = new Rect(left, top, mWidth - left, mHeight - (int) (1.2 * top));
                mMap.snapshotScope(rect, new BaiduMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
                        if (null != bitmap && null != conv) {
                            LocationContent locationContent = new LocationContent(mLatitude,
                                    mLongitude, mMapView.getMapLevel(), mDescribe);
                            String fileName = UUID.randomUUID().toString();
                            String path = BitmapLoader.saveBitmapToLocal(bitmap, fileName);
                            locationContent.setStringExtra("path", path);
                            Intent intent = new Intent();
                            Message msg = conv.createSendMessage(locationContent);
                            intent.putExtra(JChatDemoApplication.MsgIDs, msg.getId());
                            if (conv.getType() == ConversationType.single) {
                                UserInfo userInfo = (UserInfo) conv.getTargetInfo();
                                if (userInfo.isFriend()) {
                                    JMessageClient.sendMessage(msg);
                                } else {
                                    CustomContent customContent = new CustomContent();
                                    customContent.setBooleanValue("notFriend", true);
                                    Message customMsg = conv.createSendMessage(customContent);
                                    intent.putExtra("customMsg", customMsg.getId());
                                }
                            } else {
                                JMessageClient.sendMessage(msg);
                            }

                            setResult(JChatDemoApplication.RESULT_CODE_SEND_LOCATION, intent);
                            finish();
                        } else {
                            Toast.makeText(context, context.getString(R.string.send_location_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.SetIgnoreCacheException(false);
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null == location || mMapView == null) {
                return;
            }
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            mDescribe = location.getLocationDescribe();
            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100)
                    .latitude(mLatitude)
                    .longitude(mLongitude)
                    .build();
            mMap.setMyLocationData(data);
            if (mIsFirstLoc) {
                mIsFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                mMap.animateMapStatus(update);
                Log.w("SendLocationActivity", location.getLocationDescribe());

            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }


}
