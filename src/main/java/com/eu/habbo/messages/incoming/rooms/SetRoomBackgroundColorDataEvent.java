package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.furniture.FurnitureRoomTonerEvent;

public class SetRoomBackgroundColorDataEvent extends MessageHandler {
    @Override
    public void handle() {
        int itemId = this.packet.readInt();

        Room room = this.client.getHabbo().getRoomUnit().getRoom();
        if (room == null)
            return;

        if (room.getRoomRightsManager().hasRights(this.client.getHabbo()) || this.client.getHabbo().hasPermissionRight(Permission.ACC_PLACEFURNI)) {
            RoomItem item = room.getRoomItemManager().getRoomItemById(itemId);

            if (item == null)
                return;

            int hue = this.packet.readInt();
            int saturation = this.packet.readInt();
            int brightness = this.packet.readInt();

            FurnitureRoomTonerEvent event = Emulator.getPluginManager().fireEvent(new FurnitureRoomTonerEvent(item, this.client.getHabbo(), hue, saturation, brightness));

            if (event.isCancelled())
                return;

            hue = event.getHue() % 256;
            saturation = event.getSaturation() % 256;
            brightness = event.getBrightness() % 256;

            item.setExtraData(item.getExtraData().split(":")[0] + ":" + hue + ":" + saturation + ":" + brightness);
            item.setSqlUpdateNeeded(true);
            Emulator.getThreading().run(item);
            room.updateItem(item);
        }
    }
}
