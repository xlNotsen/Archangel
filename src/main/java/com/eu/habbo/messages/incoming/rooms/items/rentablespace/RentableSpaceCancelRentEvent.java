package com.eu.habbo.messages.incoming.rooms.items.rentablespace;

import com.eu.habbo.habbohotel.items.interactions.InteractionRentableSpace;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;

public class RentableSpaceCancelRentEvent extends MessageHandler {
    @Override
    public void handle() {
        int itemId = this.packet.readInt();

        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room == null)
            return;

        RoomItem item = room.getRoomItemManager().getRoomItemById(itemId);

        if (room.getRoomInfo().getOwnerInfo().getId() == this.client.getHabbo().getHabboInfo().getId() ||
                this.client.getHabbo().hasPermissionRight(Permission.ACC_ANYROOMOWNER)) {
            if (item instanceof InteractionRentableSpace) {
                ((InteractionRentableSpace) item).endRent();

                room.updateItem(item);
            }
        }
    }
}
