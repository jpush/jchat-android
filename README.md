
## Android JChat App

[JChat iOS](https://github.com/jpush/jchat-ios)

[JChat-swift](https://github.com/jpush/jchat-swift)

[JChat Web](https://github.com/jpush/jchat-web)


#### 目前 JChat 已提供两种模式:好友模式和无好友模式，通过搜索后可以选择添加好友或者直接发起聊天(添加好友还是从右上角的 **+** 处添加，通讯录中的搜索框只是过滤字符串)

- 无好友模式：搜索后可直接发起会话
- 好友模式：向对方发送好友请求，对方同意后，双方互为好友才能聊天。在通讯录的朋友推荐中可以查看好友请求的情况。

![jchat](https://github.com/jpush/jchat-android/tree/master/JChat/res/gif/jchat.gif)

如需发送地理位置功能，需要在 AndroidManifest.xml 中配置一下百度地图的 API AccessKey：

```
<meta-data android:name="com.baidu.lbsapi.API_KEY"
            android:value="UAkQeBK84ioVGzYgA1rSWYfuD4xYtpmV"/> // 将这个 value 替换成自己的
```

在你的项目中集成 jmessage-sdk，[参考这个步骤](https://github.com/KenChoi1992/SomeArticles/blob/master/%E9%9B%86%E6%88%90%20jmessage-sdk.md)；

在 Android Studio 中导入 JChat demo， [参考这个步骤](https://github.com/KenChoi1992/SomeArticles/blob/master/%E5%9C%A8Android%20Studio%E4%B8%AD%E8%BF%90%E8%A1%8CJChat%20Demo.md)；

在 Eclipse 中导入 JChat，新版本加入了 ActiveAndroid 依赖，需要手动添加（Android Studio 不需要）[具体参考这个步骤](https://github.com/KenChoi1992/SomeArticles/blob/master/%E5%9C%A8Eclipse%E4%B8%8A%E5%AF%BC%E5%85%A5JChat.md)。

#### JChat 的工程结构

JChat 的架构模型参考了 Android Passive MVC 架构(但是去掉了 Listener 模块)，有兴趣的可以[参考这里](http://pan.baidu.com/s/1mhoms4o)以及这篇文章[《Android Passive MVC 架构》](http://www.jianshu.com/p/1af58b6e8930)

- Application —— 主要作用是 jmessage-sdk 的初始化以及 Notification 的相关设置；

- chatting —— 聊天界面相关类，如果开发者只要聊天界面可以只拷贝此文件夹下的文件，而资源文件可以从 [JChat-UIKit-Chatting](https://github.com/jpush/jmessage-android-uikit/tree/master/Chatting) 中拷贝；

- activity 包 —— JChat 的 Activity 的集合，主要负责绑定 Controller 和 View，以及界面的跳转；

- controller 包 —— 主要负责事件的点击、数据处理等，是 Activity 和 View 的中间层；

- view 包 —— 主要负责界面的展示、控件的初始化、点击事件的绑定等；

- adapter 包 —— 主要负责 ListView 或 GridView 的数据处理；

- tools 包 —— 工具类的集合。


#### jmessage-sdk 接口相关文档
- [Demo 用法参考](https://github.com/KenChoi1992/SomeArticles/blob/master/jmessage-sdk%E9%83%A8%E5%88%86%E6%8E%A5%E5%8F%A3%E7%94%A8%E6%B3%95.md)；

- [Android JMessage-sdk 概述](https://docs.jiguang.cn/jmessage/guideline/jmessage_guide/)；

- [Android JMessage-sdk-doc 文档](https://docs.jiguang.cn/jmessage/client/im_android_api_docs/)。

---
#### JChat 中所使用的开源项目简介

- android-shape-imageview —— 自定义 ImageView 的形状，[github 地址](https://github.com/siyamed/android-shape-imageview)；

- PhotoView —— 根据手势缩放图片，[github 地址](https://github.com/chrisbanes/PhotoView)；

- DropDownListView —— 下拉刷新 ListView，[github 地址](https://github.com/Trinea/android-common)；

- NativeImageLoader —— 扫描手机中的图片，[blog 地址](http://blog.csdn.net/xiaanming/article/details/18730223)；

- EventBus —— 在组件之间传递消息，[github 地址](https://github.com/greenrobot/EventBus)；

- Picasso —— 加载、显示、缓存图片，[官网](http://square.github.io/picasso/)。

- StickyListHeadersListView —— 固定索引 HeadView 的 ListView [github 地址](https://github.com/emilsjolander/StickyListHeaders)(可能更名了 ＝_＝)

- ActiveAndroid —— 对象关系映射（ORM）操作数据库 [github 地址](https://github.com/pardom/ActiveAndroid)
