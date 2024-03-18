package com.eu.habbo.habbohotel.items.interactions.totems;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.users.HabboInfo;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionTotemPlanet extends InteractionDefault {
    public InteractionTotemPlanet(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionTotemPlanet(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    public TotemPlanetType getPlanetType() {
        int extraData;
        try {
            extraData = Integer.parseInt(this.getExtraData());
        } catch(NumberFormatException ex) {
            extraData = 0;
        }
        return TotemPlanetType.fromInt(extraData);
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        if(client.getHabbo().getHabboInfo().getId() != this.getOwnerInfo().getId()) {
            super.onClick(client, room, objects);
            return;
        }

        InteractionTotemLegs legs = null;
        InteractionTotemHead head = null;

        RoomTile tile = room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY());
        THashSet<RoomItem> items = room.getRoomItemManager().getItemsAt(tile);

        for(RoomItem item : items) {
            if(item instanceof InteractionTotemLegs) {
                if (item.getCurrentZ() < this.getCurrentZ()) legs = (InteractionTotemLegs) item;
            }
        }

        if(legs == null) {
            super.onClick(client, room, objects);
            return;
        }

        for(RoomItem item : items) {
            if(item instanceof InteractionTotemHead) {
                if (item.getCurrentZ() > legs.getCurrentZ()) head = (InteractionTotemHead) item;
            }
        }

        if(head == null) {
            super.onClick(client, room, objects);
            return;
        }

        int effectId = 0;

        if(getPlanetType() == TotemPlanetType.SUN && head.getTotemType() == TotemType.BIRD && legs.getTotemType() == TotemType.BIRD && legs.getTotemColor() == TotemColor.RED) {
            effectId = 25;
        }
        else if(getPlanetType() == TotemPlanetType.EARTH && head.getTotemType() == TotemType.TROLL && legs.getTotemType() == TotemType.TROLL && legs.getTotemColor() == TotemColor.YELLOW) {
            effectId = 23;
        }
        else if(getPlanetType() == TotemPlanetType.EARTH && head.getTotemType() == TotemType.SNAKE && legs.getTotemType() == TotemType.BIRD && legs.getTotemColor() == TotemColor.YELLOW) {
            effectId = 26;
        }
        else if(getPlanetType() == TotemPlanetType.MOON && head.getTotemType() == TotemType.SNAKE && legs.getTotemType() == TotemType.SNAKE && legs.getTotemColor() == TotemColor.BLUE) {
            effectId = 24;
        }

        if(effectId > 0) {
            if(client.getHabbo().getInventory().getEffectsComponent().ownsEffect(effectId)) {
                client.getHabbo().getInventory().getEffectsComponent().enableEffect(effectId);
                return;
            }

            client.getHabbo().getInventory().getEffectsComponent().createEffect(effectId);
            client.getHabbo().getInventory().getEffectsComponent().enableEffect(effectId);
            return;
        }

        super.onClick(client, room, objects);
    }
}
