package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.interactions.*;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.constants.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.generic.alerts.NotificationDialogMessageComposer;
import com.eu.habbo.messages.outgoing.inventory.FurniListRemoveComposer;

public class PlaceObjectEvent extends MessageHandler {
    @Override
    public void handle() {
        if (!this.client.getHabbo().getRoomUnit().isInRoom()) {
            this.client.sendResponse(new NotificationDialogMessageComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.getKey(), FurnitureMovementError.NO_RIGHTS.getErrorCode()));
            return;
        }

        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room == null) {
            return;
        }

        String[] values = this.packet.readString().split(" ");

        int itemId = -1;

        if (values.length != 0) {
            itemId = Integer.parseInt(values[0]);
        }

        RoomItem item = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(itemId);

        //PostIts have their own event
        if (item == null || item.getBaseItem().getInteractionType().getType() == InteractionPostIt.class) {
            return;
        }

        if (room.getRoomInfo().getId() != item.getRoomId() && item.getRoomId() != 0) {
            return;
        }

        if (item.getBaseItem().getType() == FurnitureType.FLOOR) {
            short x = Short.parseShort(values[1]);
            short y = Short.parseShort(values[2]);
            int rotation = Integer.parseInt(values[3]);

            RoomTile tile = room.getLayout().getTile(x, y);

            if(tile == null)
            {
                String userName  = this.client.getHabbo().getHabboInfo().getUsername();
                int roomId = room.getRoomInfo().getId();
                ScripterManager.scripterDetected(this.client, "User [" + userName + "] tried to place a furni with itemId [" + itemId + "] at a tile which is not existing in room [" + roomId + "], tile: [" + x + "," + y + "]");
                return;
            }

            RoomItem buildArea = null;

            for (RoomItem area : room.getRoomSpecialTypes().getItemsOfType(InteractionBuildArea.class)) {
                if (((InteractionBuildArea) area).inSquare(tile)) {
                    buildArea = area;
                }
            }

            RoomItem rentSpace = null;

            if (this.client.getHabbo().getHabboStats().isRentingSpace()) {
                rentSpace = room.getRoomItemManager().getRoomItemById(this.client.getHabbo().getHabboStats().getRentedItemId());
            }

            if ((rentSpace != null || buildArea != null) && !room.getRoomRightsManager().hasRights(this.client.getHabbo())) {
                if (item instanceof InteractionRoller ||
                        item instanceof InteractionStackHelper ||
                        item instanceof InteractionWired ||
                        item instanceof InteractionBackgroundToner ||
                        item instanceof InteractionRoomAds ||
                        item instanceof InteractionCannon ||
                        item instanceof InteractionPuzzleBox ||
                        item.getBaseItem().getType() == FurnitureType.WALL) {
                    this.client.sendResponse(new NotificationDialogMessageComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.getKey(), FurnitureMovementError.NO_RIGHTS.getErrorCode()));
                    return;
                }
                if (rentSpace != null && !RoomLayout.squareInSquare(RoomLayout.getRectangle(rentSpace.getCurrentPosition().getX(), rentSpace.getCurrentPosition().getY(), rentSpace.getBaseItem().getWidth(), rentSpace.getBaseItem().getLength(), rentSpace.getRotation()), RoomLayout.getRectangle(x, y, item.getBaseItem().getWidth(), item.getBaseItem().getLength(), rotation))) {
                    this.client.sendResponse(new NotificationDialogMessageComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.getKey(), FurnitureMovementError.NO_RIGHTS.getErrorCode()));
                    return;
                }
            }

            FurnitureMovementError error = room.getRoomItemManager().placeFloorItemAt(item, tile, rotation, this.client.getHabbo());

            if (!error.equals(FurnitureMovementError.NONE)) {
                this.client.sendResponse(new NotificationDialogMessageComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.getKey(), error.getErrorCode()));
                return;
            }

        } else {
            FurnitureMovementError error = room.getRoomItemManager().placeWallItemAt(item, values[1] + " " + values[2] + " " + values[3], this.client.getHabbo());
            if (!error.equals(FurnitureMovementError.NONE)) {
                this.client.sendResponse(new NotificationDialogMessageComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.getKey(), error.getErrorCode()));
                return;
            }
        }

        this.client.sendResponse(new FurniListRemoveComposer(item.getGiftAdjustedId()));
        this.client.getHabbo().getInventory().getItemsComponent().removeHabboItem(item.getId());
        item.setFromGift(false);
    }
}
