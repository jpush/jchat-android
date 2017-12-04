package jiguang.chat.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

/**
 * Created by ${chenyn} on 2017/11/24.
 */

@Table(name = "refuse_group", id = "_id")
public class RefuseGroupEntry extends Model {

    @Column(name = "Username")
    public String username;

    @Column(name = "DisplayName")
    public String displayName;

    @Column(name = "GroupId")
    public String groupId;

    @Column(name = "AppKey")
    public String appKey;

    @Column(name = "Avatar")
    public String avatar;

    @Column(name = "User")
    public UserEntry user;

    public RefuseGroupEntry() {}

    public RefuseGroupEntry(UserEntry user, String username, String displayName, String groupId, String appKey, String avatar) {
        super();
        this.user = user;
        this.username = username;
        this.displayName = displayName;
        this.groupId = groupId;
        this.appKey = appKey;
        this.avatar = avatar;
    }

    public static RefuseGroupEntry getEntry(UserEntry user, String username, String appKey) {
        return new Select().from(RefuseGroupEntry.class).where("Username = ?", username)
                .where("AppKey = ?", appKey)
                .where("User = ?", user.getId()).executeSingle();
    }


}
