package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

public class SoftKickCommand extends Command {
    public SoftKickCommand() {
        super("cmd_soft_kick");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params.length != 2) return true;
        final Habbo habbo = gameClient.getHabbo().getRoomUnit().getRoom().getRoomUnitManager().getRoomHabboByUsername(params[1]);

        if (habbo == null) {
            gameClient.getHabbo().whisper(replaceUser(getTextsValue("commands.keys.cmd_softkick_error"), params[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }

        if (habbo == gameClient.getHabbo()) {
            gameClient.getHabbo().whisper(getTextsValue("commands.keys.cmd_softkick_error_self"), RoomChatMessageBubbles.ALERT);
            return true;
        }

        final Room room = gameClient.getHabbo().getRoomUnit().getRoom();

        if (room != null && (!(habbo.hasPermissionRight(Permission.ACC_UNKICKABLE) || habbo.hasPermissionRight(Permission.ACC_SUPPORTTOOL) || room.getRoomInfo().isRoomOwner(habbo)))) {
            room.kickHabbo(habbo, false);
        }
        return true;
    }
}
