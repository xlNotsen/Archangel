package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;

public class StandCommand extends Command {
    public StandCommand() {
        super("cmd_stand");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (!gameClient.getHabbo().getRoomUnit().isRiding()) {
            gameClient.getHabbo().getRoomUnit().makeStand();
        }

        return true;
    }
}
