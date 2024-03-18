package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.threading.runnables.RoomUnitRidePet;

import java.util.List;

public class MountPetEvent extends MessageHandler {
    @Override
    public void handle() {
        int petId = this.packet.readInt();
        Habbo habbo = this.client.getHabbo();
        Room room = habbo.getRoomUnit().getRoom();

        if (room == null) {
            return;
        }

        Pet pet = room.getRoomUnitManager().getRoomPetManager().getRoomPetById(petId);

        if (!(pet instanceof RideablePet rideablePet)) {
            return;
        }

        if (habbo.getRoomUnit().isRiding()) {
            boolean mountAnotherPet = petId != habbo.getRoomUnit().getRidingPet().getId();
            habbo.getRoomUnit().dismountPet(false);

            if(!mountAnotherPet) {
                return;
            }
        }

        if (rideablePet.getRider() != null) {
            return;
        }

        if (!rideablePet.anyoneCanRide() && habbo.getHabboInfo().getId() != rideablePet.getUserId()) {
            return;
        }

        List<RoomTile> availableTiles = room.getLayout().getWalkableTilesAround(pet.getRoomUnit().getCurrentPosition());

        // if cant reach it then cancel
        if (availableTiles.isEmpty()) {
            return;
        }

        RoomTile goalTile = availableTiles.get(0);
        habbo.getRoomUnit().walkTo(goalTile);
        habbo.getRoomUnit().setRideLocked(true);
        Emulator.getThreading().run(new RoomUnitRidePet(rideablePet, habbo, goalTile));
        rideablePet.getRoomUnit().setWalkTimeOut(3 + Emulator.getIntUnixTimestamp());
        rideablePet.getRoomUnit().stopWalking();
    }
}
