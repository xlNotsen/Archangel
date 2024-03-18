package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredEffectAlert extends WiredEffectWhisper {
    public WiredEffectAlert(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectAlert(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        Habbo habbo = room.getRoomUnitManager().getHabboByRoomUnit(roomUnit);

        if (habbo != null) {
            habbo.alert(this.getWiredSettings().getStringParam()
                    .replace("%online%", Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + "")
                    .replace("%username%", habbo.getHabboInfo().getUsername())
                    .replace("%roomsloaded%", Emulator.getGameEnvironment().getRoomManager().loadedRoomsCount() + ""));
            return true;
        }

        return false;
    }
}