package jiguang.chat.activity.historyfile.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import jiguang.chat.R;
import jiguang.chat.activity.fragment.BaseFragment;
import jiguang.chat.activity.historyfile.adapter.OtherFileAdapter;
import jiguang.chat.activity.historyfile.controller.HistoryFileController;
import jiguang.chat.entity.FileItem;
import jiguang.chat.utils.FileUtils;
import jiguang.chat.view.listview.StickyListHeadersListView;

/**
 * Created by ${chenyn} on 2017/8/23.
 */

public class OtherFileFragment extends BaseFragment {
    private HistoryFileController mController;
    private String mUserName;
    private long mGroupId;
    private Activity mContext;
    private View mRootView;
    private final MyHandler myHandler = new MyHandler(this);
    private final static int SCAN_OK = 1;
    private final static int SCAN_ERROR = 0;
    private OtherFileAdapter mAdapter;
    private List<FileItem> mDocuments = new ArrayList<>();
    private StickyListHeadersListView mDocumentList;
    private Boolean mIsGroup;
    private static int section = 1;
    private Map<String, Integer> sectionMap = new HashMap<String, Integer>();

    public void setController(HistoryFileController controller, String userName, long groupId, boolean isGroup) {
        mController = controller;
        mUserName = userName;
        mGroupId = groupId;
        mIsGroup = isGroup;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.document_file,
                (ViewGroup) mContext.findViewById(R.id.main_view), false);
        mDocumentList = (StickyListHeadersListView) mRootView.findViewById(R.id.document_list);
        initData();
    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Conversation conversation;
                if (mIsGroup) {
                    conversation = JMessageClient.getGroupConversation(mGroupId);
                } else {
                    conversation = JMessageClient.getSingleConversation(mUserName);
                }
                List<Message> allMessage = conversation.getAllMessage();
                for (Message msg : allMessage) {
                    MessageContent content = msg.getContent();

                    if (content.getContentType() == ContentType.file) {
                        String fileType = content.getStringExtra("fileType");
                        if (fileType != null &&
                                !fileType.equals("mp4") && !fileType.equals("mov") && !fileType.equals("rm") &&
                                !fileType.equals("rmvb") && !fileType.equals("wmv") && !fileType.equals("avi") &&
                                !fileType.equals("3gp") && !fileType.equals("mkv") && !fileType.equals("wav") &&
                                !fileType.equals("mp3") && !fileType.equals("wma") && !fileType.equals("midi") &&
                                !fileType.equals("ppt") && !fileType.equals("pptx") && !fileType.equals("doc") &&
                                !fileType.equals("docx") && !fileType.equals("pdf") && !fileType.equals("xls") &&
                                !fileType.equals("xlsx") && !fileType.equals("txt") && !fileType.equals("wps")) {

                            FileContent fileContent = (FileContent) content;
                            String localPath = fileContent.getLocalPath();

                            if (!TextUtils.isEmpty(localPath)) {
                                File imageFile = new File(localPath);
                                if (imageFile.exists()) {
                                    long createTime = msg.getCreateTime();
                                    long fileSize = imageFile.length();
                                    Date date = new Date(createTime);
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月");
                                    String time = format.format(date);
                                    String size = FileUtils.getFileSize(fileSize);
                                    FileItem item = new FileItem(localPath, imageFile.getName(), size, time, msg.getId(), msg.getFromName());
                                    if (!sectionMap.containsKey(item.getDate())) {
                                        item.setSection(section);
                                        sectionMap.put(item.getDate(), section);
                                        section++;
                                    }else {
                                        item.setSection(sectionMap.get(item.getDate()));
                                    }
                                    mDocuments.add(item);
                                }
                            }
                        }

                    }
                    myHandler.sendEmptyMessage(SCAN_OK);
                }
            }
        }).start();
    }


    private static class MyHandler extends Handler {
        private final WeakReference<OtherFileFragment> mFragment;

        public MyHandler(OtherFileFragment fragment) {
            mFragment = new WeakReference<OtherFileFragment>(fragment);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            OtherFileFragment fragment = mFragment.get();
            if (fragment != null) {
                switch (msg.what) {
                    case SCAN_OK:
                        //关闭进度条
                        fragment.mAdapter = new OtherFileAdapter(fragment, fragment.mDocuments);
                        fragment.mDocumentList.setAdapter(fragment.mAdapter);
                        fragment.mAdapter.setUpdateListener(fragment.mController);
                        break;
                    case SCAN_ERROR:
                        Toast.makeText(fragment.getActivity(), fragment.getString(R.string.sdcard_not_prepare_toast),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup p = (ViewGroup) mRootView.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        return mRootView;
    }

    public void notifyOther() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    public void notifyListOther() {
        mDocuments.clear();
        initData();
        mAdapter.notifyDataSetChanged();
    }
}
