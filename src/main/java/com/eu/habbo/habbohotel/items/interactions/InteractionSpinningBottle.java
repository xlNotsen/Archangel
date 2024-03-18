package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.plugin.events.furniture.FurnitureDiceRolledEvent;
import com.eu.habbo.threading.runnables.RandomSpinningBottleNumber;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionSpinningBottle extends RoomItem {
    public InteractionSpinningBottle(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    public InteractionSpinningBottle(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
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

        if (client != null) {
            if (RoomLayout.tilesAdjecent(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), client.getHabbo().getRoomUnit().getCurrentPosition())) {
                if (!this.getExtraData().equalsIgnoreCase("-1")) {
                    FurnitureDiceRolledEvent event = Emulator.getPluginManager().fireEvent(new FurnitureDiceRolledEvent(this, client.getHabbo(), -1));

                    if (event.isCancelled())
                        return;

                    this.setExtraData("-1");
                    room.updateItemState(this);
                    Emulator.getThreading().run(this);

                    if (event.getResult() > 0) {
                        Emulator.getThreading().run(new RandomSpinningBottleNumber(room, this, event.getResult()), 1500);
                    } else {
                        Emulator.getThreading().run(new RandomSpinningBottleNumber(this, room, this.getBaseItem().getStateCount()), 1500);
                    }
                }
            }
        }
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {

    }

    @Override
    public void onPickUp(Room room) {
        this.setExtraData("0");
    }

    @Override
    public boolean allowWiredResetState() {
        return false;
    }

    @Override
    public boolean isUsable() {
        return true;
    }
}
