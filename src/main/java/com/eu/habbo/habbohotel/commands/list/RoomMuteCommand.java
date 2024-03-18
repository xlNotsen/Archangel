package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;

public class RoomMuteCommand extends Command {
    public RoomMuteCommand() {
        super("cmd_room_mute");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        Room room = gameClient.getHabbo().getRoomUnit().getRoom();

        if (room != null) {
            room.getRoomChatManager().setMuted(!room.getRoomChatManager().isMuted());
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_roommute." + (room.getRoomChatManager().isMuted() ? "muted" : "unmuted")), RoomChatMessageBubbles.ALERT);
        }

        return true;
    }
}