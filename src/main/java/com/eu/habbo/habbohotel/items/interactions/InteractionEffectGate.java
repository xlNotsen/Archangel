package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.interfaces.ConditionalGate;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.rooms.entities.units.types.RoomHabbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.threading.runnables.CloseGate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InteractionEffectGate extends InteractionDefault implements ConditionalGate {

    // List of Habboween costumes according to http://www.habboxwiki.com/Costumes
    private static final List<Integer> defaultAllowedEnables = new ArrayList<>(Arrays.asList(
            114, // Strong Arms
            115, // Ringmaster Costume
            116, // Fly Head
            117, // Executioner Hood
            118, // Evil Clown Paint
            135 // Marionette
    ));

    public InteractionEffectGate(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.setExtraData("0");
    }

    public InteractionEffectGate(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
        this.setExtraData("0");
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        if (roomUnit == null || room == null || !(roomUnit instanceof RoomHabbo roomHabbo))
            return false;

        String customparams = this.getBaseItem().getCustomParams().trim();

        if (!customparams.isEmpty()) {
            return Arrays.asList(customparams.split(";")).contains(String.valueOf(roomHabbo.getEffectId()));
        }

        return defaultAllowedEnables.contains(roomHabbo.getEffectId());
    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOn(roomUnit, room, objects);

        if (this.canWalkOn(roomUnit, room, objects)) {
            this.setExtraData("1");
            room.updateItemState(this);
        }
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        super.onClick(client, room, objects);
    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOff(roomUnit, room, objects);

        Emulator.getThreading().run(new CloseGate(this, room), 1000);
    }

    @Override
    public void onRejected(RoomUnit roomUnit, Room room, Object[] objects) {

    }
}
