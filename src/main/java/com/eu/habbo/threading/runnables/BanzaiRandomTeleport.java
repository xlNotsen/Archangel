package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.entities.RoomRotation;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import lombok.AllArgsConstructor;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class BanzaiRandomTeleport implements Runnable {
    private final RoomItem item;
    private final RoomItem toItem;
    private final RoomUnit habbo;
    private final Room room;


    @Override
    public void run() {
        RoomItem topItemNow = this.room.getRoomItemManager().getTopItemAt(this.habbo.getCurrentPosition().getX(), this.habbo.getCurrentPosition().getY());
        RoomTile lastLocation = this.habbo.getCurrentPosition();
        RoomTile newLocation = this.room.getLayout().getTile(toItem.getCurrentPosition().getX(), toItem.getCurrentPosition().getY());

        if(topItemNow != null) {
            try {
                topItemNow.onWalkOff(this.habbo, this.room, new Object[] { lastLocation, newLocation, this });
            } catch (Exception e) {
                LoggerFactory.getLogger(BanzaiRandomTeleport.class).error("BanzaiRandomTeleport exception", e);
            }
        }

        Emulator.getThreading().run(() -> {
            if (this.item.getExtraData().equals("1")) {
                this.item.setExtraData("0");
                this.room.updateItemState(this.item);
            }
        }, 500);

        if(!this.toItem.getExtraData().equals("1")) {
            this.toItem.setExtraData("1");
            this.room.updateItemState(this.toItem);
        }

        Emulator.getThreading().run(() -> {
            this.habbo.setCanWalk(true);
            RoomItem topItemNext = this.room.getRoomItemManager().getTopItemAt(this.habbo.getCurrentPosition().getX(), this.habbo.getCurrentPosition().getY());

            if(topItemNext != null) {
                try {
                    topItemNext.onWalkOn(this.habbo, this.room, new Object[] { lastLocation, newLocation, this });
                } catch (Exception e) {
                    LoggerFactory.getLogger(BanzaiRandomTeleport.class).error("BanzaiRandomTeleport exception", e);
                }
            }

            if (this.toItem.getExtraData().equals("1")) {
                this.toItem.setExtraData("0");
                this.room.updateItemState(this.toItem);
            }
        }, 750);

        Emulator.getThreading().run(() -> {
            this.habbo.setRotation(RoomRotation.fromValue(Emulator.getRandom().nextInt(8)));
            this.room.teleportRoomUnitToLocation(this.habbo, newLocation.getX(), newLocation.getY(), newLocation.getStackHeight());
        }, 250);

    }
}
