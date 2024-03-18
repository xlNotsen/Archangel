package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RequestSpamWallPostItMessageComposer extends MessageComposer {
    private final RoomItem item;

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.requestSpamWallPostItMessageComposer);
        this.response.appendInt(this.item == null ? -1234 : this.item.getId());
        this.response.appendString("");
        return this.response;
    }
}
