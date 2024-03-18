package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ActivityPointsMessageComposer extends MessageComposer {

    private final Habbo habbo;


    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.activityPointsMessageComposer);
        String[] pointsTypes = Emulator.getConfig().getValue("seasonal.types").split(";");
        this.response.appendInt(pointsTypes.length);
        for (String s : pointsTypes) {
            int type;
            try {
                type = Integer.parseInt(s);
            } catch (Exception e) {
                log.error("Caught exception", e);
                return null;
            }

            this.response.appendInt(type);
            this.response.appendInt(this.habbo.getHabboInfo().getCurrencyAmount(type));
        }
        return this.response;
    }
}
