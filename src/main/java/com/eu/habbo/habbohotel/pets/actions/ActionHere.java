package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.constants.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;

public class ActionHere extends PetAction {
    public ActionHere() {
        super(PetTasks.HERE, false);

        this.statusToRemove.add(RoomUnitStatus.DEAD);
    }

    @Override
    public boolean apply(Pet pet, Habbo habbo, String[] data) {
        pet.getRoomUnit().walkTo(pet.getRoom().getLayout().getTileInFront(habbo.getRoomUnit().getCurrentPosition(), habbo.getRoomUnit().getBodyRotation().getValue()));
        pet.getRoomUnit().setCanWalk(true);

        if (pet.getHappiness() > 50) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.PLAYFUL));
        } else {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.GENERIC_NEUTRAL));
        }

        return true;
    }
}
