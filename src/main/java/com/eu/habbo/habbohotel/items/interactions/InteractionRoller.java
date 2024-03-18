package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.RoomItemManager;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.math3.util.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class InteractionRoller extends RoomItem {
    public static boolean NO_RULES = false;
    public static final int DELAY = 400;

    public InteractionRoller(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.setExtraData("0");
    }

    public InteractionRoller(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
        this.setExtraData("0");
    }

    @Override
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt((this.isLimited() ? 256 : 0));
        serverMessage.appendString(this.getExtraData());

        super.serializeExtradata(serverMessage);
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return true;
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        super.onClick(client, room, objects);
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {

    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOn(roomUnit, room, objects);
    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOff(roomUnit, room, objects);
    }


    @Override
    public boolean canStackAt(List<Pair<RoomTile, THashSet<RoomItem>>> itemsAtLocation) {
        if (NO_RULES) return true;
        if (itemsAtLocation.isEmpty()) return false;

        for (Pair<RoomTile, THashSet<RoomItem>> set : itemsAtLocation) {
            if (set.getValue() != null && !set.getValue().isEmpty()) {
                if (set.getValue().size() > 1) {
                    return false;
                } else if (!set.getValue().contains(this)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void removeThisItem(RoomItemManager roomItemManager) {
        synchronized (roomItemManager.getRollers()) {
            roomItemManager.getRollers().remove(getId());
        }
    }

    @Override
    public void addThisItem(RoomItemManager roomItemManager) {
        synchronized (roomItemManager.getRollers()) {
            roomItemManager.getRollers().put(getId(), this);
        }
    }
}
