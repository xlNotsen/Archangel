package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.rooms.entities.units.types.RoomHabbo;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboInfo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionTrap extends InteractionDefault {
    public InteractionTrap(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionTrap(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        if (this.getExtraData().equals("0") || roomUnit == null || room.getRoomUnitManager().getHabboByRoomUnit(roomUnit) == null) return;

        Habbo habbo = room.getRoomUnitManager().getHabboByRoomUnit(roomUnit);
        int effect = habbo.getClient().getHabbo().getRoomUnit().getEffectId();
        roomUnit.stopWalking();
        super.onWalkOn(roomUnit, room, objects);
        int delay = Emulator.getConfig().getInt("hotel.item.trap." + this.getBaseItem().getName());
        if (delay == 0) {
            Emulator.getConfig().register("hotel.item.trap." + this.getBaseItem().getName(), "3000");
            delay = 3000;
        }

        if (this.getBaseItem().getEffectF() > 0 || this.getBaseItem().getEffectM() > 0) {
            if (roomUnit instanceof RoomHabbo roomHabbo) {

                if (habbo.getHabboInfo().getGender().equals(HabboGender.M) && this.getBaseItem().getEffectM() > 0 && roomHabbo.getEffectId() != this.getBaseItem().getEffectM()) {
                    roomHabbo.giveEffect(this.getBaseItem().getEffectM(), -1);
                    return;
                }

                if (habbo.getHabboInfo().getGender().equals(HabboGender.F) && this.getBaseItem().getEffectF() > 0 && roomHabbo.getEffectId() != this.getBaseItem().getEffectF()) {
                    roomHabbo.giveEffect(this.getBaseItem().getEffectF(), -1);
                    return;
                }


                roomHabbo.setCanWalk(false);
                Emulator.getThreading().run(() -> {
                    roomHabbo.giveEffect(0, -1);
                    roomUnit.setCanWalk(true);
                    roomHabbo.giveEffect(effect, -1);
                }, delay);
            }
        }

    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) {
    }

}
