package com.eu.habbo.habbohotel.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.database.DatabaseConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class Guild implements Runnable {
    public boolean needsUpdate;
    public int lastRequested = Emulator.getIntUnixTimestamp();
    @Setter
    @Getter
    private int id;
    @Getter
    private final int ownerId;
    @Getter
    private final String ownerName;
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String description;
    @Getter
    private final int roomId;
    @Setter
    @Getter
    private String roomName;
    @Setter
    @Getter
    private GuildState state;
    @Setter
    @Getter
    private boolean rights;
    @Setter
    @Getter
    private int colorOne;
    @Setter
    @Getter
    private int colorTwo;
    @Setter
    @Getter
    private String badge;
    @Getter
    private final int dateCreated;
    @Getter
    private int memberCount;
    @Getter
    private int requestCount;
    @Setter
    private boolean hasForum = false;
    @Setter
    private SettingsState readForum = SettingsState.ADMINS;
    @Setter
    private SettingsState postMessages = SettingsState.ADMINS;
    @Setter
    private SettingsState postThreads = SettingsState.ADMINS;
    private SettingsState modForum = SettingsState.ADMINS;

    public Guild(ResultSet set) throws SQLException {
        this.id = set.getInt("id");
        this.ownerId = set.getInt(DatabaseConstants.USER_ID);
        this.ownerName = set.getString("username");
        this.name = set.getString("name");
        this.description = set.getString("description");
        this.state = GuildState.values()[set.getInt("state")];
        this.roomId = set.getInt("room_id");
        this.roomName = set.getString("room_name");
        this.rights = set.getString("rights").equalsIgnoreCase("1");
        this.colorOne = set.getInt("color_one");
        this.colorTwo = set.getInt("color_two");
        this.badge = set.getString("badge");
        this.dateCreated = set.getInt("date_created");
        this.hasForum = set.getString("forum").equalsIgnoreCase("1");
        this.readForum = SettingsState.valueOf(set.getString("read_forum"));
        this.postMessages = SettingsState.valueOf(set.getString("post_messages"));
        this.postThreads = SettingsState.valueOf(set.getString("post_threads"));
        this.modForum = SettingsState.valueOf(set.getString("mod_forum"));
        this.memberCount = 0;
        this.requestCount = 0;
    }

    public Guild(int ownerId, String ownerName, int roomId, String roomName, String name, String description, int colorOne, int colorTwo, String badge) {
        this.id = 0;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.roomId = roomId;
        this.roomName = roomName;
        this.name = name;
        this.description = description;
        this.state = GuildState.OPEN;
        this.rights = false;
        this.colorOne = colorOne;
        this.colorTwo = colorTwo;
        this.badge = badge;
        this.memberCount = 0;
        this.dateCreated = Emulator.getIntUnixTimestamp();
    }

    public void loadMemberCount() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(id) as count FROM guilds_members WHERE level_id < 3 AND guild_id = ?")) {
                statement.setInt(1, this.id);
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        this.memberCount = set.getInt(1);
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(id) as count FROM guilds_members WHERE level_id = 3 AND guild_id = ?")) {
                statement.setInt(1, this.id);
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        this.requestCount = set.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    @Override
    public void run() {
        if (this.needsUpdate) {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE guilds SET name = ?, description = ?, state = ?, rights = ?, color_one = ?, color_two = ?, badge = ?, read_forum = ?, post_messages = ?, post_threads = ?, mod_forum = ?, forum = ? WHERE id = ?")) {
                statement.setString(1, this.name);
                statement.setString(2, this.description);
                statement.setInt(3, this.state.getState());
                statement.setString(4, this.rights ? "1" : "0");
                statement.setInt(5, this.colorOne);
                statement.setInt(6, this.colorTwo);
                statement.setString(7, this.badge);
                statement.setString(8, this.readForum.name());
                statement.setString(9, this.postMessages.name());
                statement.setString(10, this.postThreads.name());
                statement.setString(11, this.modForum.name());
                statement.setString(12, this.hasForum ? "1" : "0");
                statement.setInt(13, this.id);
                statement.execute();

                this.needsUpdate = false;
            } catch (SQLException e) {
                log.error("Caught SQL exception", e);
            }
        }
    }

    public void increaseMemberCount() {
        this.memberCount++;
    }

    public void decreaseMemberCount() {
        this.memberCount--;
    }

    public void increaseRequestCount() {
        this.requestCount++;
    }

    public void decreaseRequestCount() {
        this.requestCount--;
    }

    public boolean hasForum() {
        return this.hasForum;
    }

    public SettingsState canReadForum() {
        return this.readForum;
    }

    public SettingsState canPostMessages() {
        return this.postMessages;
    }

    public SettingsState canPostThreads() {
        return this.postThreads;
    }

    public SettingsState canModForum() {
        return this.modForum;
    }

    public void setModForum(SettingsState modForum) {
        this.modForum = modForum;
    }
}
