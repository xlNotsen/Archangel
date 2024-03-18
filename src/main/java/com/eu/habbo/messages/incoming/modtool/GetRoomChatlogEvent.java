package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.RoomChatlogComposer;

public class GetRoomChatlogEvent extends MessageHandler {
    @Override
    public void handle() {
        if (this.client.getHabbo().hasPermissionRight(Permission.ACC_SUPPORTTOOL)) {
            this.packet.readInt();
            Room room = Emulator.getGameEnvironment().getRoomManager().getActiveRoomById(this.packet.readInt());

            if (room != null)
                this.client.sendResponse(new RoomChatlogComposer(room, Emulator.getGameEnvironment().getModToolManager().getRoomChatlog(room.getRoomInfo().getId())));
        } else {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.chatlog").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
        }
    }
}
