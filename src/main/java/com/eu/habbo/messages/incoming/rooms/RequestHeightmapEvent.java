package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

public class RequestHeightmapEvent extends MessageHandler {
    @Override
    public void handle() {
        if (this.client.getHabbo().getRoomUnit().isLoadingRoom()) {
            Room room = this.client.getHabbo().getRoomUnit().getLoadingRoom();

            if (room != null) {
                Emulator.getGameEnvironment().getRoomManager().enterRoom(this.client.getHabbo(), room);
            }
        }
    }
}
