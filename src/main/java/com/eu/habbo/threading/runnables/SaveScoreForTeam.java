package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.rooms.Room;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@AllArgsConstructor
public class SaveScoreForTeam implements Runnable {

    public final GameTeam team;
    public final Game game;
    

    @Override
    public void run() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO room_game_scores (room_id, game_start_timestamp, game_name, user_id, team_id, score, team_score) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            for (GamePlayer player : this.team.getMembers()) {
                Room room = this.game.getRoom();
                statement.setInt(1, room.getRoomInfo().getId());
                statement.setInt(2, this.game.getStartTime());
                statement.setString(3, this.game.getClass().getName());
                statement.setInt(4, player.getHabbo().getHabboInfo().getId());
                statement.setInt(5, player.getTeamColor().type);
                statement.setInt(6, player.getScore());
                statement.setInt(7, this.team.getTeamScore());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }
}
