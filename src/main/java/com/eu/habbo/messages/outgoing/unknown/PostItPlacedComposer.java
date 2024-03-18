package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PostItPlacedComposer extends MessageComposer {
    private final RoomItem item;
    private final int unknownInt;

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.postItPlacedComposer);
        this.response.appendInt(this.item.getId());
        this.response.appendInt(this.unknownInt);
        return this.response;
    }
}