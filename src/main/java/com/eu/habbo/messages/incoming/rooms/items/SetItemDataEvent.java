package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.PostItColor;
import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;

import java.util.Arrays;
import java.util.List;

public class SetItemDataEvent extends MessageHandler {
    private static final List<String> COLORS = Arrays.asList("9CCEFF", "FF9CFF", "9CFF9C", "FFFF33");

    @Override
    public void handle() {
        int itemId = this.packet.readInt();
        String color = this.packet.readString();
        String text = Emulator.getGameEnvironment().getWordFilter().filter(this.packet.readString().replace(((char) 9) + "", ""), this.client.getHabbo());

        if (text.length() > Emulator.getConfig().getInt("postit.charlimit")) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.sticky.size").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%amount%", text.length() + "").replace("%limit%", Emulator.getConfig().getInt("postit.charlimit") + ""));
            return;
        }

        if (!COLORS.contains(color)) {
            return;
        }

        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room == null)
            return;

        RoomItem item = room.getRoomItemManager().getRoomItemById(itemId);

        if (!(item instanceof InteractionPostIt))
            return;

        if (!color.equalsIgnoreCase(PostItColor.YELLOW.hexColor) && !room.getRoomRightsManager().hasRights(this.client.getHabbo())) {
            if (!text.startsWith(item.getExtraData().replace(item.getExtraData().split(" ")[0], ""))) {
                return;
            }
        } else {
            if (!room.getRoomRightsManager().hasRights(this.client.getHabbo()))
                return;
        }

        if (color.isEmpty())
            color = PostItColor.YELLOW.hexColor;

        item.setExtraData(color + " " + text);
        item.setSqlUpdateNeeded(true);
        room.updateItem(item);
        Emulator.getThreading().run(item);
    }
}
