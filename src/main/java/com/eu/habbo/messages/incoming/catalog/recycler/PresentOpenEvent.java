package com.eu.habbo.messages.incoming.catalog.recycler;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionGift;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.*;
import com.eu.habbo.habbohotel.rooms.chat.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.FurniListInvalidateComposer;
import com.eu.habbo.messages.outgoing.inventory.UnseenItemsComposer;
import com.eu.habbo.messages.outgoing.rooms.HeightMapUpdateMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.items.PresentOpenedMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.users.WhisperMessageComposer;
import com.eu.habbo.threading.runnables.OpenGift;

public class PresentOpenEvent extends MessageHandler {
    @Override
    public void handle() {
        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room == null)
            return;

        if (room.getRoomInfo().getOwnerInfo().getId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermissionRight(Permission.ACC_ANYROOMOWNER)) {
            int id = this.packet.readInt();
            RoomItem item = room.getRoomItemManager().getRoomItemById(id);

            if (item == null)
                return;

            if (item instanceof InteractionGift) {
                if (item.getBaseItem().getName().contains("present_wrap")) {
                    ((InteractionGift) item).explode = true;
                    room.updateItem(item);
                }

                Emulator.getThreading().run(new OpenGift(item, this.client.getHabbo(), room), item.getBaseItem().getName().contains("present_wrap") ? 1000 : 0);
            } else {
                if (item.getExtraData().length() == 0) {
                    this.client.sendResponse(new WhisperMessageComposer(new RoomChatMessage(Emulator.getTexts().getValue("error.recycler.box.empty"), this.client.getHabbo(), this.client.getHabbo(), RoomChatMessageBubbles.BOT)));
                } else {
                    RoomItem reward = Emulator.getGameEnvironment().getItemManager().handleOpenRecycleBox(this.client.getHabbo(), item);

                    if (reward != null) {
                        this.client.getHabbo().getInventory().getItemsComponent().addItem(reward);
                        this.client.sendResponse(new UnseenItemsComposer(reward));
                        this.client.sendResponse(new FurniListInvalidateComposer());

                        this.client.sendResponse(new PresentOpenedMessageComposer(reward, item.getExtraData(), true));
                    }
                }
                room.sendComposer(new RemoveFloorItemComposer(item).compose());
                room.getRoomItemManager().removeRoomItem(item);

            }

            if (item.getRoomId() == 0) {
                room.updateTile(room.getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY()));
                RoomLayout roomLayout = room.getLayout();
                short z = (short)room.getStackHeight(item.getCurrentPosition().getX(), item.getCurrentPosition().getY(), true);
                if(roomLayout != null) {
                    RoomTile roomTile = roomLayout.getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY());
                    if(roomTile != null) {
                        z = roomTile.getZ();
                    }
                }
                room.sendComposer(new HeightMapUpdateMessageComposer(item.getCurrentPosition().getX(), item.getCurrentPosition().getY(), z, room.getStackHeight(item.getCurrentPosition().getX(), item.getCurrentPosition().getY(), true)).compose());
            }
        }
    }
}
