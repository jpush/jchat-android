package jiguang.chat.controller;

import android.app.Dialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.activity.FinishRegisterActivity;
import jiguang.chat.activity.LoginActivity;
import jiguang.chat.activity.MainActivity;
import jiguang.chat.application.JGApplication;
import jiguang.chat.database.UserEntry;
import jiguang.chat.utils.DialogCreator;
import jiguang.chat.utils.SharePreferenceManager;
import jiguang.chat.utils.ToastUtil;

/**
 * Created by ${chenyn} on 2017/2/16.
 */

public class LoginController implements View.OnClickListener {

    private LoginActivity mContext;

    public LoginController(LoginActivity loginActivity) {
        this.mContext = loginActivity;
    }

    private boolean isContainChinese(String str) {
        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    private boolean whatStartWith(String str) {
        Pattern pattern = Pattern.compile("^([A-Za-z]|[0-9])");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    private boolean whatContain(String str) {
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z][a-zA-Z0-9_\\-@\\.]{3,127}$");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                //登陆验证
                final String userId = mContext.getUserId();
                final String password = mContext.getPassword();
                if (TextUtils.isEmpty(userId)) {
                    ToastUtil.shortToast(mContext, "用户名不能为空");
                    mContext.mLogin_userName.setShakeAnimation();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    ToastUtil.shortToast(mContext, "密码不能为空");
                    mContext.mLogin_passWord.setShakeAnimation();
                    return;
                }
                if (userId.length() < 4 || userId.length() > 128) {
                    mContext.mLogin_userName.setShakeAnimation();
                    ToastUtil.shortToast(mContext, "用户名为4-128位字符");
                    return;
                }
                if (password.length() < 4 || password.length() > 128) {
                    mContext.mLogin_userName.setShakeAnimation();
                    ToastUtil.shortToast(mContext, "密码为4-128位字符");
                    return;
                }
                if (isContainChinese(userId)) {
                    mContext.mLogin_userName.setShakeAnimation();
                    ToastUtil.shortToast(mContext, "用户名不支持中文");
                    return;
                }
                if (!whatStartWith(userId)) {
                    mContext.mLogin_userName.setShakeAnimation();
                    ToastUtil.shortToast(mContext, "用户名以字母或者数字开头");
                    return;
                }
                if (!whatContain(userId)) {
                    mContext.mLogin_userName.setShakeAnimation();
                    ToastUtil.shortToast(mContext, "只能含有: 数字 字母 下划线 . - @");
                    return;
                }
                //登陆
                if (JGApplication.registerOrLogin % 2 == 1) {
                    final Dialog dialog = DialogCreator.createLoadingDialog(mContext,
                            mContext.getString(R.string.login_hint));
                    dialog.show();
                    JMessageClient.login(userId, password, new BasicCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage) {
                            dialog.dismiss();
                            if (responseCode == 0) {
                                SharePreferenceManager.setCachedPsw(password);
                                UserInfo myInfo = JMessageClient.getMyInfo();
                                File avatarFile = myInfo.getAvatarFile();
                                //登陆成功,如果用户有头像就把头像存起来,没有就设置null
                                if (avatarFile != null) {
                                    SharePreferenceManager.setCachedAvatarPath(avatarFile.getAbsolutePath());
                                } else {
                                    SharePreferenceManager.setCachedAvatarPath(null);
                                }
                                String username = myInfo.getUserName();
                                String appKey = myInfo.getAppKey();
                                UserEntry user = UserEntry.getUser(username, appKey);
                                if (null == user) {
                                    user = new UserEntry(username, appKey);
                                    user.save();
                                }
                                mContext.goToActivity(mContext, MainActivity.class);
                                ToastUtil.shortToast(mContext, "登陆成功");
                                mContext.finish();
                            } else {
                                ToastUtil.shortToast(mContext, "登陆失败" + responseMessage);
                            }
                        }
                    });
                    //注册
                } else {
                    final String uri = "https://api.im.jpush.cn/v1" + "/users/" + userId;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String httpGet = executeHttpPost(uri);
                            if (httpGet == null) {
                                SharePreferenceManager.setRegisterName(userId);
                                SharePreferenceManager.setRegistePass(password);
                                mContext.startActivity(new Intent(mContext, FinishRegisterActivity.class));
                            } else {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtil.shortToast(mContext, "该用户已经存在");
                                    }
                                });
                            }

                        }
                    }).start();

                }
                break;
            case R.id.login_register:
            case R.id.new_user:
                mContext.mLogin_passWord.setText("");
                JGApplication.registerOrLogin++;
                if (JGApplication.registerOrLogin % 2 == 0) {
                    mContext.mBtn_login.setText("注册");
                    mContext.mNewUser.setText("去登陆");
                    mContext.mLogin_register.setText("立即登陆");
                    mContext.mLogin_desc.setText("已有账号? ");
                } else {
                    mContext.mBtn_login.setText("登录");
                    mContext.mNewUser.setText("新用户");
                    mContext.mLogin_register.setText("立即注册");
                    mContext.mLogin_desc.setText("还没有账号? ");
                }
                break;
        }
    }

    public String executeHttpPost(final String uid) {
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            //GET请求直接在链接后面拼上请求参数
            URL url = new URL(uid);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //Get请求不需要DoOutPut
            conn.setDoOutput(false);
            conn.setDoInput(true);
            //设置连接超时时间和读取超时时间
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Basic NGY3YWVmMzRmYjM2MTI5MmM1NjZhMWNkOjA1NGQ2MTAzODIzYTcyNmZjMTJkMDQ2Ng==");
            //连接服务器
            conn.connect();
            // 取得输入流，并使用Reader读取
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            return null;
        }
        //关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }


}
