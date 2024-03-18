package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;

public class UpdateWordFilterCommand extends Command {
    public UpdateWordFilterCommand() {
        super("cmd_update_wordfilter");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        Emulator.getGameEnvironment().getWordFilter().reload();

        gameClient.getHabbo().whisper(getTextsValue("commands.succes.cmd_update_wordfilter"), RoomChatMessageBubbles.ALERT);

        return true;
    }
}
