package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@AllArgsConstructor
public class QueryDeleteHabboItem implements Runnable {

    private final int itemId;


    public QueryDeleteHabboItem(RoomItem item) {
        this.itemId = item.getId();
    }

    @Override
    public void run() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM items WHERE id = ?")) {
            statement.setInt(1, this.itemId);
            statement.execute();
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }
}
