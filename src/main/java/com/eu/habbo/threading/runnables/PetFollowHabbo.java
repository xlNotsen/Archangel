package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PetFollowHabbo implements Runnable {
    private final Pet pet;
    private final Habbo habbo;
    private final int directionOffset;

    @Override
    public void run() {
        if (this.pet != null) {
            if (this.pet.getTask() != PetTasks.FOLLOW)
                return;

            if (this.habbo != null) {
                if (this.habbo.getRoomUnit() != null) {
                    if (this.pet.getRoomUnit() != null) {
                        RoomTile target = this.habbo.getRoomUnit().getRoom().getLayout().getTileInFront(this.habbo.getRoomUnit().getCurrentPosition(), Math.abs((this.habbo.getRoomUnit().getBodyRotation().getValue() + this.directionOffset + 4) % 8));

                        if (target != null) {
                            if (target.getX() < 0 || target.getY() < 0)
                                target = this.habbo.getRoomUnit().getRoom().getLayout().getTileInFront(this.habbo.getRoomUnit().getCurrentPosition(), this.habbo.getRoomUnit().getBodyRotation().getValue());

                            if (target.getX() >= 0 && target.getY() >= 0) {
                                if (this.pet.getRoom().getLayout().tileWalkable(target)) {
                                    this.pet.getRoomUnit().walkTo(target);
                                    this.pet.getRoomUnit().setCanWalk(true);
                                    this.pet.setTask(PetTasks.FOLLOW);
                                }
                            }
                            if(target.distance(this.pet.getRoomUnit().getCurrentPosition()) > 1) {
                                Emulator.getThreading().run(this, 500);
                            } else {
                                this.pet.setTask(PetTasks.FREE);
                            }
                        }
                    }
                }
            }
        }
    }
}
