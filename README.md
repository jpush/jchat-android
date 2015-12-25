# jchat-android

[![Join the chat at https://gitter.im/jpush/jchat-android](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/jpush/jchat-android?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
JChat android app. A real app for jmessage.

[JChat App](http://jchat.io)

[JChat iOS](https://github.com/jpush/jchat-ios)

[JChat Web](https://github.com/jpush/jchat-web)

####在Android Studio中导入JChat demo

1、下载jchat.zip或者在[这里](https://www.jpush.cn/common/downloads/sdk/im_android/)下载.

2、解压jchat.zip，在Android Studio中新建一个project或者在你当前的project中选择import module--> jchat（选择jchat文件夹）

![如图](https://github.com/jpush/jchat-android/raw/master/screenshots/)

3、修改jchat module下的build.gradle文件，将buildToolsVersion改为你Android Studio当前所使用的版本，sync一下。

4、修改AndroidManifest，将Manifest中“您的包名”、“您的AppKey”全部替换为你在JPush控制台上注册应用的包名和对应的AppKey。（或者

将“您的包名”全部替换成${applicationId}，然后在build.gradle文件中defaultConfig集合中声明一个applicationId

![如图](http://pan.baidu.com/s/1eQNsCW6)）
