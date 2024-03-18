package com.eu.habbo.habbohotel.items.interactions.games.battlebanzai;

import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.battlebanzai.BattleBanzaiGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.math3.util.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class InteractionBattleBanzaiTile extends RoomItem {
    public InteractionBattleBanzaiTile(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.setExtraData("0");
    }

    public InteractionBattleBanzaiTile(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
        this.setExtraData("0");
    }

    @Override
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt((this.isLimited() ? 256 : 0));
        serverMessage.appendString(this.getExtraData());

        super.serializeExtradata(serverMessage);
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return true;
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
        super.onWalkOn(roomUnit, room, objects);

        if (this.getExtraData().isEmpty())
            this.setExtraData("0");

        int state = Integer.parseInt(this.getExtraData());

        if (state % 3 == 2)
            return;

        Habbo habbo = room.getRoomUnitManager().getHabboByRoomUnit(roomUnit);

        if (habbo == null)
            return;

        if (this.isLocked())
            return;

        if (habbo.getHabboInfo().getCurrentGame() != null && habbo.getHabboInfo().getCurrentGame().equals(BattleBanzaiGame.class)) {
            BattleBanzaiGame game = ((BattleBanzaiGame) room.getGame(BattleBanzaiGame.class));

            if (game == null)
                return;

            if (!game.state.equals(GameState.RUNNING))
                return;

            game.markTile(habbo, this, state);
        }

    }

    public boolean isLocked() {
        if (this.getExtraData().isEmpty())
            return false;

        return Integer.parseInt(this.getExtraData()) % 3 == 2;
    }

    @Override
    public boolean canStackAt(List<Pair<RoomTile, THashSet<RoomItem>>> itemsAtLocation) {
        for (Pair<RoomTile, THashSet<RoomItem>> set : itemsAtLocation) {
            if (set.getValue() != null && !set.getValue().isEmpty()) return false;
        }

        return super.canStackAt(itemsAtLocation);
    }

    @Override
    public void onPickUp(Room room) {
        super.onPickUp(room);

        this.setExtraData("0");
        room.updateItem(this);
    }

    @Override
    public void onPlace(Room room) {
        super.onPlace(room);

        BattleBanzaiGame game = (BattleBanzaiGame) room.getGame(BattleBanzaiGame.class);

        if (game != null && game.getState() != GameState.IDLE) {
            this.setExtraData("1");
        }
    }
}
