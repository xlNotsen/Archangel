package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.ClothItem;
import com.eu.habbo.habbohotel.items.interactions.InteractionClothing;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.generic.alerts.NotificationDialogMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.HeightMapUpdateMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.users.FigureSetIdsComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
public class CustomizeAvatarWithFurniEvent extends MessageHandler {


    @Override
    public void handle() {
        int itemId = this.packet.readInt();

        if (this.client.getHabbo().getRoomUnit().getRoom() != null && this.client.getHabbo().getRoomUnit().getRoom().getRoomRightsManager().hasRights(this.client.getHabbo())) {
            RoomItem item = this.client.getHabbo().getRoomUnit().getRoom().getRoomItemManager().getRoomItemById(itemId);

            if (item != null && item.getOwnerInfo().getId() == this.client.getHabbo().getHabboInfo().getId()) {
                if (item instanceof InteractionClothing) {
                    ClothItem clothing = Emulator.getGameEnvironment().getCatalogManager().getClothing(item.getBaseItem().getName());

                    if (clothing != null) {
                        if (!this.client.getHabbo().getInventory().getWardrobeComponent().getClothing().contains(clothing.getId())) {
                            //Deprecated
                            item.setRoomId(0);
                            item.setRoom(null);
                            RoomTile tile = this.client.getHabbo().getRoomUnit().getRoom().getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY());
                            this.client.getHabbo().getRoomUnit().getRoom().getRoomItemManager().removeRoomItem(item);
                            this.client.getHabbo().getRoomUnit().getRoom().updateTile(tile);
                            this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new HeightMapUpdateMessageComposer(tile.getX(), tile.getY(), tile.getZ(), tile.relativeHeight()).compose());
                            this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new RemoveFloorItemComposer(item, true).compose());
                            Emulator.getThreading().run(new QueryDeleteHabboItem(item.getId()));

                            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO users_clothing (user_id, clothing_id) VALUES (?, ?)")) {
                                statement.setInt(1, this.client.getHabbo().getHabboInfo().getId());
                                statement.setInt(2, clothing.getId());
                                statement.execute();
                            } catch (SQLException e) {
                                log.error("Caught SQL exception", e);
                            }

                            this.client.getHabbo().getInventory().getWardrobeComponent().getClothing().add(clothing.getId());
                            this.client.getHabbo().getInventory().getWardrobeComponent().getClothingSets().addAll(clothing.getSetId());
                            this.client.sendResponse(new FigureSetIdsComposer(this.client.getHabbo()));
                            this.client.sendResponse(new NotificationDialogMessageComposer(BubbleAlertKeys.FIGURESET_REDEEMED.getKey()));

                        } else {
                            this.client.sendResponse(new NotificationDialogMessageComposer(BubbleAlertKeys.FIGURESET_OWNED_ALREADY.getKey()));
                        }
                    } else {
                        log.error("[Catalog] No definition in catalog_clothing found for clothing name " + item.getBaseItem().getName() + ". Could not redeem clothing!");
                    }
                }
            }
        }
    }
}
