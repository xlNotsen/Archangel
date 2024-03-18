package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.AccountPreferencesComposer;
import com.eu.habbo.messages.outgoing.users.UserObjectComposer;
import com.eu.habbo.messages.outgoing.users.UserPerksComposer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class InfoRetrieveEvent extends MessageHandler {


    @Override
    public void handle() {
        if (this.client.getHabbo() != null) {
            //this.client.sendResponse(new TestComposer());

            //this.client.sendResponse(new UserDataComposer(this.client.getHabbo()));
            //this.client.sendResponse(new HotelViewComposer());
            //this.client.sendResponse(new UserHomeRoomComposer());
            //this.client.sendResponse(new UserPermissionsComposer(this.client.getHabbo()));

            //this.client.sendResponse(new UserCreditsComposer(this.client.getHabbo()));
            //this.client.sendResponse(new UserCurrencyComposer(this.client.getHabbo()));
            //this.client.sendResponse(new FavoriteRoomsCountComposer());

            //this.client.sendResponse(new UserAchievementScoreComposer(this.client.getHabbo()));
            //this.client.sendResponse(new UserClothesComposer());
            //this.client.sendResponse(new GenericAlertComposer(Emulator.getTexts().getValue("hotel.alert.message.welcome").replace("%user%", this.client.getHabbo().getHabboInfo().getUsername()), this.client.getHabbo()));


            //

            ArrayList<ServerMessage> messages = new ArrayList<>();


            messages.add(new UserObjectComposer(this.client.getHabbo()).compose());
            messages.add(new UserPerksComposer(this.client.getHabbo()).compose());

            messages.add(new AccountPreferencesComposer(this.client.getHabbo()).compose());


//
//

//
//
//


            this.client.sendResponses(messages);


        } else {
            log.debug("Attempted to request user data where Habbo was null.");
            Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
        }
    }
}
