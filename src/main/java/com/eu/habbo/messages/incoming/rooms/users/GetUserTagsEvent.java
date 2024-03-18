package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.UserTagsMessageComposer;

public class GetUserTagsEvent extends MessageHandler {
    @Override
    public void handle() {
        int roomUnitId = this.packet.readInt();

        if (this.client.getHabbo().getRoomUnit().getRoom() != null) {
            Habbo habbo = this.client.getHabbo().getRoomUnit().getRoom().getRoomUnitManager().getHabboByVirtualId(roomUnitId);

            if (habbo != null) {
                this.client.sendResponse(new UserTagsMessageComposer(habbo));
            }
        }
    }
}
