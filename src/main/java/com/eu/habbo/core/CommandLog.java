package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import lombok.AllArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class CommandLog implements DatabaseLoggable {
    private static final String INSERT_QUERY = "INSERT INTO commandlogs (`user_id`, `timestamp`, `command`, `params`, `succes`) VALUES (?, ?, ?, ?, ?)";

    private final int userId;
    private final int timestamp = Emulator.getIntUnixTimestamp();
    private final Command command;
    private final String params;
    private final boolean succes;

    @Override
    public String getQuery() {
        return CommandLog.INSERT_QUERY;
    }

    @Override
    public void log(PreparedStatement statement) throws SQLException {
        statement.setInt(1, this.userId);
        statement.setInt(2, this.timestamp);
        statement.setString(3, this.command.getClass().getSimpleName());
        statement.setString(4, this.params);
        statement.setString(5, this.succes ? "yes" : "no");
        statement.addBatch();
    }

}