package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.UserChatlogComposer;

public class GetUserChatlogEvent extends MessageHandler {
    @Override
    public void handle() {
        if (this.client.getHabbo().hasPermissionRight(Permission.ACC_SUPPORTTOOL)) {
            int userId = this.packet.readInt();
            String username = Emulator.getGameEnvironment().getHabboManager().getOfflineHabboInfo(userId).getUsername();

            this.client.sendResponse(new UserChatlogComposer(Emulator.getGameEnvironment().getModToolManager().getUserRoomVisitsAndChatlogs(userId), userId, username));
        } else {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.chatlog").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
        }
    }
}
