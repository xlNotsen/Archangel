package com.eu.habbo.messages.outgoing.guild;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.set.hash.THashSet;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GuildCreationInfoMessageComposer extends MessageComposer {
    private final THashSet<Room> rooms;
    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.guildCreationInfoMessageComposer);
        this.response.appendInt(Emulator.getConfig().getInt("catalog.guild.price"));
        this.response.appendInt(this.rooms.size());

        for (Room room : this.rooms) {
            this.response.appendInt(room.getRoomInfo().getId());
            this.response.appendString(room.getRoomInfo().getName());
            this.response.appendBoolean(false);
        }

        this.response.appendInt(5);

        this.response.appendInt(10);
        this.response.appendInt(3);
        this.response.appendInt(4);

        this.response.appendInt(25);
        this.response.appendInt(17);
        this.response.appendInt(5);

        this.response.appendInt(25);
        this.response.appendInt(17);
        this.response.appendInt(3);

        this.response.appendInt(29);
        this.response.appendInt(11);
        this.response.appendInt(4);

        this.response.appendInt(0);
        this.response.appendInt(0);
        this.response.appendInt(0);
        return this.response;
    }
}
