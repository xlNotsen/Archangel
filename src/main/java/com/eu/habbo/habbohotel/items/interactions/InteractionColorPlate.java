package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class InteractionColorPlate extends InteractionDefault {

    public InteractionColorPlate(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionColorPlate(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOn(roomUnit, room, objects);

        this.change(room, 1);
    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOff(roomUnit, room, objects);

        this.change(room, -1);
    }

    private void change(Room room, int amount) {
        int state = 0;

        if (this.getExtraData() == null || this.getExtraData().isEmpty()) {
            this.setExtraData("0");
        }

        try {
            state = Integer.parseInt(this.getExtraData());
        } catch (Exception e) {
            log.error("Caught exception", e);
        }

        state += amount;
        if (state > this.getBaseItem().getStateCount()) {
            state = this.getBaseItem().getStateCount();
        }

        if (state < 0) {
            state = 0;
        }

        this.setExtraData(state + "");
        this.setSqlUpdateNeeded(true);
        room.updateItemState(this);
    }
}