package com.eu.habbo.messages.incoming.rooms.items.rentablespace;

import com.eu.habbo.habbohotel.items.interactions.InteractionRentableSpace;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;

public class RentableSpaceRentEvent extends MessageHandler {
    @Override
    public void handle() {
        int itemId = this.packet.readInt();

        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room == null)
            return;

        RoomItem item = room.getRoomItemManager().getRoomItemById(itemId);

        if (!(item instanceof InteractionRentableSpace))
            return;

        ((InteractionRentableSpace) item).rent(this.client.getHabbo());

        room.updateItem(item);

        ((InteractionRentableSpace) item).sendRentWidget(this.client.getHabbo());
    }
}
