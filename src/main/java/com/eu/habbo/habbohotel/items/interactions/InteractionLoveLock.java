package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.lovelock.FriendFurniStartConfirmationMessageComposer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public class InteractionLoveLock extends RoomItem {
    public int userOneId;
    public int userTwoId;

    public InteractionLoveLock(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionLoveLock(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(2 + (this.isLimited() ? 256 : 0));
        serverMessage.appendInt(6);

        String[] data = this.getExtraData().split("\t");

        if (data.length == 6) {
            serverMessage.appendString("1");
            serverMessage.appendString(data[1]);
            serverMessage.appendString(data[2]);
            serverMessage.appendString(data[3]);
            serverMessage.appendString(data[4]);
            serverMessage.appendString(data[5]);
        } else {
            serverMessage.appendString("0");
            serverMessage.appendString("");
            serverMessage.appendString("");
            serverMessage.appendString("");
            serverMessage.appendString("");
            serverMessage.appendString("");
        }

        super.serializeExtradata(serverMessage);
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return false;
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {

    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) {
        if (this.getExtraData().contains("\t"))
            return;

        if (client == null)
            return;

        if (RoomLayout.tilesAdjecent(client.getHabbo().getRoomUnit().getCurrentPosition(), room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()))) {
            if (this.userOneId == 0) {
                this.userOneId = client.getHabbo().getHabboInfo().getId();
                client.sendResponse(new FriendFurniStartConfirmationMessageComposer(this));
            } else {
                if (this.userOneId != client.getHabbo().getHabboInfo().getId()) {
                    Habbo habbo = room.getRoomUnitManager().getRoomHabboById(this.userOneId);

                    if (habbo != null) {
                        this.userTwoId = client.getHabbo().getHabboInfo().getId();
                        client.sendResponse(new FriendFurniStartConfirmationMessageComposer(this));
                    }
                }
            }
        }
    }

    public boolean lock(Habbo userOne, Habbo userTwo, Room room) {
        RoomTile tile = room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY());
        if (RoomLayout.tilesAdjecent(userOne.getRoomUnit().getCurrentPosition(), tile) && RoomLayout.tilesAdjecent(userTwo.getRoomUnit().getCurrentPosition(), tile)) {
            String data = "1";
            data += "\t";
            data += userOne.getHabboInfo().getUsername();
            data += "\t";
            data += userTwo.getHabboInfo().getUsername();
            data += "\t";
            data += userOne.getHabboInfo().getLook();
            data += "\t";
            data += userTwo.getHabboInfo().getLook();
            data += "\t";
            data += Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-" + Calendar.getInstance().get(Calendar.YEAR);

            this.setExtraData(data);
            this.setSqlUpdateNeeded(true);
            Emulator.getThreading().run(this);
            room.updateItem(this);

            return true;
        }

        return false;
    }
}
