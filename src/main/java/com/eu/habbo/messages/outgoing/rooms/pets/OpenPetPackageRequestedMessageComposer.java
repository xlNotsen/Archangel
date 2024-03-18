package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OpenPetPackageRequestedMessageComposer extends MessageComposer {
    private final RoomItem item;


    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.openPetPackageRequestedMessageComposer);
        this.response.appendInt(this.item.getId());
        return this.response;
    }
}