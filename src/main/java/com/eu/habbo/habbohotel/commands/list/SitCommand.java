package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;

public class SitCommand extends Command {
    public SitCommand() {
        super("cmd_sit");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (!gameClient.getHabbo().getRoomUnit().isRiding()) //TODO Make this an event plugin which fires that can be cancelled
            gameClient.getHabbo().getRoomUnit().makeSit();
        return true;
    }
}
