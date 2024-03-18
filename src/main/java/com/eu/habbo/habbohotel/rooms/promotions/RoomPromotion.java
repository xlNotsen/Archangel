package com.eu.habbo.habbohotel.rooms.promotions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Getter
@Setter
public class RoomPromotion {

    private final Room room;
    public boolean needsUpdate;

    private String title;

    private String description;

    private int endTimestamp;
    private int startTimestamp;
    private int category;

    public RoomPromotion(Room room, String title, String description, int endTimestamp, int startTimestamp, int category) {
        this.room = room;
        this.title = title;
        this.description = description;
        this.endTimestamp = endTimestamp;
        this.startTimestamp = startTimestamp;
        this.category = category;
    }

    public RoomPromotion(Room room, ResultSet set) throws SQLException {
        this.room = room;
        this.title = set.getString("title");
        this.description = set.getString("description");
        this.endTimestamp = set.getInt("end_timestamp");
        this.startTimestamp = set.getInt("start_timestamp");
        this.category = set.getInt("category");
    }

    public void save() {
        if (this.needsUpdate) {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE room_promotions SET title = ?, description = ?, category = ? WHERE room_id = ?")) {
                statement.setString(1, this.title);
                statement.setString(2, this.description);
                statement.setInt(3, this.category);
                statement.setInt(4, this.room.getRoomInfo().getId());
                statement.executeUpdate();
            } catch (SQLException e) {
                log.error("Caught SQL exception", e);
            }

            this.needsUpdate = false;
        }
    }

    public void addEndTimestamp(int time) {
        this.endTimestamp += time;
    }

}
