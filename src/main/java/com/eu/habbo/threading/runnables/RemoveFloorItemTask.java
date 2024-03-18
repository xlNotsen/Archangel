package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.outgoing.rooms.HeightMapUpdateMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class RemoveFloorItemTask implements Runnable {
    private final Room room;
    private final RoomItem item;


    @Override
    public void run() {
        if (this.item == null || this.room == null)
            return;

        RoomTile tile = this.room.getLayout().getTile(this.item.getCurrentPosition().getX(), this.item.getCurrentPosition().getY());
        this.room.getRoomItemManager().removeRoomItem(this.item);
        this.room.updateTile(tile);
        this.room.sendComposer(new RemoveFloorItemComposer(this.item, true).compose());
        this.room.sendComposer(new HeightMapUpdateMessageComposer(this.item.getCurrentPosition().getX(), this.item.getCurrentPosition().getY(), tile.getZ(), tile.relativeHeight()).compose());
    }
}
