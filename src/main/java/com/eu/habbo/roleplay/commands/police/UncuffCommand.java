package com.eu.habbo.roleplay.commands.police;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.roleplay.RoleplayHelper;
import com.eu.habbo.roleplay.corp.Corp;
import com.eu.habbo.roleplay.facility.FacilityPrisonManager;

public class UncuffCommand extends Command {
    public UncuffCommand() {
        super("cmd_police_uncuff");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        Habbo targetedHabbo = RoleplayHelper.getInstance().getTarget(gameClient, params);

        if (targetedHabbo == null) {
            return true;
        }

        Corp corp = gameClient.getHabbo().getHabboRoleplayStats().getCorp();

        if (corp == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.roleplay.unemployed"));
            return true;
        }

        if (!corp.getTags().contains(FacilityPrisonManager.PRISON_TAG)) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.roleplay.police_only"));
            return true;
        }

        if (!gameClient.getHabbo().getHabboRoleplayStats().isWorking()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.roleplay.must_be_working"));
            return true;
        }

        if (!targetedHabbo.getHabboRoleplayStats().isCuffed()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_uncuff_user_isnt_cuffed").replace(":username", targetedHabbo.getHabboInfo().getUsername()));
            return true;
        }

        int distanceX = targetedHabbo.getRoomUnit().getCurrentPosition().getX() - gameClient.getHabbo().getRoomUnit().getCurrentPosition().getX();
        int distanceY = targetedHabbo.getRoomUnit().getCurrentPosition().getY() - gameClient.getHabbo().getRoomUnit().getCurrentPosition().getY();

        int rangeInTiles = 1;

        boolean isTargetWithinRange = Math.abs(distanceX) <= rangeInTiles && Math.abs(distanceY) <= rangeInTiles;

        if (!isTargetWithinRange) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("roleplay.generic_target_too_far").replace(":username", targetedHabbo.getHabboInfo().getUsername()));
            return true;
        }

        targetedHabbo.getHabboRoleplayStats().setIsCuffed(false);

        gameClient.getHabbo().shout(Emulator.getTexts().getValue("commands.roleplay_cmd_uncuff_success").replace(":username", targetedHabbo.getHabboInfo().getUsername()));

        return true;
    }
}