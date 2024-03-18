package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
public class BotsComponent {

    @Getter
    private final THashMap<Integer, Bot> bots = new THashMap<>();

    public BotsComponent(Habbo habbo) {
        this.loadBots(habbo);
    }

    private void loadBots(Habbo habbo) {
        synchronized (this.bots) {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT users.username AS owner_name, bots.* FROM bots INNER JOIN users ON users.id = bots.user_id WHERE user_id = ? AND room_id = 0 ORDER BY id ASC")) {
                statement.setInt(1, habbo.getHabboInfo().getId());
                try (ResultSet set = statement.executeQuery()) {
                    while (set.next()) {
                        Bot bot = Emulator.getGameEnvironment().getBotManager().loadBot(set);
                        if (bot != null) {
                            this.bots.put(set.getInt("id"), bot);
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("Caught SQL exception", e);
            }
        }
    }

    public Bot getBot(int botId) {
        return this.bots.get(botId);
    }

    public void addBot(Bot bot) {
        synchronized (this.bots) {
            this.bots.put(bot.getId(), bot);
        }
    }

    public void removeBot(Bot bot) {
        synchronized (this.bots) {
            this.bots.remove(bot.getId());
        }
    }

    public void dispose() {
        synchronized (this.bots) {
            for (Map.Entry<Integer, Bot> map : this.bots.entrySet()) {
                if (map.getValue().isSqlUpdateNeeded()) {
                    Emulator.getThreading().run(map.getValue());
                }
            }
            this.bots.clear();
        }
    }
}
