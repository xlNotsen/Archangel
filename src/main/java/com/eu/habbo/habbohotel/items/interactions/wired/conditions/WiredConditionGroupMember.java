package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.wired.WiredConditionType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionGroupMember extends InteractionWiredCondition {
    public WiredConditionGroupMember(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionGroupMember(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (room.getRoomInfo().getGuild() == null) {
            return false;
        }

        Habbo habbo = room.getRoomUnitManager().getHabboByRoomUnit(roomUnit);

        return habbo != null && habbo.getHabboStats().hasGuild(room.getRoomInfo().getGuild().getId());
    }

    @Override
    public WiredConditionType getType() {
        return WiredConditionType.ACTOR_IN_GROUP;
    }
}
