# jchat-android

[![Join the chat at https://gitter.im/jpush/jchat-android](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/jpush/jchat-android?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
JChat android app. A real app for jmessage.

[JChat App](http://jchat.io)

[JChat iOS](https://github.com/jpush/jchat-ios)

[JChat Web](https://github.com/jpush/jchat-web)

####在Android Studio中导入JChat demo

如果你想在Android Studio上运行JChat demo 

1. 下载jchat.zip或者在[这里](https://www.jpush.cn/common/downloads/sdk/im_android/)下载.

2. 解压jchat.zip，在Android Studio中新建一个project或者在你当前的project中选择import module--> jchat（选择jchat文件夹）![如图](https://github.com/KenChoi1992/jchat-android/raw/dev/JChat/screenshots/screenshot2.png)

3. 修改jchat module下的build.gradle文件，将buildToolsVersion改为你Android Studio当前所使用的版本，sync一下。

4. 修改AndroidManifest，将Manifest中“您的包名”、“您的AppKey”全部替换为你在JPush控制台上注册应用的包名和对应的AppKey。（或者将“您的包名”全部替换成${applicationId}，然后在build.gradle文件中defaultConfig集合中声明一个applicationId![如图](https://github.com/KenChoi1992/jchat-android/raw/dev/JChat/screenshots/screenshot3.png)）

5. 全局替换R引用。选择src下io.jchat.android目录，右键点击，选择Replace in Path... 

![如图](https://github.com/KenChoi1992/jchat-android/raw/dev/JChat/screenshots/screenshot4.png)

在弹出的菜单中输入你要替换的R文件引用

![如图](https://github.com/KenChoi1992/jchat-android/raw/dev/JChat/screenshots/screenshot5.png)

然后点击find，完成后在菜单栏中选择Build-->Make Module 'JChat'即可。

#####JChat的工程结构

JChat的架构模型参考了Android Passive MVC架构(但是去掉了Listener模块)，有兴趣的可以参考[这里](http://pan.baidu.com/s/1mhoms4o)。

- Application 主要作用是jmessage－sdk的初始化以及Notification的相关设置

- activity包 JChat的Activity的集合，主要负责绑定Controller和View，以及界面的跳转

- controller包 主要负责事件的点击、数据处理等，是Activity和View的中间层

- view包 主要负责界面的展示、控件的初始化、点击事件的绑定等

- adapter包 主要负责ListView或GridView的数据处理

- tools包 工具类的集合

 
---

####在你的项目中集成jmessage-sdk

* 类库配置

在下载的JChat demo中打开libs文件夹，将libs的so库文件以及jmessage－sdk拷贝到你的项目中，目录结构![如图](https://github.com/KenChoi1992/jchat-android/raw/dev/JChat/screenshots/screenshot1.png)

接下来，修改你项目中的build.gradle文件，在android块中加入sourceSets
> demo build.gradle

```
    sourceSets {
        main {
            manifest.srcFile 'AndroidManifest.xml'
            jniLibs.srcDirs = ['libs']
            java.srcDirs = ['src']
            resources.srcDirs = ['src']
            aidl.srcDirs = ['src']
            renderscript.srcDirs = ['src']
            res.srcDirs = ['res']
            assets.srcDirs = ['assets']
        }

        // Move the tests to tests/java, tests/res, etc...
        instrumentTest.setRoot('tests')

        // Move the build types to build-types/<type>
        // For instance, build-types/debug/java, build-types/debug/AndroidManifest.xml, ...
        // This moves them out of them default location under src/<type>/... which would
        // conflict with src/ being used by the main source set.
        // Adding new build types or product flavors should be accompanied
        // by a similar customization.
        debug.setRoot('build-types/debug')
        release.setRoot('build-types/release')
    }
```
这样可以兼容Android Studio和Eclipse。

* AndroidManifest配置

在demo中将jmessage－sdk以及jpush需求的配置项复制过来（jmessage集成了jpush的功能）

权限声明
> demo AndroidManifest.xml

```

    <permission
        android:name="${applicationId}.permission.JPUSH_MESSAGE"
        android:protectionLevel="signature" />

    <!--Required 一些系统要求的权限，如访问网络等-->
    <uses-permission android:name="${applicationId}.permission.JPUSH_MESSAGE" />
    <uses-permission android:name="android.permission.RECEIVE_USER_PRESENT" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />


    <!-- JMessage Demo required for record audio-->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>

```

application配置项
> demo AndroidManifest.xml

```

        <!-- Required Push SDK核心功能-->
        <service
            android:name="cn.jpush.android.service.PushService"
            android:enabled="true"
            android:exported="false"
            android:process=":remote">
            <intent-filter>
                <action android:name="cn.jpush.android.intent.REGISTER" />
                <action android:name="cn.jpush.android.intent.REPORT" />
                <action android:name="cn.jpush.android.intent.PushService" />
                <action android:name="cn.jpush.android.intent.PUSH_TIME" />
            </intent-filter>
        </service>

        <!-- Required Push SDK核心功能-->
        <receiver
            android:name="cn.jpush.android.service.PushReceiver"
            android:enabled="true"
            android:exported="false">
            <intent-filter android:priority="1000">
                <action android:name="cn.jpush.android.intent.NOTIFICATION_RECEIVED_PROXY" />  <!--Required  显示通知栏 -->
                <category android:name="${applicationId}" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.USER_PRESENT" />
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
            </intent-filter>
            <!-- Optional -->
            <intent-filter>
                <action android:name="android.intent.action.PACKAGE_ADDED" />
                <action android:name="android.intent.action.PACKAGE_REMOVED" />

                <data android:scheme="package" />
            </intent-filter>
        </receiver>

        <!-- Required Push SDK核心功能 -->
        <activity
            android:name="cn.jpush.android.ui.PushActivity"
            android:configChanges="orientation|keyboardHidden"
            android:theme="@android:style/Theme.NoTitleBar"
            android:exported="false">
            <intent-filter>
                <action android:name="cn.jpush.android.ui.PushActivity" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="${applicationId}" />
            </intent-filter>
        </activity>
        <!-- Required Push SDK核心功能 -->
        <service
            android:name="cn.jpush.android.service.DownloadService"
            android:enabled="true"
            android:exported="false" />
        <!-- Required Push SDK核心功能 -->
        <receiver android:name="cn.jpush.android.service.AlarmReceiver" android:exported="false"/>

        <!-- IM Required IM SDK核心功能-->
        <receiver
            android:name="cn.jpush.im.android.helpers.IMReceiver"
            android:enabled="true"
            android:exported="false">
            <intent-filter android:priority="1000">
                <action android:name="cn.jpush.im.android.action.IM_RESPONSE" />
                <action android:name="cn.jpush.im.android.action.NOTIFICATION_CLICK_PROXY" />

                <category android:name="${applicationId}" />
            </intent-filter>
        </receiver>

        <!-- option 可选项。用于同一设备中不同应用的JPush服务相互拉起的功能。 -->
        <!-- 若不启用该功能可删除该组件，将不拉起其他应用也不能被其他应用拉起 -->
        <service
            android:name="cn.jpush.android.service.DaemonService"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="cn.jpush.android.intent.DaemonService" />
                <category android:name="${applicationId}" />
            </intent-filter>

        </service>

        <!-- Required. Enable it you can get statistics data with channel -->
        <meta-data
            android:name="JPUSH_CHANNEL"
            android:value="developer-default" />
        <!-- Required. AppKey copied from Portal -->
        <meta-data
            android:name="JPUSH_APPKEY"
            android:value="4f7aef34fb361292c566a1cd" /><!--  </>值来自开发者平台取得的AppKey-->

```

* 初始化jmessage－sdk

在你的application类中，需要调用以下方法以初始化jmessage－sdk
> demo JChatApplication.java

```
  JMessageClient.init(getApplicationContext());
  SharePreferenceManager.init(getApplicationContext(), JCHAT_CONFIGS);
  JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_DEFAULT);
  new NotificationClickEventReceiver(getApplicationContext());
```
如果你想自定义Notification的样式，可以将上面的NotificationMode的值设为No_Notification，并且去掉下面的

NotificationClickEventReceiver，然后在接收到消息时再创建Notification（下面会说到）。

在你的启动Activity的onPause()和onResume()方法中需要分别调用

```
JPushInterface.onPause(this);
```
以及
```
JPushInterface.onResume(this);
```

####jmessage－sdk主要接口的使用

- 接收消息

 在Activity的onCreate()方法中先调用

```
 JMessageClient.registerEventReceiver(this);
```
 然后重写onEvent()方法，刷新聊天界面，如下所示：

> demo ChatActivity.java onEvent()

```
    /**
     * 接收消息类事件
     *
     * @param event 消息事件
     */
    public void onEvent(MessageEvent event) {
        final Message msg = event.getMessage();
        
        //可以在这里创建Notification
        ...
        
        //刷新消息
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //收到消息的类型为单聊
                if (msg.getTargetType() == ConversationType.single) {
                    String targetID = ((UserInfo) msg.getTargetInfo()).getUserName();
                    //判断消息是否在当前会话中
                    if (targetID.equals(mTargetId)) {
                        Message lastMsg = mChatAdapter.getLastMsg();
                        //收到的消息和Adapter中最后一条消息比较，如果最后一条为空或者不相同，则加入到MsgList
                        if (lastMsg == null || msg.getId() != lastMsg.getId()) {
                            mChatAdapter.addMsgToList(msg);
                        } else {
                            mChatAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    long groupId = ((GroupInfo)msg.getTargetInfo()).getGroupID();
                    if (!mIsSingle && groupId == mGroupId) {
                        Message lastMsg = mChatAdapter.getLastMsg();
                        if (lastMsg == null || msg.getId() != lastMsg.getId()) {
                            mChatAdapter.addMsgToList(msg);
                        } else {
                            mChatAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }
```

其中，MessageEvent中的Message类型可以是普通消息，也可以是EventNotification或者Custom类型，可以根据ContentType分别处理。

- 发送消息

  发送文本消息：
```
   //其中msgContent为string，mConv为Conversation
   TextContent content = new TextContent(msgContent);
   Message msg = mConv.createSendMessage(content);
   JMessageClient.sendMessage(msg);
```

  发送语音消息：
```
  //mRecAudioFile为录音文件，duration为录音时长
  VoiceContent content = new VoiceContent(myRecAudioFile, duration);
  Message msg = mConv.createSendMessage(content);
  JMessageClient.sendMessage(msg);
```

  发送图片消息
```
  ImageContent.createImageContentAsync(bitmap, new ImageContent.CreateImageContentCallback() {
      @Override
      public void gotResult(int status, String desc, ImageContent imageContent) {
          if (status == 0) {
              Message msg = mConv.createSendMessage(imageContent);
              JMessageClient.sendMessage(msg);
          }
      }
  });
```




