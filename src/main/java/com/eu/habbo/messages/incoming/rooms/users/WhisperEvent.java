package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.chat.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatType;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserTalkEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WhisperEvent extends MessageHandler {

    @Override
    public void handle() {
        if (this.client.getHabbo().getRoomUnit().getRoom() == null)
            return;

        RoomChatMessage chatMessage = new RoomChatMessage(this);

        if (chatMessage.getMessage().length() <= RoomChatMessage.MAXIMUM_LENGTH) {
            if (!this.client.getHabbo().getHabboStats().allowTalk() || chatMessage.getTargetHabbo() == null)
                return;

            if (Emulator.getPluginManager().fireEvent(new UserTalkEvent(this.client.getHabbo(), chatMessage, RoomChatType.WHISPER)).isCancelled()) {
                return;
            }

            this.client.getHabbo().getRoomUnit().getRoom().getRoomChatManager().talk(this.client.getHabbo(), chatMessage, RoomChatType.WHISPER, true);

            if (RoomChatMessage.SAVE_ROOM_CHATS) {
                Emulator.getThreading().run(chatMessage);
            }
        } else {
            String reportMessage = Emulator.getTexts().getValue("scripter.warning.chat.length").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%length%", chatMessage.getMessage().length() + "");
            ScripterManager.scripterDetected(this.client, reportMessage);
            log.info(reportMessage);
        }
    }
}
