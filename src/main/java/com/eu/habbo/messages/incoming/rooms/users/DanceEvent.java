package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.DanceType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.DanceMessageComposer;
import com.eu.habbo.plugin.events.users.UserIdleEvent;

public class DanceEvent extends MessageHandler {
    @Override
    public void handle() {
        if (this.client.getHabbo().getRoomUnit().getRoom() == null)
            return;

        int danceId = this.packet.readInt();
        if (danceId >= 0 && danceId <= 5) {
            if (this.client.getHabbo().getRoomUnit().isInRoom()) {

                Habbo habbo = this.client.getHabbo();

                if (this.client.getHabbo().getRoomUnit().getCacheable().get("control") != null) {
                    habbo = (Habbo) this.client.getHabbo().getRoomUnit().getCacheable().get("control");

                    if (habbo.getRoomUnit().getRoom() != this.client.getHabbo().getRoomUnit().getRoom()) {
                        habbo.getRoomUnit().getCacheable().remove("controller");
                        this.client.getHabbo().getRoomUnit().getCacheable().remove("control");
                        habbo = this.client.getHabbo();
                    }
                }

                habbo.getRoomUnit().setDanceType(DanceType.values()[danceId]);

                UserIdleEvent event = new UserIdleEvent(this.client.getHabbo(), UserIdleEvent.IdleReason.DANCE, false);
                Emulator.getPluginManager().fireEvent(event);

                if (!event.isCancelled()) {
                    if (!event.isIdle()) {
                        this.client.getHabbo().getRoomUnit().unIdle();
                    }
                }

                this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new DanceMessageComposer(habbo.getRoomUnit()).compose());
            }
        }
    }
}
