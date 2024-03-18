package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredTriggerRoomWalkOnFurni extends InteractionWiredTrigger {
    public WiredTriggerRoomWalkOnFurni(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerRoomWalkOnFurni(int id, HabboInfo ownerInfo, Item item, String extraData, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extraData, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if(stuff.length == 0) {
            return false;
        }

        if (stuff[0] instanceof RoomItem) {
            return this.getWiredSettings().getItems(room).contains(stuff[0]);
        }

        return false;
    }

    @Override
    public WiredTriggerType getType() {
        return WiredTriggerType.WALKS_ON_FURNI;
    }
}
