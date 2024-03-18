package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RoomUnitWalkToRoomUnit implements Runnable {
    private final RoomUnit walker;
    private final RoomUnit target;
    private final Room room;
    private final List<Runnable> targetReached;
    private final List<Runnable> failedReached;
    private final int minDistance;

    private RoomTile goalTile = null;

    public RoomUnitWalkToRoomUnit(RoomUnit walker, RoomUnit target, Room room, List<Runnable> targetReached, List<Runnable> failedReached) {
        this.walker = walker;
        this.target = target;
        this.room = room;
        this.targetReached = targetReached;
        this.failedReached = failedReached;
        this.minDistance = 1;
    }

    @Override
    public void run() {
        if (this.goalTile == null) {
            this.findNewLocation();
            Emulator.getThreading().run(this, 500);
            return;
        }

        if (this.walker.getTargetPosition().equals(this.goalTile)) { // check that the action hasn't been cancelled by changing the goal
            if (this.walker.getCurrentPosition().distance(this.goalTile) <= this.minDistance) {
                for (Runnable r : this.targetReached) {
                    Emulator.getThreading().run(r);

                    WiredHandler.handle(WiredTriggerType.BOT_REACHED_AVTR, this.target, this.room, new Object[]{ this.walker });
                }
            } else {
                Emulator.getThreading().run(this, 500);
            }
        }
    }

    private void findNewLocation() {
        this.goalTile = this.walker.getClosestAdjacentTile(this.target.getCurrentPosition().getX(), this.target.getCurrentPosition().getY(), true);

        if (this.goalTile == null) {
            if (this.failedReached != null) {
                for (Runnable r : this.failedReached) {
                    Emulator.getThreading().run(r);
                }
            }

            return;
        }

        this.walker.walkTo(this.goalTile);

        if (this.walker.getPath().isEmpty() && this.failedReached != null) {
            for (Runnable r : this.failedReached) {
                Emulator.getThreading().run(r);
            }
        }
    }
}
