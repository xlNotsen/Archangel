package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredEffectTriggerStacks extends InteractionWiredEffect {
    public WiredEffectTriggerStacks(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectTriggerStacks(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if(this.getWiredSettings().getItemIds().isEmpty()) {
            return false;
        }

        if (stuff == null || (stuff.length >= 1 && stuff[stuff.length - 1] instanceof WiredEffectTriggerStacks)) {
            return false;
        }

        THashSet<RoomTile> usedTiles = new THashSet<>();

        boolean found;

        for (RoomItem item : this.getWiredSettings().getItems(room)) {
            //if(item instanceof InteractionWiredTrigger)
            {
                found = false;
                for (RoomTile tile : usedTiles) {
                    if (tile.getX() == item.getCurrentPosition().getX() && tile.getY() == item.getCurrentPosition().getY()) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    usedTiles.add(room.getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY()));
                }
            }
        }

        Object[] newStuff = new Object[stuff.length + 1];
        System.arraycopy(stuff, 0, newStuff, 0, stuff.length);
        newStuff[newStuff.length - 1] = this;
        WiredHandler.executeEffectsAtTiles(usedTiles, roomUnit, room, newStuff);

        return true;
    }

    @Override
    protected long requiredCooldown() {
        return 250;
    }

    @Override
    public WiredEffectType getType() {
        return WiredEffectType.CALL_STACKS;
    }
}
