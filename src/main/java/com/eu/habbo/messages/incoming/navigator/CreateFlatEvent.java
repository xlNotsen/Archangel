package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomCategory;
import com.eu.habbo.habbohotel.rooms.RoomManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.navigator.CanCreateRoomComposer;
import com.eu.habbo.messages.outgoing.navigator.FlatCreatedComposer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateFlatEvent extends MessageHandler {

    @Override
    public void handle() {
        String name = this.packet.readString();
        String description = this.packet.readString();
        String modelName = this.packet.readString();
        int categoryId = this.packet.readInt();
        int maxUsers = this.packet.readInt();
        int tradeType = this.packet.readInt();

        if (!Emulator.getGameEnvironment().getRoomManager().layoutExists(modelName)) {
            log.error("[SCRIPTER] Incorrect layout name \"" + modelName + "\". " + this.client.getHabbo().getHabboInfo().getUsername());
            return;
        }

        RoomCategory category = Emulator.getGameEnvironment().getRoomManager().getCategory(categoryId);

        if (category == null || category.getMinRank() > this.client.getHabbo().getHabboInfo().getPermissionGroup().getId()) {
            log.error("[SCRIPTER] Incorrect rank or non existing category ID: \"" + categoryId + "\"." + this.client.getHabbo().getHabboInfo().getUsername());
            return;
        }

        if (maxUsers > 250)
            return;

        if (tradeType > 2)
            return;

        if (name.trim().length() < 3 || name.length() > 25 || !Emulator.getGameEnvironment().getWordFilter().filter(name, this.client.getHabbo()).equals(name))
            return;

        if (description.length() > 128 || !Emulator.getGameEnvironment().getWordFilter().filter(description, this.client.getHabbo()).equals(description))
            return;

        int totalRoomsCount = Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(this.client.getHabbo()).size();
        int maxRoomsCount = this.client.getHabbo().getHabboStats().hasActiveClub() ? RoomManager.MAXIMUM_ROOMS_HC : RoomManager.MAXIMUM_ROOMS_USER;

        if (totalRoomsCount >= maxRoomsCount) {
            this.client.sendResponse(new CanCreateRoomComposer(totalRoomsCount, maxRoomsCount));
            return;
        }

        final Room room = Emulator.getGameEnvironment().getRoomManager().createRoomForHabbo(this.client.getHabbo(), name, description, modelName, maxUsers, categoryId, tradeType);

        if (room != null) {
            this.client.sendResponse(new FlatCreatedComposer(room));
        }
    }
}
