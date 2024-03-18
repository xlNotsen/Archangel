package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetBreedingNest;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.pets.breeding.GoToBreedingNestFailureComposer;
import org.apache.commons.lang3.StringUtils;

public class ActionBreed extends PetAction {
    public ActionBreed() {
        super(PetTasks.BREED, true);
    }

    @Override
    public boolean apply(Pet pet, Habbo habbo, String[] data) {
        InteractionPetBreedingNest nest = null;
        for (RoomItem item : pet.getRoom().getRoomSpecialTypes().getItemsOfType(InteractionPetBreedingNest.class)) {
            if (StringUtils.containsIgnoreCase(item.getBaseItem().getName(), pet.getPetData().getName()) && !((InteractionPetBreedingNest) item).boxFull()) {
                nest = (InteractionPetBreedingNest) item;
                break;
            }
        }

        if (nest != null) {
            pet.getRoomUnit().walkTo(pet.getRoom().getLayout().getTile(nest.getCurrentPosition().getX(), nest.getCurrentPosition().getY()));

            return true;
        } else {
            habbo.getClient().sendResponse(new GoToBreedingNestFailureComposer(GoToBreedingNestFailureComposer.NO_NESTS));
        }

        return false;
    }
}
