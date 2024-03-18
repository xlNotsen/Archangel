package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.CarryObjectMessageComposer;

public class RoomItemCommand extends Command {
    public RoomItemCommand() {
        super("cmd_room_item");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        int itemId = 0;

        if (params.length >= 2) {
            try {
                itemId = Integer.parseInt(params[1]);

                if (itemId < 0) {
                    gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_roomitem.positive"), RoomChatMessageBubbles.ALERT);
                    return true;
                }
            } catch (Exception e) {
                gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_roomitem.no_item"), RoomChatMessageBubbles.ALERT);
                return true;
            }
        }

        for (Habbo habbo : gameClient.getHabbo().getRoomUnit().getRoom().getRoomUnitManager().getCurrentHabbos().values()) {
            habbo.getRoomUnit().setHandItem(itemId);
            habbo.getRoomUnit().getRoom().sendComposer(new CarryObjectMessageComposer(habbo.getRoomUnit()).compose());
        }

        if (itemId > 0) {
            gameClient.getHabbo().whisper(getTextsValue("commands.succes.cmd_roomitem.given").replace("%item%", itemId + ""), RoomChatMessageBubbles.ALERT);
        } else {
            gameClient.getHabbo().whisper(getTextsValue("commands.succes.cmd_roomitem.removed"), RoomChatMessageBubbles.ALERT);
        }
        return true;
    }
}
