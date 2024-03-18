package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HabboItemNewState implements Runnable {
    private final RoomItem item;
    private final Room room;
    private final String state;

    @Override
    public void run() {
        this.item.setExtraData(this.state);

        if (this.item.getRoomId() == this.room.getRoomInfo().getId()) {
            this.room.updateItemState(this.item);
        }
    }
}
