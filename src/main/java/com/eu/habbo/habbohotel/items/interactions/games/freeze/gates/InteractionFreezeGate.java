package com.eu.habbo.habbohotel.items.interactions.games.freeze.gates;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.games.freeze.FreezeGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameGate;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionFreezeGate extends InteractionGameGate {
    public InteractionFreezeGate(ResultSet set, Item baseItem, GameTeamColors teamColor) throws SQLException {
        super(set, baseItem, teamColor);
    }

    public InteractionFreezeGate(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells, GameTeamColors teamColor) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells, teamColor);
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return room.getGame(FreezeGame.class) == null || room.getGame(FreezeGame.class).state.equals(GameState.IDLE);
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {
    }

    @Override
    public boolean isWalkable() {
        Room room = Emulator.getGameEnvironment().getRoomManager().getActiveRoomById(this.getRoomId());

        if (room == null) return false;

        Game game = room.getGame(FreezeGame.class);

        return game == null || game.getState() == GameState.IDLE;
    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        FreezeGame game = (FreezeGame) room.getGame(FreezeGame.class);

        if (game == null) {
            game = FreezeGame.class.getDeclaredConstructor(Room.class).newInstance(room);
            room.addGame(game);
        }

        GameTeam team = game.getTeamForHabbo(room.getRoomUnitManager().getHabboByRoomUnit(roomUnit));

        if (team != null) {
            game.removeHabbo(room.getRoomUnitManager().getHabboByRoomUnit(roomUnit));
        } else {
            game.addHabbo(room.getRoomUnitManager().getHabboByRoomUnit(roomUnit), this.teamColor);
        }

        updateState(game, 5);

        super.onWalkOn(roomUnit, room, objects);
    }
}
