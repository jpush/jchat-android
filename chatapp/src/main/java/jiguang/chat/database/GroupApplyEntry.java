package jiguang.chat.database;

/**
 * Created by ${chenyn} on 2017/7/18.
 */

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "group_apply", id = "_id")
public class GroupApplyEntry extends Model {

    @Column(name = "FromUsername")
    public String fromUsername;

    @Column(name = "ToUsername")
    public String toUsername;

    @Column(name = "AppKey")
    public String appKey;

    @Column(name = "Avatar")
    public String Avatar;

    @Column(name = "FromDisplayName")
    public String fromDisplayName;

    @Column(name = "toDisplayName")
    public String toDisplayName;

    @Column(name = "Reason")
    public String reason;

    @Column(name = "State")
    public String state;

    @Column(name = "EventJson")
    public String eventJson;

    @Column(name = "GroupName")
    public String groupName;

    @Column(name = "User")
    public UserEntry user;

    @Column(name = "BtnState")
    public int btnState;

    @Column(name = "GroupType")
    public int groupType;

    public GroupApplyEntry() {
        super();
    }

    public GroupApplyEntry(String fromUsername, String toUsername, String appKey, String Avatar, String fromDisplayName, String toDisplayName,
                           String reason, String state, String eventJson, String groupName, UserEntry user, int btnState, int groupType) {
        super();
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.appKey = appKey;
        this.Avatar = Avatar;
        this.fromDisplayName = fromDisplayName;
        this.toDisplayName = toDisplayName;
        this.reason = reason;
        this.state = state;
        this.eventJson = eventJson;
        this.groupName = groupName;
        this.user = user;
        this.btnState = btnState;
        this.groupType = groupType;
    }

    public static GroupApplyEntry getEntry(UserEntry user, String username, String appKey) {
        return new Select().from(GroupApplyEntry.class).where("ToUsername = ?", username)
                .where("AppKey = ?", appKey)
                .where("User = ?", user.getId()).executeSingle();
    }

    public static GroupApplyEntry getEntry(long id) {
        return new Select().from(GroupApplyEntry.class).where("_id = ?", id).executeSingle();
    }

    public static void deleteEntry(GroupApplyEntry entry) {
        new Delete().from(GroupApplyEntry.class).where("_id = ?", entry.getId()).execute();
    }

}
