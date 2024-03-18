package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.InteractionRandomState;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SetRandomStateEvent extends MessageHandler {

    @Override
    public void handle() {
        try {
            int itemId = this.packet.readInt();
            int state = this.packet.readInt();

            Room room = this.client.getHabbo().getRoomUnit().getRoom();

            RoomItem item = room.getRoomItemManager().getRoomItemById(itemId);

            if (!(item instanceof InteractionRandomState randomStateItem))
                return;

            randomStateItem.onRandomStateClick(room);
        } catch (Exception e) {
            log.error("Caught exception", e);
        }
    }
}
