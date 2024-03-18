package com.eu.habbo.habbohotel.items.interactions.games.tag;

import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.tag.TagGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.RoomItemManager;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class InteractionTagField extends RoomItem {
    public Class<? extends Game> gameClazz;

    public InteractionTagField(ResultSet set, Item baseItem, Class<? extends Game> gameClazz) throws SQLException {
        super(set, baseItem);

        this.gameClazz = gameClazz;
    }

    public InteractionTagField(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells, Class<? extends Game> gameClazz) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);

        this.gameClazz = gameClazz;
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        Habbo habbo = room.getRoomUnitManager().getHabboByRoomUnit(roomUnit);

        if (habbo != null) {
            return habbo.getHabboInfo().getCurrentGame() == null || habbo.getHabboInfo().getCurrentGame() == this.gameClazz;
        }

        return false;
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {

    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        Habbo habbo = room.getRoomUnitManager().getHabboByRoomUnit(roomUnit);

        if (habbo != null) {
            if (habbo.getHabboInfo().getCurrentGame() == null) {
                TagGame game = (TagGame) room.getGame(this.gameClazz);

                if (game == null) {
                    game = (TagGame) this.gameClazz.getDeclaredConstructor(Room.class).newInstance(room);
                    room.addGame(game);
                }

                game.addHabbo(habbo, null);
                habbo.getHabboInfo().setCurrentGame(this.gameClazz);
            }
        }
    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {

    }

    @Override
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt((this.isLimited() ? 256 : 0));
        serverMessage.appendString(this.getExtraData());

        super.serializeExtradata(serverMessage);
    }

    @Override
    public void removeThisItem(RoomItemManager roomItemManager) {
        synchronized (roomItemManager.getUndefinedSpecials()) {
            roomItemManager.getUndefinedSpecials().remove(getId());
        }
    }

    @Override
    public void addThisItem(RoomItemManager roomItemManager) {
        synchronized (roomItemManager.getUndefinedSpecials()) {
            roomItemManager.getUndefinedSpecials().put(getId(), this);
        }
    }
}