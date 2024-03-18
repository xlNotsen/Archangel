package com.eu.habbo.messages.outgoing.rooms.promotions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.promotions.RoomPromotion;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RoomEventComposer extends MessageComposer {
    private final Room room;
    private final RoomPromotion roomPromotion;

    @Override
    protected ServerMessage composeInternal() {

        this.response.init(Outgoing.roomEventComposer);

        if (this.room == null || this.roomPromotion == null) {
            this.response.appendInt(-1);
            this.response.appendInt(-1);
            this.response.appendString("");
            this.response.appendInt(0);
            this.response.appendInt(0);
            this.response.appendString("");
            this.response.appendString("");
            this.response.appendInt(0);
            this.response.appendInt(0);
            this.response.appendInt(0);
        } else {
            this.response.appendInt(this.room.getRoomInfo().getId()); // promotion id
            this.response.appendInt(this.room.getRoomInfo().getOwnerInfo().getId());
            this.response.appendString(this.room.getRoomInfo().getOwnerInfo().getUsername());

            this.response.appendInt(this.room.getRoomInfo().getId()); // room id
            this.response.appendInt(1); // "type"

            this.response.appendString(this.roomPromotion.getTitle());
            this.response.appendString(this.roomPromotion.getDescription());
            this.response.appendInt((Emulator.getIntUnixTimestamp() - this.roomPromotion.getStartTimestamp()) / 60); // minutes since starting
            this.response.appendInt((this.roomPromotion.getEndTimestamp() - Emulator.getIntUnixTimestamp()) / 60); // minutes until end
            this.response.appendInt(this.roomPromotion.getCategory()); // category
        }

        return this.response;

    }
}
