package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.hotelview.CloseConnectionMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.FlatAccessDeniedMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.FlatAccessibleMessageComposer;

public class LetUserInEvent extends MessageHandler {
    @Override
    public void handle() {
        if (this.client.getHabbo().getRoomUnit().getRoom() != null && this.client.getHabbo().getRoomUnit().getRoom().getRoomRightsManager().hasRights(this.client.getHabbo())) {
            String username = this.packet.readString();
            boolean accepted = this.packet.readBoolean();

            Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(username);

            if (habbo != null) {
                if (habbo.getHabboInfo().getRoomQueueId() == this.client.getHabbo().getRoomUnit().getRoom().getRoomInfo().getId()) {
                    this.client.getHabbo().getRoomUnit().getRoom().removeFromQueue(habbo);

                    if (accepted) {
                        habbo.getClient().sendResponse(new FlatAccessibleMessageComposer(""));
                        Emulator.getGameEnvironment().getRoomManager().enterRoom(habbo, this.client.getHabbo().getRoomUnit().getRoom().getRoomInfo().getId(), "", true);
                    } else {
                        habbo.getClient().sendResponse(new FlatAccessDeniedMessageComposer(""));
                        habbo.getClient().sendResponse(new CloseConnectionMessageComposer());
                    }
                    habbo.getHabboInfo().setRoomQueueId(0);
                }
            }

        }
    }
}
