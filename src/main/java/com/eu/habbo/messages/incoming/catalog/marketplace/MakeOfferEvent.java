package com.eu.habbo.messages.incoming.catalog.marketplace;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.marketplace.MarketPlace;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.PurchaseErrorMessageComposer;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceMakeOfferResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MakeOfferEvent extends MessageHandler {

    @Override
    public void handle() {
        if (!MarketPlace.MARKETPLACE_ENABLED) {
            this.client.sendResponse(new MarketplaceMakeOfferResult(MarketplaceMakeOfferResult.MARKETPLACE_DISABLED));
            return;
        }

        int credits = this.packet.readInt();

        int unknown = this.packet.readInt();
        int itemId = this.packet.readInt();

        RoomItem item = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(itemId);
        if (item != null) {
            if (!item.getBaseItem().allowMarketplace()) {
                String message = Emulator.getTexts().getValue("scripter.warning.marketplace.forbidden").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%itemname%", item.getBaseItem().getName()).replace("%credits%", credits + "");
                ScripterManager.scripterDetected(this.client, message);
                log.info(message);
                this.client.sendResponse(new PurchaseErrorMessageComposer(PurchaseErrorMessageComposer.SERVER_ERROR));
                return;
            }

            if (credits < 0) {
                String message = Emulator.getTexts().getValue("scripter.warning.marketplace.negative").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%itemname%", item.getBaseItem().getName()).replace("%credits%", credits + "");
                ScripterManager.scripterDetected(this.client, message);
                log.info(message);
                this.client.sendResponse(new PurchaseErrorMessageComposer(PurchaseErrorMessageComposer.SERVER_ERROR));
                return;
            }

            if (MarketPlace.sellItem(this.client, item, credits)) {
                this.client.sendResponse(new MarketplaceMakeOfferResult(MarketplaceMakeOfferResult.POST_SUCCESS));
            } else {
                this.client.sendResponse(new MarketplaceMakeOfferResult(MarketplaceMakeOfferResult.FAILED_TECHNICAL_ERROR));
            }
        }
    }
}
