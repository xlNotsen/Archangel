package com.eu.habbo.habbohotel.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.database.DatabaseConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

@Slf4j
@Getter
public class ModToolBan implements Runnable {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final int userId;
    private final String ip;
    private final String machineId;
    private final int staffId;
    private final int expireDate;
    private final int timestamp;
    private final String reason;
    private final ModToolBanType type;
    private final int cfhTopic;

    private final boolean needsInsert;

    public ModToolBan(ResultSet set) throws SQLException {
        this.userId = set.getInt(DatabaseConstants.USER_ID);
        this.ip = set.getString("ip");
        this.machineId = set.getString("machine_id");
        this.staffId = set.getInt("user_staff_id");
        this.timestamp = set.getInt("timestamp");
        this.expireDate = set.getInt("ban_expire");
        this.reason = set.getString("ban_reason");
        this.type = ModToolBanType.fromString(set.getString("type"));
        this.cfhTopic = set.getInt("cfh_topic");
        this.needsInsert = false;
    }

    public ModToolBan(int userId, String ip, String machineId, int staffId, int expireDate, String reason, ModToolBanType type, int cfhTopic) {
        this.userId = userId;
        this.staffId = staffId;
        this.timestamp = Emulator.getIntUnixTimestamp();
        this.expireDate = expireDate;
        this.reason = reason;
        this.ip = ip;
        this.machineId = machineId;
        this.type = type;
        this.cfhTopic = cfhTopic;
        this.needsInsert = true;
    }

    @Override
    public void run() {
        if (this.needsInsert) {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO bans (user_id, ip, machine_id, user_staff_id, timestamp, ban_expire, ban_reason, type, cfh_topic) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                statement.setInt(1, this.userId);
                statement.setString(2, this.ip);
                statement.setString(3, this.machineId);
                statement.setInt(4, this.staffId);
                statement.setInt(5, Emulator.getIntUnixTimestamp());
                statement.setInt(6, this.expireDate);
                statement.setString(7, this.reason);
                statement.setString(8, this.type.getType());
                statement.setInt(9, this.cfhTopic);
                statement.execute();
            } catch (SQLException e) {
                log.error("Caught SQL exception", e);
            }
        }
    }

    public String listInfo() {
        return "Banned User Id: " + this.userId + "\r" +
                "Type: " + this.type.getType() + "\r" +
                "Reason: " + "<i>" + this.reason + "</i>" + "\r" +
                "Moderator Id: " + this.staffId + "\r" +
                "Date: " + dateFormat.format(this.timestamp * 1000L) + "\r" +
                "Expire Date: " + dateFormat.format(this.expireDate * 1000L) + "\r" +
                "IP: " + this.ip + "\r" +
                "MachineID: " + this.machineId + "\r" +
                "Topic: " + this.cfhTopic;
    }
}
