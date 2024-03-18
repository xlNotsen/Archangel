package com.eu.habbo.habbohotel.commands.list.badge;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.messages.outgoing.inventory.BadgesComposer;
import com.eu.habbo.messages.outgoing.users.UserBadgesComposer;

public class TakeBadgeCommand extends BaseBadgeCommand {
    public TakeBadgeCommand() {
        super("cmd_take_badge");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params.length == 2) {
            gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_take_badge.forgot_badge"), RoomChatMessageBubbles.ALERT);
            return true;
        } else if (params.length == 1) {
            gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_take_badge.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }

        if (params.length == 3) {
            String username = params[1];
            String badge = params[2];

            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(username);

            if (habbo != null) {
                HabboBadge b = habbo.getInventory().getBadgesComponent().removeBadge(badge);

                if (b == null) {
                    gameClient.getHabbo().whisper(replaceUsername(getTextsValue("commands.error.cmd_take_badge.no_badge"), username).replace("%badge%", badge), RoomChatMessageBubbles.ALERT);
                    return true;
                }

                habbo.getClient().sendResponse(new BadgesComposer(habbo));
                if (habbo.getRoomUnit().getRoom() != null) {
                    habbo.getRoomUnit().getRoom().sendComposer(new UserBadgesComposer(habbo.getInventory().getBadgesComponent().getWearingBadges(), habbo.getHabboInfo().getId()).compose());
                }
            }

            int userId = 0;

            if (habbo != null)
                userId = habbo.getHabboInfo().getId();
            else {
                HabboInfo habboInfo = Emulator.getGameEnvironment().getHabboManager().getOfflineHabboInfo(username);
                if (habboInfo != null)
                    userId = habboInfo.getId();
            }

            if (userId > 0) {
                gameClient.getHabbo().whisper(getTextsValue("commands.succes.cmd_take_badge"), RoomChatMessageBubbles.ALERT);

                BadgesComponent.deleteBadge(userId, badge);
            }
        }

        return true;
    }
}