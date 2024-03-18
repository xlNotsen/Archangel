package com.eu.habbo.messages.incoming.rooms.bots;

import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

public class PlaceBotEvent extends MessageHandler {
    @Override
    public void handle() {
        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room == null) {
            return;
        }

        Bot bot = this.client.getHabbo().getInventory().getBotsComponent().getBot(this.packet.readInt());

        if (bot == null) {
            return;
        }

        int x = this.packet.readInt();
        int y = this.packet.readInt();

        room.getRoomUnitManager().getRoomBotManager().placeBot(bot, this.client.getHabbo(), x, y);
    }
}
