package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.entities.RoomRotation;
import com.eu.habbo.habbohotel.users.Habbo;

public class ActionTurnRight extends PetAction {
    public ActionTurnRight() {
        super(null, true);
    }

    @Override
    public boolean apply(Pet pet, Habbo habbo, String[] data) {
        pet.getRoomUnit().setBodyRotation(RoomRotation.values()[(pet.getRoomUnit().getBodyRotation().getValue() + 1 > 7 ? 0 : pet.getRoomUnit().getBodyRotation().getValue() + 1)]);
        pet.getRoomUnit().setHeadRotation(RoomRotation.values()[(pet.getRoomUnit().getHeadRotation().getValue() + 1 > 7 ? 0 : pet.getRoomUnit().getHeadRotation().getValue() + 1)]);
        pet.say(pet.getPetData().randomVocal(PetVocalsType.GENERIC_NEUTRAL));
        return true;
    }
}
