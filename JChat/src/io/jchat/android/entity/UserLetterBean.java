package io.jchat.android.entity;

public class UserLetterBean {

    private String mNickname;
    private String mLetter;

    public void setNickname(String nickname){
        this.mNickname = nickname;
    }

    public String getNickname(){
        return mNickname;
    }

    public void setLetter(String letter){
        this.mLetter = letter;
    }

    public String getLetter(){
        return mLetter;
    }
}
