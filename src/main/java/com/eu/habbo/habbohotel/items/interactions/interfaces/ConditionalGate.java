package com.eu.habbo.habbohotel.items.interactions.interfaces;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;

public interface ConditionalGate {
    void onRejected(RoomUnit roomUnit, Room room, Object[] objects);
}
