package jiguang.chat.utils;

import java.util.ArrayList;
import java.util.Comparator;

import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.utils.pinyin.HanziToPinyin;

/**
 * Created by ${chenyn} on 2017/11/3.
 */

public class GroupMemberListComparator implements Comparator<UserInfo>{
    @Override
    public int compare(UserInfo o1, UserInfo o2) {
        if (getLetter(o1.getDisplayName()).equals("@") ||
                getLetter(o2.getDisplayName()).equals("#")) {
            return -1;
        }else if (getLetter(o1.getDisplayName()).equals("#")
                || getLetter(o2.getDisplayName()).equals("@")) {
            return 1;
        }else {
            return getLetter(o1.getDisplayName()).compareTo(getLetter(o2.getDisplayName()));
        }
    }

    public String getLetter(String name) {
        String letter;
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance()
                .get(name);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (token.type == HanziToPinyin.Token.PINYIN) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }
        String sortString = sb.toString().substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Z]")) {
            letter = sortString.toUpperCase();
        } else {
            letter = "#";
        }
        return letter;
    }
}
