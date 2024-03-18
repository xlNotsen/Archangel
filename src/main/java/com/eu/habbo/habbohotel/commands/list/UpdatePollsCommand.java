package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;

public class UpdatePollsCommand extends Command {
    public UpdatePollsCommand() {
        super("cmd_update_polls");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        Emulator.getGameEnvironment().getPollManager().loadPolls();
        gameClient.getHabbo().whisper(getTextsValue("commands.succes.cmd_update_polls"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}