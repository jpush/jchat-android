package io.jchat.android.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;


@Table(name = "friend_recommends", id = "_id")
public class FriendRecommendEntry extends Model {

    @Column(name = "Username")
    public String username;

    @Column(name = "AppKey")
    public String appKey;

    @Column(name = "Avatar")
    public String avatar;

    @Column(name = "DisplayName")
    public String displayName;

    @Column(name = "Reason")
    public String reason;

    @Column(name = "State")
    public String state;

    @Column(name = "User")
    public UserEntry user;

    public FriendRecommendEntry() {
        super();
    }

    public FriendRecommendEntry(String username, String appKey, String avatar,
                                String displayName, String reason, String state, UserEntry user) {
        super();
        this.username = username;
        this.appKey = appKey;
        this.avatar = avatar;
        this.displayName = displayName;
        this.reason = reason;
        this.state = state;
        this.user = user;
    }


    public static FriendRecommendEntry getEntry(UserEntry user, String username, String appKey) {
        return new Select().from(FriendRecommendEntry.class).where("Username = ?", username)
                .where("AppKey = ?", appKey)
                .where("User = ?", user.getId()).executeSingle();
    }

    public static FriendRecommendEntry getEntry(long id) {
        return new Select().from(FriendRecommendEntry.class).where("_id = ?", id).executeSingle();
    }

    public static void deleteEntry(FriendRecommendEntry entry) {
        new Delete().from(FriendRecommendEntry.class).where("_id = ?", entry.getId()).execute();
    }
}