
Android JChat App

[JChat iOS](https://github.com/jpush/jchat-ios)

[JChat Web](https://github.com/jpush/jchat-web)

#####在你的项目中集成jmessage-sdk，[参考这个步骤](https://github.com/KenChoi1992/SomeArticles/blob/master/%E9%9B%86%E6%88%90jmessage-sdk.md)

#####在Android Studio中导入JChat demo [参考这个步骤](https://github.com/KenChoi1992/SomeArticles/blob/master/%E5%9C%A8Android%20Studio%E4%B8%AD%E8%BF%90%E8%A1%8CJChat%20Demo.md)

####JChat的工程结构

JChat的架构模型参考了Android Passive MVC架构(但是去掉了Listener模块)，有兴趣的可以[参考这里](http://pan.baidu.com/s/1mhoms4o)。

- Application 主要作用是jmessage－sdk的初始化以及Notification的相关设置

- chatting 聊天界面相关类，如果开发者只要聊天界面可以只拷贝此文件夹下的文件，而资源文件可以从[JChat-UIKit-Chatting](https://github.com/jpush/jmessage-android-uikit/tree/master/Chatting)中拷贝

- activity包 JChat的Activity的集合，主要负责绑定Controller和View，以及界面的跳转

- controller包 主要负责事件的点击、数据处理等，是Activity和View的中间层

- view包 主要负责界面的展示、控件的初始化、点击事件的绑定等

- adapter包 主要负责ListView或GridView的数据处理

- tools包 工具类的集合


####jmessage－sdk接口相关文档
- [Demo用法参考](https://github.com/KenChoi1992/SomeArticles/blob/master/jmessage-sdk%E9%83%A8%E5%88%86%E6%8E%A5%E5%8F%A3%E7%94%A8%E6%B3%95.md)

- [Android JMessage-sdk 概述](http://docs.jpush.io/client/im_sdk_android/)

- [Android JMessage-sdk-doc文档](http://docs.jpush.io/client/im_android_api_docs/)

---
####JChat中所使用的开源项目简介

- android-shape-imageview [github地址](https://github.com/siyamed/android-shape-imageview) 自定义ImageView的形状

- PhotoView [github地址](https://github.com/chrisbanes/PhotoView) 根据手势缩放图片

- DropDownListView [github地址](https://github.com/Trinea/android-common) 下拉刷新ListView

- NativeImageLoader [blog地址](http://blog.csdn.net/xiaanming/article/details/18730223) 扫描手机中的图片

- EventBus [github地址](https://github.com/greenrobot/EventBus) 在组件之间传递消息

- Picasso [官网](http://square.github.io/picasso/) 加载、显示、缓存图片




