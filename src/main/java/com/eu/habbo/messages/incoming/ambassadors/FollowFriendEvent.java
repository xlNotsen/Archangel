package com.eu.habbo.messages.incoming.ambassadors;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.RoomForwardMessageComposer;

public class FollowFriendEvent extends MessageHandler {
    @Override
    public void handle() {
        if (this.client.getHabbo().hasPermissionRight(Permission.ACC_AMBASSADOR)) {
            String username = this.packet.readString();

            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(username);

            if (habbo != null) {
                if (habbo.getRoomUnit().getRoom() != null) {
                    this.client.sendResponse(new RoomForwardMessageComposer(habbo.getRoomUnit().getRoom().getRoomInfo().getId()));
                }
            }
        }
    }
}
