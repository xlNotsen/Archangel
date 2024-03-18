package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.chat.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatType;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserTalkEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShoutEvent extends MessageHandler {

    @Override
    public void handle() {
        if (this.client.getHabbo().getRoomUnit().getRoom() == null)
            return;

        if (!this.client.getHabbo().getHabboStats().allowTalk())
            return;


        RoomChatMessage message = new RoomChatMessage(this);

        if (message.getMessage().length() <= RoomChatMessage.MAXIMUM_LENGTH) {
            if (Emulator.getPluginManager().fireEvent(new UserTalkEvent(this.client.getHabbo(), message, RoomChatType.SHOUT)).isCancelled()) {
                return;
            }

            this.client.getHabbo().getRoomUnit().getRoom().getRoomChatManager().talk(this.client.getHabbo(), message, RoomChatType.SHOUT);

            if (!message.isCommand) {
                if (RoomChatMessage.SAVE_ROOM_CHATS) {
                    Emulator.getThreading().run(message);
                }
            }
        } else {
            String reportMessage = Emulator.getTexts().getValue("scripter.warning.chat.length").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%length%", message.getMessage().length() + "");
            ScripterManager.scripterDetected(this.client, reportMessage);
            log.info(reportMessage);
        }
    }
}
