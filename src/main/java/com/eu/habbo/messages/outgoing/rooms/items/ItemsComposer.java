package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

@AllArgsConstructor
public class ItemsComposer extends MessageComposer {
    private final Room room;

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.itemsComposer);
        THashMap<Integer, String> userNames = new THashMap<>();
        TIntObjectMap<String> furniOwnerNames = this.room.getFurniOwnerNames();
        TIntObjectIterator<String> iterator = furniOwnerNames.iterator();

        for (int i = furniOwnerNames.size(); i-- > 0; ) {
            try {
                iterator.advance();

                userNames.put(iterator.key(), iterator.value());
            } catch (NoSuchElementException e) {
                break;
            }
        }

        this.response.appendInt(userNames.size());
        for (Map.Entry<Integer, String> set : userNames.entrySet()) {
            this.response.appendInt(set.getKey());
            this.response.appendString(set.getValue());
        }

        Collection<RoomItem> items = this.room.getRoomItemManager().getWallItems().values();

        this.response.appendInt(items.size());
        for (RoomItem item : items) {
            item.serializeWallData(this.response);
        }
        return this.response;
    }
}
