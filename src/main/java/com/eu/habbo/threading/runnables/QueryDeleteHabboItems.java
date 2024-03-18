package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import gnu.trove.map.TIntObjectMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@AllArgsConstructor
public class QueryDeleteHabboItems implements Runnable {

    private TIntObjectMap<RoomItem> items;


    @Override
    public void run() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM items WHERE id = ?")) {
            for (RoomItem item : this.items.valueCollection()) {
                if (item.getRoomId() > 0)
                    continue;

                statement.setInt(1, item.getId());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }

        this.items.clear();
    }
}
