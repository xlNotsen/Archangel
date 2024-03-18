package com.eu.habbo.messages.incoming.hotelview;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.hotelview.SecondsUntilMessageComposer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class HotelViewRequestSecondsUntilEvent extends MessageHandler {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");

    @Override
    public void handle() throws Exception {
        String date = this.packet.readString();
        int secondsUntil = Math.max(0, (int) (dateFormat.parse(date).getTime() / 1000) - Emulator.getIntUnixTimestamp());

        this.client.sendResponse(new SecondsUntilMessageComposer(date, secondsUntil));
    }
}
