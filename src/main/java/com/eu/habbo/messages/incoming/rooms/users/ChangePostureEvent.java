package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserIdleEvent;

public class ChangePostureEvent extends MessageHandler {
    @Override
    public void handle() {
        if (this.client.getHabbo().getRoomUnit().getRoom() != null) {
            if (this.client.getHabbo().getRoomUnit().isWalking()) {
                this.client.getHabbo().getRoomUnit().stopWalking();
            }

            this.client.getHabbo().getRoomUnit().makeSit();

            UserIdleEvent event = new UserIdleEvent(this.client.getHabbo(), UserIdleEvent.IdleReason.WALKED, false);
            Emulator.getPluginManager().fireEvent(event);

            if (!event.isCancelled()) {
                if (!event.isIdle()) {
                    this.client.getHabbo().getRoomUnit().unIdle();
                }
            }
        }
    }
}
