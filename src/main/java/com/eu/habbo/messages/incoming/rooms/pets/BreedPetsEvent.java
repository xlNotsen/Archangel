package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.incoming.MessageHandler;

public class BreedPetsEvent extends MessageHandler {
    @Override
    public void handle() {
        int unknownInt = this.packet.readInt(); //Something state. 2 = accept

        if (unknownInt == 0) {
            int petId1 = this.packet.readInt();
            Pet petOne = this.client.getHabbo().getRoomUnit().getRoom().getRoomUnitManager().getRoomPetManager().getRoomPetById(petId1);
            int petId = this.packet.readInt();
            Pet petTwo = this.client.getHabbo().getRoomUnit().getRoom().getRoomUnitManager().getRoomPetManager().getRoomPetById(petId);

            if (petOne == null || petTwo == null || petOne == petTwo) {
                //TODO Add error
                return;
            }

            if (petOne instanceof MonsterplantPet && petTwo instanceof MonsterplantPet) {
                ((MonsterplantPet) petOne).breed((MonsterplantPet) petTwo);
            }
        }
    }
}
