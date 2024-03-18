package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.clothingvalidation.ClothingValidationManager;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.users.UserChangeMessageComposer;
import com.eu.habbo.messages.outgoing.users.UserObjectComposer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionMannequin extends RoomItem {
    public InteractionMannequin(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionMannequin(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public int getMaximumRotations() {
        return 8;
    }

    @Override
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(1 + (this.isLimited() ? 256 : 0));
        serverMessage.appendInt(3);
        if (this.getExtraData().split(":").length >= 2) {
            String[] data = this.getExtraData().split(":");
            serverMessage.appendString("GENDER");
            serverMessage.appendString(data[0].toLowerCase());
            serverMessage.appendString("FIGURE");
            serverMessage.appendString(data[1]);
            serverMessage.appendString("OUTFIT_NAME");
            serverMessage.appendString((data.length >= 3 ? data[2] : ""));
        } else {
            serverMessage.appendString("GENDER");
            serverMessage.appendString("m");
            serverMessage.appendString("FIGURE");
            serverMessage.appendString("");
            serverMessage.appendString("OUTFIT_NAME");
            serverMessage.appendString("My Look");
            this.setExtraData("m: :My look");
            this.setSqlUpdateNeeded(true);
            Emulator.getThreading().run(this);
        }
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
    public void onClick(GameClient client, Room room, Object[] objects) {
        String[] data = this.getExtraData().split(":");

        if(data.length < 2)
            return;

        String gender = data[0];
        String figure = data[1];

        if (gender.isEmpty() || figure.isEmpty() || (!gender.equalsIgnoreCase("m") && !gender.equalsIgnoreCase("f")) || !client.getHabbo().getHabboInfo().getGender().name().equalsIgnoreCase(gender))
            return;

        StringBuilder newFigure = new StringBuilder();

        for (String playerFigurePart : client.getHabbo().getHabboInfo().getLook().split("\\.")) {
            if (!playerFigurePart.startsWith("ch") && !playerFigurePart.startsWith("lg"))
                newFigure.append(playerFigurePart).append(".");
        }

        String newFigureParts = figure;

        for (String newFigurePart : newFigureParts.split("\\.")) {
            if (newFigurePart.startsWith("hd"))
                newFigureParts = newFigureParts.replace(newFigurePart, "");
        }

        if (newFigureParts.equals("")) return;

        String newLook = newFigure + newFigureParts;

        if (newLook.length() > 512)
            return;

        client.getHabbo().getHabboInfo().setLook(ClothingValidationManager.VALIDATE_ON_MANNEQUIN ? ClothingValidationManager.validateLook(client.getHabbo(), newLook, client.getHabbo().getHabboInfo().getGender().name()) : newLook);
        room.sendComposer(new UserChangeMessageComposer(client.getHabbo()).compose());
        client.sendResponse(new UserObjectComposer(client.getHabbo()));
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {

    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {

    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) {

    }
}
