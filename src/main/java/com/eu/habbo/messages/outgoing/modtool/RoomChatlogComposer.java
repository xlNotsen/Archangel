package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.habbohotel.modtool.ModToolChatLog;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import lombok.AllArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@AllArgsConstructor
public class RoomChatlogComposer extends MessageComposer {
    private final Room room;
    private final ArrayList<ModToolChatLog> chatlog;

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.roomChatlogComposer);
        this.response.appendByte(1);
        this.response.appendShort(2);
        this.response.appendString("roomName");
        this.response.appendByte(2);
        this.response.appendString(this.room.getRoomInfo().getName());
        this.response.appendString("roomId");
        this.response.appendByte(1);
        this.response.appendInt(this.room.getRoomInfo().getId());

        SimpleDateFormat formatDate = new SimpleDateFormat("HH:mm");

        this.response.appendShort(this.chatlog.size());
        for (ModToolChatLog line : this.chatlog) {
            this.response.appendString(formatDate.format(new Date((line.getTimestamp() * 1000L))));
            this.response.appendInt(line.getHabboId());
            this.response.appendString(line.getUsername());
            this.response.appendString(line.getMessage());
            this.response.appendBoolean(false);
        }
        return this.response;
    }
}
