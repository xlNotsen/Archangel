package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.wired.interfaces.IWiredEffectInteraction;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import gnu.trove.set.hash.THashSet;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class InteractionWiredEffect extends InteractionWired implements IWiredEffectInteraction {
    @Getter
    @Setter
    private List<Integer> blockedTriggers;

    @Getter
    @Setter
    private WiredEffectType type;

    public InteractionWiredEffect(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionWiredEffect(int id, HabboInfo ownerInfo, Item item, String extraData, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extraData, limitedStack, limitedSells);
    }

    public List<Integer> getBlockedTriggers(Room room) {
        List<Integer> blockedTriggers = new ArrayList<>();
        THashSet<InteractionWiredTrigger> triggers = room.getRoomSpecialTypes().getTriggers(this.getCurrentPosition().getX(), this.getCurrentPosition().getY());

        for(InteractionWiredTrigger trigger : triggers) {
            if(!trigger.isTriggeredByRoomUnit()) {
                blockedTriggers.add(trigger.getBaseItem().getSpriteId());
            }
        }

        return blockedTriggers;
    }

    public boolean requiresTriggeringUser() {
        return false;
    }
}
