package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class CustomRoomLayout extends RoomLayout implements Runnable {

    private final int roomId;
    private boolean needsUpdate;

    public CustomRoomLayout(ResultSet set, Room room) {
        super(set, room);

        this.roomId = room.getRoomInfo().getId();
    }

    @Override
    public void run() {
        if (this.needsUpdate) {
            this.needsUpdate = false;

            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE room_models_custom SET door_x = ?, door_y = ?, door_dir = ?, heightmap = ? WHERE id = ? LIMIT 1")) {
                statement.setInt(1, this.getDoorX());
                statement.setInt(2, this.getDoorY());
                statement.setInt(3, this.getDoorDirection());
                statement.setString(4, this.getHeightmap());
                statement.setInt(5, this.roomId);
                statement.execute();
            } catch (SQLException e) {
                log.error("Caught SQL exception", e);
            }
        }
    }

    public boolean needsUpdate() {
        return this.needsUpdate;
    }

    public void needsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }
}
