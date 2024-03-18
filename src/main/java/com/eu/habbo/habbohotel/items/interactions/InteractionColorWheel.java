package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.ItemUpdateMessageComposer;
import com.eu.habbo.threading.runnables.RandomDiceNumber;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionColorWheel extends RoomItem {
    private Runnable rollTaks;

    public InteractionColorWheel(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionColorWheel(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
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
        return false;
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        super.onClick(client, room, objects);

        if (!room.getRoomRightsManager().hasRights(client.getHabbo()))
            return;

        if (this.rollTaks == null && !this.getExtraData().equalsIgnoreCase("-1")) {
            this.setExtraData("-1");
            room.sendComposer(new ItemUpdateMessageComposer(this).compose());
            Emulator.getThreading().run(this);
            Emulator.getThreading().run(new RandomDiceNumber(this, room, this.getBaseItem().getStateCount()), 3000);
        }
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {

    }

    @Override
    public void onWalkOn(RoomUnit client, Room room, Object[] objects) {

    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOff(roomUnit, room, objects);
    }

    public void clearRunnable() {
        this.rollTaks = null;
    }
}
