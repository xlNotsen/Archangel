package com.eu.habbo.roleplay.commands.corp;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.roleplay.corp.CorpPosition;

public class CorpPromoteCommand extends Command {
    public CorpPromoteCommand() {
        super("cmd_corp_promote");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params == null) {
            return true;
        }

        String targetedUsername = params[1];

        if (targetedUsername == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.user_not_found"));
            return true;
        }

        Habbo targetedHabbo = gameClient.getHabbo().getRoomUnit().getRoom().getRoomUnitManager().getRoomHabboByUsername(targetedUsername);

        if (targetedHabbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.user_not_found").replace("%username%", targetedUsername));
            return true;
        }

        if (targetedHabbo == gameClient.getHabbo()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_promote_user_is_self"));
            return true;
        }

        if (gameClient.getHabbo().getHabboRoleplayStats().getCorporation().getGuild().getId() != targetedHabbo.getHabboRoleplayStats().getCorporation().getGuild().getId()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_demote_not_allowed"));
            return true;
        }

        if (!gameClient.getHabbo().getHabboRoleplayStats().getCorporationPosition().isCanPromote()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_promote_not_allowed"));
            return true;
        }

        if (gameClient.getHabbo().getHabboRoleplayStats().getCorporationPosition().getOrderID() <= targetedHabbo.getHabboRoleplayStats().getCorporationPosition().getOrderID()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_promote_not_allowed"));
            return true;
        }

        CorpPosition newPosition = targetedHabbo.getHabboRoleplayStats().getCorporation().getPositionByOrderID(targetedHabbo.getHabboRoleplayStats().getCorporationPosition().getOrderID() + 1);

        if (newPosition == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_promote_not_allowed_too_high"));
            return true;
        }

        if (gameClient.getHabbo().getHabboRoleplayStats().getCorporationPosition().getOrderID() < newPosition.getOrderID()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_promote_not_allowed"));
            return true;
        }

        targetedHabbo.getHabboRoleplayStats().setCorporation(targetedHabbo.getHabboRoleplayStats().getCorporation().getGuild().getId(), newPosition.getId());

        gameClient.getHabbo().shout(Emulator.getTexts().getValue("commands.roleplay.cmd_promote_success")
                .replace("%username%", targetedHabbo.getHabboInfo().getUsername())
                .replace("%corp%", targetedHabbo.getHabboRoleplayStats().getCorporation().getGuild().getName())
                .replace("%position%", targetedHabbo.getHabboRoleplayStats().getCorporationPosition().getName()));

        targetedHabbo.shout(Emulator.getTexts().getValue("generic.roleplay.started_new_job").
                replace("%corp%", targetedHabbo.getHabboRoleplayStats().getCorporation().getGuild().getName())
                .replace("%position%", newPosition.getName()));

        return true;
    }
}