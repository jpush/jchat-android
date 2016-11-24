package io.jchat.android.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "friends", id = "_id")
public class FriendEntry extends Model {

    @Column(name = "Username")
    public String username;

    @Column(name = "AppKey")
    public String appKey;

    @Column(name = "Avatar")
    public String avatar;

    @Column(name = "DisplayName")
    public String displayName;

    @Column(name = "Letter")
    public String letter;

    @Column(name = "User")
    public UserEntry user;

    public FriendEntry() {
        super();
    }

    public FriendEntry(String username, String appKey, String avatar, String displayName, String letter,
                       UserEntry user) {
        super();
        this.username = username;
        this.appKey = appKey;
        this.avatar = avatar;
        this.displayName = displayName;
        this.letter = letter;
        this.user = user;
    }

    public static FriendEntry getFriend(String username, String appKey) {
        return new Select().from(FriendEntry.class).where("Username = ?", username)
                .where("AppKey = ?", appKey).executeSingle();
    }

    public static FriendEntry getFriend(long id) {
        return new Select().from(FriendEntry.class).where("_id = ?", id).executeSingle();
    }


}
