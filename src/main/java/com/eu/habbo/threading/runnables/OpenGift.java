package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionGift;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.inventory.FurniListAddOrUpdateComposer;
import com.eu.habbo.messages.outgoing.inventory.FurniListInvalidateComposer;
import com.eu.habbo.messages.outgoing.inventory.UnseenItemsComposer;
import com.eu.habbo.messages.outgoing.rooms.items.PresentOpenedMessageComposer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@AllArgsConstructor
public class OpenGift implements Runnable {

    private final RoomItem item;
    private final Habbo habbo;
    private final Room room;

    @Override
    public void run() {
        try {
            RoomItem inside = null;

            HashSet<RoomItem> items = ((InteractionGift) this.item).loadItems();
            for (RoomItem i : items) {
                if (inside == null)
                    inside = i;

                i.setOwnerInfo(this.habbo.getHabboInfo());
                i.setSqlUpdateNeeded(true);
                i.run();
            }

            if (inside != null) inside.setFromGift(true);

            this.habbo.getInventory().getItemsComponent().addItems(items);

            RoomTile tile = this.room.getLayout().getTile(this.item.getCurrentPosition().getX(), this.item.getCurrentPosition().getY());
            if (tile != null) {
                this.room.updateTile(tile);
            }

            Emulator.getThreading().run(new QueryDeleteHabboItem(this.item.getId()));
            Emulator.getThreading().run(new RemoveFloorItemTask(this.room, this.item), this.item.getBaseItem().getName().contains("present_wrap") ? 5000 : 0);

            this.habbo.getClient().sendResponse(new FurniListInvalidateComposer());

            Map<UnseenItemsComposer.AddHabboItemCategory, List<Integer>> unseenItems = new HashMap<>();

            for (RoomItem item : items) {
                switch (item.getBaseItem().getType()) {
                    case WALL, FLOOR -> {
                        if (!unseenItems.containsKey(UnseenItemsComposer.AddHabboItemCategory.OWNED_FURNI))
                            unseenItems.put(UnseenItemsComposer.AddHabboItemCategory.OWNED_FURNI, new ArrayList<>());
                        unseenItems.get(UnseenItemsComposer.AddHabboItemCategory.OWNED_FURNI).add(item.getGiftAdjustedId());
                    }
                    case BADGE -> {
                        if (!unseenItems.containsKey(UnseenItemsComposer.AddHabboItemCategory.BADGE))
                            unseenItems.put(UnseenItemsComposer.AddHabboItemCategory.BADGE, new ArrayList<>());
                        unseenItems.get(UnseenItemsComposer.AddHabboItemCategory.BADGE).add(item.getId()); // badges cannot be placed so no need for gift adjusted ID
                    }
                    case PET -> {
                        if (!unseenItems.containsKey(UnseenItemsComposer.AddHabboItemCategory.PET))
                            unseenItems.put(UnseenItemsComposer.AddHabboItemCategory.PET, new ArrayList<>());
                        unseenItems.get(UnseenItemsComposer.AddHabboItemCategory.PET).add(item.getGiftAdjustedId());
                    }
                    case ROBOT -> {
                        if (!unseenItems.containsKey(UnseenItemsComposer.AddHabboItemCategory.BOT))
                            unseenItems.put(UnseenItemsComposer.AddHabboItemCategory.BOT, new ArrayList<>());
                        unseenItems.get(UnseenItemsComposer.AddHabboItemCategory.BOT).add(item.getGiftAdjustedId());
                    }
                }
            }

            this.habbo.getClient().sendResponse(new UnseenItemsComposer(unseenItems));

            if (inside != null) {
                this.habbo.getClient().sendResponse(new FurniListAddOrUpdateComposer(inside));
                this.habbo.getClient().sendResponse(new PresentOpenedMessageComposer(inside, "", false));
            }
        } catch (Exception e) {
            log.error("Caught exception", e);
        }
    }
}
