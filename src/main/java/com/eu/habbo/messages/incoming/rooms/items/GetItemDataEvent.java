package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.ItemDataUpdateMessageComposer;

public class GetItemDataEvent extends MessageHandler {
    @Override
    public void handle() {
        int itemId = this.packet.readInt();

        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room != null) {
            RoomItem item = room.getRoomItemManager().getRoomItemById(itemId);

            if (item instanceof InteractionPostIt) {
                this.client.sendResponse(new ItemDataUpdateMessageComposer((InteractionPostIt) item));
            }
        }
    }
}
