package com.eu.habbo.roleplay.commands.corporation;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;

public class HireCommand extends Command {
    public HireCommand() {
        super("cmd_hire");
    }
    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        String targetedUsername = params[1];

        if (targetedUsername == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.user_not_found"));
            return true;
        }

        Habbo targetedHabbo = gameClient.getHabbo().getRoomUnit().getRoom().getRoomUnitManager().getRoomHabboByUsername(targetedUsername);

        if (targetedHabbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.user_not_found"));
            return true;
        }

        if (!gameClient.getHabbo().getHabboRoleplayStats().getCorporationPosition().isCanHire()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_hire_not_allowed"));
            return true;
        }

        if (gameClient.getHabbo().getHabboRoleplayStats().getCorporationID() == targetedHabbo.getHabboRoleplayStats().getCorporationID()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_hire_user_has_same_employer"));
            return true;
        }

        return true;
    }
}