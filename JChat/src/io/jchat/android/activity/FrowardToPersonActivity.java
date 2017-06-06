package io.jchat.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import io.jchat.android.R;
import io.jchat.android.adapter.FrowardToPersonAdapter;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.database.FriendEntry;
import io.jchat.android.database.UserEntry;
import io.jchat.android.entity.Event;
import io.jchat.android.entity.EventType;

/**
 * Created by ${chenyn} on 2017/6/3.
 */

public class FrowardToPersonActivity extends BaseActivity {

    private ListView mFrowardListView;
    private Message mMessage;
    private List<FriendEntry> mFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_froward_person);

        mFrowardListView = (ListView) findViewById(R.id.froward_list_view);

        TextView title = (TextView) findViewById(R.id.jmui_title_tv);
        ImageButton returnBtn = (ImageButton) findViewById(R.id.return_btn);
        Button rightBtn = (Button) findViewById(R.id.jmui_commit_btn);

        title.setText("选择转发的好友");
        rightBtn.setVisibility(View.GONE);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        if (mMessage != null) {
            UserEntry userEntry = JChatDemoApplication.getUserEntry();
            mFriend = userEntry.getFriends();
            FrowardToPersonAdapter adapter = new FrowardToPersonAdapter(this, mFriend);
            mFrowardListView.setAdapter(adapter);
        }

        mFrowardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                FriendEntry friendEntry = mFriend.get(position);
                Conversation singleConversation = JMessageClient.getSingleConversation(friendEntry.username);
                if (singleConversation == null) {
                    singleConversation = Conversation.createSingleConversation(friendEntry.username);
                    EventBus.getDefault().post(new Event.Builder()
                            .setType(EventType.createConversation)
                            .setConversation(singleConversation)
                            .build());
                }
                TextContent content = (TextContent) mMessage.getContent();
                String text = content.getText();
                if (getIntent().getStringExtra("frowardMsg") != null && getIntent().getStringExtra("frowardMsg").equals(friendEntry.username)) {
                    Intent intent = new Intent();
                    intent.putExtra("messageBack", text);
                    setResult(428, intent);
                }else {
                    Message singleTextMessage = JMessageClient.createSingleTextMessage(friendEntry.username, text);
                    JMessageClient.sendMessage(singleTextMessage);
                }
                finish();
            }
        });
    }

    @Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.POSTING, sticky = true)
    public void getMessageForFroward(Message message) {
        mMessage = message;
    }

}
