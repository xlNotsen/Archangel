package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public class InteractionPressurePlate extends InteractionDefault {
    public InteractionPressurePlate(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.setExtraData("0");
    }

    public InteractionPressurePlate(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
        this.setExtraData("0");
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
    public void onClick(GameClient client, Room room, Object[] objects) {

    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {

    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOn(roomUnit, room, objects);

        Emulator.getThreading().run(() -> updateState(room), 100);
    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOff(roomUnit, room, objects);

        Emulator.getThreading().run(() -> updateState(room), 100);
    }

    @Override
    public void onMove(Room room, RoomTile oldLocation, RoomTile newLocation) {
        super.onMove(room, oldLocation, newLocation);

        updateState(room);
    }

    @Override
    public void onPickUp(Room room) {
        this.setExtraData("0");
    }

    public void updateState(Room room) {
        boolean occupied = false;

        if (room == null || room.getLayout() == null || this.getBaseItem() == null) return;

        RoomTile tileAtItem = room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY());

        if (tileAtItem == null) return;

        THashSet<RoomTile> tiles = room.getLayout().getTilesAt(tileAtItem, this.getBaseItem().getWidth(), this.getBaseItem().getLength(), this.getRotation());

        if (tiles == null) return;

        for (RoomTile tile : tiles) {
            HashSet<RoomUnit> tileHasHabboOrBot = (HashSet<RoomUnit>) room.getRoomUnitManager().getAvatarsAt(tile);

            if (tileHasHabboOrBot.isEmpty() && this.requiresAllTilesOccupied()) {
                occupied = false;
                break;
            }

            if (!tileHasHabboOrBot.isEmpty()) {
                occupied = true;
            }
        }

        this.setExtraData(occupied ? "1" : "0");
        room.updateItemState(this);
    }

    @Override
    public boolean allowWiredResetState() {
        return true;
    }

    public boolean requiresAllTilesOccupied() {
        return false;
    }

}
