package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.rooms.constants.RoomUserAction;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExpressionMessageComposer extends MessageComposer {
    private final RoomUnit roomUnit;
    private final RoomUserAction action;

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.expressionMessageComposer);
        this.response.appendInt(this.roomUnit.getVirtualId());
        this.response.appendInt(this.action.getAction());
        return this.response;
    }
}
