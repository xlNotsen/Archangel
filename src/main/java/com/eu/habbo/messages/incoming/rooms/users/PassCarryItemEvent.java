package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.threading.runnables.HabboGiveHandItemToHabbo;
import com.eu.habbo.threading.runnables.RoomUnitWalkToRoomUnit;

import java.util.ArrayList;
import java.util.List;

public class PassCarryItemEvent extends MessageHandler {
    @Override
    public void handle() {
        int userId = this.packet.readInt();

        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room != null) {
            Habbo target = room.getRoomUnitManager().getRoomHabboById(userId);

            if (target != null) {
                List<Runnable> executable = new ArrayList<>();
                executable.add(new HabboGiveHandItemToHabbo(this.client.getHabbo(), target));
                Emulator.getThreading().run(new RoomUnitWalkToRoomUnit(this.client.getHabbo().getRoomUnit(), target.getRoomUnit(), this.client.getHabbo().getRoomUnit().getRoom(), executable, executable));
            }
        }
    }
}
