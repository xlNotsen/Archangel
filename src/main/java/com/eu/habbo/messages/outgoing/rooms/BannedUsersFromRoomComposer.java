package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.infractions.RoomBan;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.set.hash.THashSet;
import lombok.AllArgsConstructor;

import java.util.NoSuchElementException;

@AllArgsConstructor
public class BannedUsersFromRoomComposer extends MessageComposer {
    private final Room room;

    @Override
    protected ServerMessage composeInternal() {
        int timeStamp = Emulator.getIntUnixTimestamp();

        THashSet<RoomBan> roomBans = new THashSet<>();

        TIntObjectIterator<RoomBan> iterator = this.room.getRoomInfractionManager().getBannedHabbos().iterator();

        for (int i = this.room.getRoomInfractionManager().getBannedHabbos().size(); i-- > 0; ) {
            try {
                iterator.advance();

                if (iterator.value().getEndTimestamp() > timeStamp)
                    roomBans.add(iterator.value());
            } catch (NoSuchElementException e) {
                break;
            }
        }

        if (roomBans.isEmpty())
            return null;

        this.response.init(Outgoing.bannedUsersFromRoomComposer);
        this.response.appendInt(this.room.getRoomInfo().getId());
        this.response.appendInt(roomBans.size());

        for (RoomBan ban : roomBans) {
            this.response.appendInt(ban.getUserId());
            this.response.appendString(ban.getUsername());
        }

        return this.response;
    }
}
