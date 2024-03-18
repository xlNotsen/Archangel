package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.GetGuestRoomResultComposer;

public class GetGuestRoomEvent extends MessageHandler {
    @Override
    public void handle() {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.packet.readInt());

        int something = this.packet.readInt();
        int something2 = this.packet.readInt();

        if (room != null) {
            boolean unknown = something != 0 || something2 != 1;

            //this.client.getHabbo().getRoomUnit().getRoom() != room
            this.client.sendResponse(new GetGuestRoomResultComposer(room, this.client.getHabbo(), true, unknown));
        }
    }
}
