package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class HabboBadge implements Runnable {
    @Getter
    private int id;
    @Setter
    @Getter
    private String code;
    @Setter
    @Getter
    private int slot;
    private Habbo habbo;
    private boolean needsUpdate;
    private boolean needsInsert;

    public HabboBadge(ResultSet set, Habbo habbo) throws SQLException {
        this.id = set.getInt("id");
        this.code = set.getString("badge_code");
        this.slot = set.getInt("slot_id");
        this.habbo = habbo;
        this.needsUpdate = false;
        this.needsInsert = false;
    }

    public HabboBadge(int id, String code, int slot, Habbo habbo) {
        this.id = id;
        this.code = code;
        this.slot = slot;
        this.habbo = habbo;
        this.needsUpdate = false;
        this.needsInsert = true;
    }

    @Override
    public void run() {
        try {
            if (this.needsInsert) {
                try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO users_badges (user_id, slot_id, badge_code) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, this.habbo.getHabboInfo().getId());
                    statement.setInt(2, this.slot);
                    statement.setString(3, this.code);
                    statement.execute();
                    try (ResultSet set = statement.getGeneratedKeys()) {
                        if (set.next()) {
                            this.id = set.getInt(1);
                        }
                    }
                }
                this.needsInsert = false;
            } else if (this.needsUpdate) {
                try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE users_badges SET slot_id = ?, badge_code = ? WHERE id = ? AND user_id = ?")) {
                    statement.setInt(1, this.slot);
                    statement.setString(2, this.code);
                    statement.setInt(3, this.id);
                    statement.setInt(4, this.habbo.getHabboInfo().getId());
                    statement.execute();
                }
                this.needsUpdate = false;
            }
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    public void needsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    public void needsInsert(boolean needsInsert) {
        this.needsInsert = needsInsert;
    }
}
