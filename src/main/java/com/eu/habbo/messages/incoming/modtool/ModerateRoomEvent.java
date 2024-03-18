package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

public class ModerateRoomEvent extends MessageHandler {
    @Override
    public void handle() {
        if (this.client.getHabbo().hasPermissionRight(Permission.ACC_SUPPORTTOOL)) {
            Room room = Emulator.getGameEnvironment().getRoomManager().getActiveRoomById(this.packet.readInt());

            if (room != null) {
                final boolean lockDoor = this.packet.readInt() == 1;
                final boolean changeTitle = this.packet.readInt() == 1;
                final boolean kickUsers = this.packet.readInt() == 1;

                Emulator.getGameEnvironment().getModToolManager().roomAction(room, this.client.getHabbo(), kickUsers, lockDoor, changeTitle);
            }
        } else {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.roomsettings").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
        }
    }
}
