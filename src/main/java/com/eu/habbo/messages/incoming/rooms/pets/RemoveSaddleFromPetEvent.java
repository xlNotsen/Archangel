package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.pets.HorsePet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.FurniListInvalidateComposer;
import com.eu.habbo.messages.outgoing.inventory.UnseenItemsComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.PetFigureUpdateComposer;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class RemoveSaddleFromPetEvent extends MessageHandler {


    @Override
    public void handle() {
        Room room = this.client.getHabbo().getRoomUnit().getRoom();
        int petId = this.packet.readInt();
        Pet pet = room.getRoomUnitManager().getRoomPetManager().getRoomPetById(petId);

        if (!(pet instanceof HorsePet horse) || pet.getUserId() != this.client.getHabbo().getHabboInfo().getId()) return;

        if (!horse.hasSaddle()) return;

        int saddleItemId = horse.getSaddleItemId();

        if (saddleItemId == 0) { // backwards compatibility: horses could be missing the saddle item ID
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id FROM items_base WHERE item_name LIKE 'horse_saddle%' LIMIT 1")) {
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        saddleItemId = set.getInt("id");
                    } else {
                        log.error("There is no viable fallback saddle item for old horses with no saddle item ID. Horse pet ID: " + horse.getId());
                        return;
                    }
                }
            } catch (SQLException e) {
                log.error("Caught SQL exception", e);
            }
        }

        Item saddleItem = Emulator.getGameEnvironment().getItemManager().getItem(saddleItemId);

        if (saddleItem == null) return;

        horse.hasSaddle(false);
        horse.setSqlUpdateNeeded(true);
        Emulator.getThreading().run(pet);
        this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new PetFigureUpdateComposer(horse).compose());

        RoomItem saddle = Emulator.getGameEnvironment().getItemManager().createItem(this.client.getHabbo().getHabboInfo().getId(), saddleItem, 0, 0, "");

        this.client.getHabbo().getInventory().getItemsComponent().addItem(saddle);

        this.client.sendResponse(new UnseenItemsComposer(saddle));
        this.client.sendResponse(new FurniListInvalidateComposer());
    }
}
