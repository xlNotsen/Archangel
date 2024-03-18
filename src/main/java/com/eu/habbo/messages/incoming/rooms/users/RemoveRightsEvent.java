package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

public class RemoveRightsEvent extends MessageHandler {
    @Override
    public void handle() {
        int amount = this.packet.readInt();

        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room == null)
            return;

        if (room.getRoomInfo().getOwnerInfo().getId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermissionRight(Permission.ACC_ANYROOMOWNER)) {
            for (int i = 0; i < amount; i++) {
                int userId = this.packet.readInt();

                room.getRoomRightsManager().removeRights(userId);
            }
        }
    }
}
