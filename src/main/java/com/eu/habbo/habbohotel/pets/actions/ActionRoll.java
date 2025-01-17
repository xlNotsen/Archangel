package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetTree;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.constants.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;

public class ActionRoll extends PetAction {
    public ActionRoll() {
        super(null, true);
        this.minimumActionDuration = 4000;
    }

    @Override
    public boolean apply(Pet pet, Habbo habbo, String[] data) {

        boolean findTree = pet.findPetItem(PetTasks.ROLL, InteractionPetTree.class);
        if (!findTree && pet.getPetData().getToyItems().stream().noneMatch(item -> item.getInteractionType().getType() == InteractionPetTree.class)) {
            pet.getRoomUnit().setCanWalk(false);
            pet.getRoomUnit().addStatus(RoomUnitStatus.ROLL, pet.getRoomUnit().getCurrentPosition().getStackHeight() + "");

            Emulator.getThreading().run(() -> {
                pet.getRoomUnit().setCanWalk(true);
                pet.clearPosture();
            }, minimumActionDuration);
        } else if (!findTree) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.DISOBEY));
            return false;
        }

        pet.say(pet.getPetData().randomVocal(PetVocalsType.GENERIC_NEUTRAL));
        return true;
    }
}