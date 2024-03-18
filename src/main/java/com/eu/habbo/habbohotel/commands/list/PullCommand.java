package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.chat.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.messages.outgoing.rooms.users.ChatMessageComposer;

public class PullCommand extends Command {
    public PullCommand() {
        super("cmd_pull");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params.length != 2) return true;
        Habbo habbo = gameClient.getHabbo().getRoomUnit().getRoom().getRoomUnitManager().getRoomHabboByUsername(params[1]);

        if (habbo == null) {
            gameClient.getHabbo().whisper(replaceUser(getTextsValue("commands.error.cmd_pull.not_found"), params[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo == gameClient.getHabbo()) {
            gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_pull.pull_self"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        int distanceX = habbo.getRoomUnit().getCurrentPosition().getX() - gameClient.getHabbo().getRoomUnit().getCurrentPosition().getX();
        int distanceY = habbo.getRoomUnit().getCurrentPosition().getY() - gameClient.getHabbo().getRoomUnit().getCurrentPosition().getY();

        if (distanceX < -2 || distanceX > 2 || distanceY < -2 || distanceY > 2) {
            gameClient.getHabbo().whisper(replaceUser(getTextsValue("commands.error.cmd_pull.cant_reach"), params[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        RoomTile tile = gameClient.getHabbo().getRoomUnit().getRoom().getLayout().getTileInFront(gameClient.getHabbo().getRoomUnit().getCurrentPosition(), gameClient.getHabbo().getRoomUnit().getBodyRotation().getValue());

        if (tile != null && tile.isWalkable()) {
            if (gameClient.getHabbo().getRoomUnit().getRoom().getLayout().getDoorTile() == tile) {
                gameClient.getHabbo().whisper(replaceUsername(getTextsValue("commands.error.cmd_pull.invalid"), params[1]));
                return true;
            }
            habbo.getRoomUnit().walkTo(tile);
            gameClient.getHabbo().getRoomUnit().getRoom().sendComposer(new ChatMessageComposer(new RoomChatMessage(replaceUser(getTextsValue("commands.succes.cmd_pull.pull"), params[1]).replace("%gender_name%", (gameClient.getHabbo().getHabboInfo().getGender().equals(HabboGender.M) ? getTextsValue("gender.him") : getTextsValue("gender.her"))), gameClient.getHabbo(), gameClient.getHabbo(), RoomChatMessageBubbles.NORMAL)).compose());
        }


        return true;
    }
}
