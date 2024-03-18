package com.eu.habbo.messages.incoming.inventory;

import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.FurniListComposer;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;

@Slf4j
public class RequestFurniInventoryWhenNotInRoomEvent extends MessageHandler {

    @Override
    public void handle() {
        int totalItems = this.client.getHabbo().getInventory().getItemsComponent().getItems().size();

        if (totalItems == 0) {
                this.client.sendResponse(new FurniListComposer(0, 1, new TIntObjectHashMap<>()));
                return;
            }
            
        int totalFragments = (int) Math.ceil((double) totalItems / 1000.0);

        if (totalFragments == 0) {
            totalFragments = 1;
        }

        synchronized (this.client.getHabbo().getInventory().getItemsComponent().getItems()) {
            TIntObjectMap<RoomItem> items = new TIntObjectHashMap<>();

            TIntObjectIterator<RoomItem> iterator = this.client.getHabbo().getInventory().getItemsComponent().getItems().iterator();

            int count = 0;
            int fragmentNumber = 0;

            for (int i = this.client.getHabbo().getInventory().getItemsComponent().getItems().size(); i-- > 0; ) {

                if (count == 0) {
                    fragmentNumber++;
                }

                try {
                    iterator.advance();
                    items.put(iterator.key(), iterator.value());
                    count++;
                } catch (NoSuchElementException e) {
                    log.error("Caught exception", e);
                    break;
                }

                if (count == 1000) {
                    this.client.sendResponse(new FurniListComposer(fragmentNumber, totalFragments, items));
                    count = 0;
                    items.clear();
                }
            }

            if(count > 0 && items.size() > 0) this.client.sendResponse(new FurniListComposer(fragmentNumber, totalFragments, items));
        }
    }
}
