package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.navigator.GuestRoomSearchResultComposer;

public class MyGuildBasesSearchEvent extends MessageHandler {
    @Override
    public void handle() {
        this.client.sendResponse(new GuestRoomSearchResultComposer(Emulator.getGameEnvironment().getRoomManager().getRoomsInGroup()));
    }
}
