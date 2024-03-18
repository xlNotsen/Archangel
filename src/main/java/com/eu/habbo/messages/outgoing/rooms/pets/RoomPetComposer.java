package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.habbohotel.pets.*;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import lombok.AllArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class RoomPetComposer extends MessageComposer {
    private final ConcurrentHashMap<Integer, Pet> pets;

    public RoomPetComposer(Pet pet) {
        this.pets = new ConcurrentHashMap<>();
        this.pets.put(pet.getId(), pet);
    }


    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.usersComposer);
        this.response.appendInt(this.pets.size());
        this.pets.forEach((a, p) -> this.execute(a, p));
        return this.response;
    }

    public boolean execute(int a, Pet pet) {
        this.response.appendInt(pet.getId());
        this.response.appendString(pet.getName());
        this.response.appendString("");
        if (pet instanceof IPetLook) {
            this.response.appendString(((IPetLook) pet).getLook());
        } else {
            // TODO: It will never be a type of MonsterplantPet in this scenario?
            this.response.appendString(pet.getPetData().getType() + " " + pet.getRace() + " " + pet.getColor() + " " + ((pet instanceof HorsePet ? (((HorsePet) pet).hasSaddle() ? "3" : "2") + " 2 " + ((HorsePet) pet).getHairStyle() + " " + ((HorsePet) pet).getHairColor() + " 3 " + ((HorsePet) pet).getHairStyle() + " " + ((HorsePet) pet).getHairColor() + (((HorsePet) pet).hasSaddle() ? " 4 9 0" : "") : pet instanceof MonsterplantPet ? (((MonsterplantPet) pet).look.isEmpty() ? "2 1 8 6 0 -1 -1" : ((MonsterplantPet) pet).look) : "2 2 -1 0 3 -1 0")));
        }
        this.response.appendInt(pet.getRoomUnit().getVirtualId());
        this.response.appendInt(pet.getRoomUnit().getCurrentPosition().getX());
        this.response.appendInt(pet.getRoomUnit().getCurrentPosition().getY());
        this.response.appendString(pet.getRoomUnit().getCurrentZ() + "");
        this.response.appendInt(0);
        this.response.appendInt(2);
        this.response.appendInt(pet.getPetData().getType());
        this.response.appendInt(pet.getUserId());
        this.response.appendString(pet.getRoom().getFurniOwnerNames().get(pet.getUserId()));
        this.response.appendInt(pet instanceof MonsterplantPet ? ((MonsterplantPet) pet).getRarity() : 1);
        this.response.appendBoolean(pet instanceof RideablePet && ((RideablePet) pet).hasSaddle());
        this.response.appendBoolean(false);
        this.response.appendBoolean((pet instanceof MonsterplantPet && ((MonsterplantPet) pet).canBreed())); //Has breeasasd//
        this.response.appendBoolean(!(pet instanceof MonsterplantPet && ((MonsterplantPet) pet).isFullyGrown())); //unknown 1
        this.response.appendBoolean(pet instanceof MonsterplantPet && ((MonsterplantPet) pet).isDead()); //Can revive
        this.response.appendBoolean(pet instanceof MonsterplantPet && ((MonsterplantPet) pet).isPubliclyBreedable()); //Breedable checkbox //Toggle breeding permission
        this.response.appendInt(pet instanceof MonsterplantPet ? ((MonsterplantPet) pet).getGrowthStage() : pet.getLevel());
        this.response.appendString("");

        return true;
    }
}
