package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.chat.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.WhisperMessageComposer;
import com.eu.habbo.plugin.events.users.UserKickEvent;

public class RoomUserKickEvent extends MessageHandler {
    @Override
    public void handle() {
        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room == null)
            return;

        int userId = this.packet.readInt();

        Habbo target = room.getRoomUnitManager().getRoomHabboById(userId);

        if (target == null)
            return;

        if (target.hasPermissionRight(Permission.ACC_UNKICKABLE)) {
            this.client.sendResponse(new WhisperMessageComposer(new RoomChatMessage(Emulator.getTexts().getValue("commands.error.cmd_kick.unkickable").replace("%username%", target.getHabboInfo().getUsername()), this.client.getHabbo(), this.client.getHabbo(), RoomChatMessageBubbles.ALERT)));
            return;
        }

        if (room.getRoomInfo().isRoomOwner(target)) {
            return;
        }

        UserKickEvent event = new UserKickEvent(this.client.getHabbo(), target);
        Emulator.getPluginManager().fireEvent(event);

        if (event.isCancelled())
            return;

        if (room.getRoomRightsManager().hasRights(this.client.getHabbo()) || this.client.getHabbo().hasPermissionRight(Permission.ACC_ANYROOMOWNER) || this.client.getHabbo().hasPermissionRight(Permission.ACC_AMBASSADOR)) {
            if (target.hasPermissionRight(Permission.ACC_UNKICKABLE)) return;

            room.kickHabbo(target, true);
            AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModKickSeen"));
        }
    }
}
