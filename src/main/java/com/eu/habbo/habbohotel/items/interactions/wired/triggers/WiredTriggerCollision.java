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

public class WiredTriggerCollision extends InteractionWiredTrigger {
    private static final WiredTriggerType type = WiredTriggerType.COLLISION;

    public WiredTriggerCollision(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerCollision(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        return stuff.length > 0 && stuff[0] instanceof RoomItem;
    }

    @Override
    public WiredTriggerType getType() {
        return WiredTriggerType.COLLISION;
    }
}
