package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;

public class UpdateConfigCommand extends Command {
    public UpdateConfigCommand() {
        super("cmd_update_config");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        Emulator.getConfig().reload();

        gameClient.getHabbo().whisper(getTextsValue("commands.succes.cmd_update_config"), RoomChatMessageBubbles.ALERT);

        return true;
    }
}
