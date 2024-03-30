package com.eu.habbo.roleplay.commands.gang;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.roleplay.guilds.Guild;

public class GangInviteDeclineCommand extends Command {
    public GangInviteDeclineCommand() {
        super("cmd_gang_invite_decline");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params == null) {
            return true;
        }

        String gangName = params[1];

        if (gangName == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.gang_not_found"));
            return true;
        }

        Guild targetedGang = Emulator.getGameEnvironment().getGuildManager().getGuild(gangName);

        if (targetedGang == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.gang_not_found"));
            return true;
        }

        if (targetedGang.getInvitedUser(gameClient.getHabbo()) == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_gang_invite_missing"));
            return true;
        }

        targetedGang.removeInvitedUser(gameClient.getHabbo());

        gameClient.getHabbo().shout(Emulator.getTexts().getValue("commands.roleplay.cmd_gang_invite_rejected").replace("%gang%", targetedGang.getName()));

        return true;
    }
}