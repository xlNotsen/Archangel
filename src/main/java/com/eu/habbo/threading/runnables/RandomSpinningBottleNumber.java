package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;

public class RandomSpinningBottleNumber implements Runnable {
    private final RoomItem item;
    private final Room room;
    private final int maxNumber;
    private int result;

    public RandomSpinningBottleNumber(RoomItem item, Room room, int maxNumber) {
        this.item = item;
        this.room = room;
        this.maxNumber = maxNumber;
        this.result = -1;
    }

    public RandomSpinningBottleNumber(Room room, RoomItem item, int result) {
        this.item = item;
        this.room = room;
        this.maxNumber = -1;
        this.result = result;
    }

    @Override
    public void run() {
        if (this.result <= 0)
            this.result = Emulator.getRandom().nextInt(this.maxNumber);

        this.item.setExtraData(this.result + "");
        this.item.setSqlUpdateNeeded(true);
        Emulator.getThreading().run(this.item);

        this.room.updateItem(this.item);
    }
}

