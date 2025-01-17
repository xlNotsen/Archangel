package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.HabboInfo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionRedeemableSubscriptionBox extends InteractionCrackable {
    public InteractionRedeemableSubscriptionBox(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionRedeemableSubscriptionBox(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    public boolean userRequiredToBeAdjacent() {
        return false;
    }
}
