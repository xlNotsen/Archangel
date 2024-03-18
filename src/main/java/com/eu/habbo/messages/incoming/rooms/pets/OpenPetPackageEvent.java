package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.PurchaseErrorMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.HeightMapUpdateMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.PerkAllowancesComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;

public class OpenPetPackageEvent extends MessageHandler {
    @Override
    public void handle() {
        int itemId = this.packet.readInt();
        String name = this.packet.readString();

        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room != null) {
            RoomItem item = room.getRoomItemManager().getRoomItemById(itemId);
            if (item != null && item.getOwnerInfo().getId() == this.client.getHabbo().getHabboInfo().getId()) {
                if (!name.matches("^[a-zA-Z0-9]*$")) {
                    this.client.sendResponse(new PerkAllowancesComposer(itemId, PerkAllowancesComposer.CONTAINS_INVALID_CHARS, name.replaceAll("^[a-zA-Z0-9]*$", "")));
                    return;
                }

                Pet pet = null;

                if (item.getBaseItem().getName().equalsIgnoreCase("val11_present")) {
                    pet = Emulator.getGameEnvironment().getPetManager().createPet(11, name, this.client);
                }

                if (item.getBaseItem().getName().equalsIgnoreCase("gnome_box")) {
                    pet = Emulator.getGameEnvironment().getPetManager().createGnome(name, room, this.client.getHabbo());
                }

                if (item.getBaseItem().getName().equalsIgnoreCase("leprechaun_box")) {
                    pet = Emulator.getGameEnvironment().getPetManager().createLeprechaun(name, room, this.client.getHabbo());
                }

                if (item.getBaseItem().getName().equalsIgnoreCase("velociraptor_egg")) {
                    pet = Emulator.getGameEnvironment().getPetManager().createPet(34, name, this.client);
                }

                if (item.getBaseItem().getName().equalsIgnoreCase("pterosaur_egg")) {
                    pet = Emulator.getGameEnvironment().getPetManager().createPet(33, name, this.client);
                }

                if (item.getBaseItem().getName().equalsIgnoreCase("petbox_epic")) {
                    pet = Emulator.getGameEnvironment().getPetManager().createPet(32, name, this.client);
                }

                if (pet != null) {
                    room.getRoomUnitManager().getRoomPetManager().placePet(pet, room, item.getCurrentPosition().getX(), item.getCurrentPosition().getY(), item.getCurrentZ());
                    pet.setUserId(this.client.getHabbo().getHabboInfo().getId());
                    pet.setSqlUpdateNeeded(true);
                    pet.getRoomUnit().setLocation(room.getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY()));
                    pet.getRoomUnit().setCurrentZ(item.getCurrentZ());
                    Emulator.getThreading().run(new QueryDeleteHabboItem(item.getId()));
                    room.getRoomItemManager().removeRoomItem(item);
                    room.sendComposer(new RemoveFloorItemComposer(item).compose());
                    RoomTile tile = room.getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY());
                    room.updateTile(room.getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY()));
                    room.sendComposer(new HeightMapUpdateMessageComposer(tile.getX(), tile.getY(), tile.getZ(), tile.relativeHeight()).compose());
                    item.setOwnerInfo(null);
                } else {
                    this.client.sendResponse(new PurchaseErrorMessageComposer(PurchaseErrorMessageComposer.SERVER_ERROR));
                }
            }
        }


        this.client.sendResponse(new PerkAllowancesComposer(itemId, PerkAllowancesComposer.CLOSE_WIDGET, ""));
    }
}