package com.eu.habbo.habbohotel.items.interactions.games.freeze.gates;

import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.HabboInfo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionFreezeGateYellow extends InteractionFreezeGate {
    public static final GameTeamColors TEAM_COLOR = GameTeamColors.YELLOW;

    public InteractionFreezeGateYellow(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem, TEAM_COLOR);
    }

    public InteractionFreezeGateYellow(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells, TEAM_COLOR);
    }
}
