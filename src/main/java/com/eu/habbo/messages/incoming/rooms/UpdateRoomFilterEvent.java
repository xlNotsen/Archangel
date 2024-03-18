package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

public class UpdateRoomFilterEvent extends MessageHandler {
    @Override
    public void handle() {
        final int roomId = this.packet.readInt();
        final boolean add = this.packet.readBoolean();
        String word = this.packet.readString();

        if (word.length() > 25) {
            word = word.substring(0, 24);
        }

        // Get current room of user.
        final Room room = this.client.getHabbo().getRoomUnit().getRoom();
        if (room == null || room.getRoomInfo().getId() != roomId) {
            return;
        }

        // Check if owner.
        if (!room.getRoomInfo().isRoomOwner(this.client.getHabbo())) {
            ScripterManager.scripterDetected(this.client, String.format("User (%s) tried to change wordfilter for a not owned room.", this.client.getHabbo().getHabboInfo().getUsername()));
            return;
        }

        // Modify word filter.
        if (add) {
            room.getRoomWordFilterManager().addWord(word);
        } else {
            room.getRoomWordFilterManager().removeWord(word);
        }
    }
}
