package com.eu.habbo.messages.outgoing.trading;

import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.rooms.trades.RoomTrade;
import com.eu.habbo.habbohotel.rooms.trades.RoomTradeUser;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TradingItemListComposer extends MessageComposer {
    private final RoomTrade roomTrade;

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.tradingItemListComposer);
        for (RoomTradeUser roomTradeUser : this.roomTrade.getRoomTradeUsers()) {
            this.response.appendInt(roomTradeUser.getUserId());

            this.response.appendInt(roomTradeUser.getItems().size());
            for (RoomItem item : roomTradeUser.getItems()) {
                this.response.appendInt(item.getId());
                this.response.appendString(item.getBaseItem().getType().code);
                this.response.appendInt(item.getId());
                this.response.appendInt(item.getBaseItem().getSpriteId());
                this.response.appendInt(0);
                this.response.appendBoolean(item.getBaseItem().allowInventoryStack() && !item.isLimited());
                item.serializeExtradata(this.response);
                this.response.appendInt(0);
                this.response.appendInt(0);
                this.response.appendInt(0);

                if (item.getBaseItem().getType() == FurnitureType.FLOOR)
                    this.response.appendInt(0);
            }

            this.response.appendInt(roomTradeUser.getItems().size());
            this.response.appendInt(roomTradeUser.getItems().stream().mapToInt(RoomTrade::getCreditsByItem).sum());
        }
        return this.response;
    }
}
