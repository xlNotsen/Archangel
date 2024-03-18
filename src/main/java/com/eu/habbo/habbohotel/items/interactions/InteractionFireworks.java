package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.RoomItemManager;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InteractionFireworks extends InteractionDefault {


    private static final String STATE_EMPTY = "0"; // Not used since the removal of pixels
    private static final String STATE_CHARGED = "1";
    private static final String STATE_EXPLOSION = "2";

    public InteractionFireworks(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionFireworks(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return false;
    }

    /**
     * Checked in Habbo on 2021-01-03
     * - Fireworks should be charged to be able to detonate them
     * - Habbos with Rights can detonate fireworks from anywhere in a room
     * - Habbos without rights have to walk to an adjecent tile to be able to detonate (see Interaction Switch)
     * - Wired can always detonate fireworks
     */
    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        if (room == null)
            return;

        // Wireds can always detonate fireworks if charged
        if (objects.length >= 2 && objects[1] instanceof WiredEffectType && objects[1] == WiredEffectType.TOGGLE_STATE) {
            if (this.getExtraData().equalsIgnoreCase(STATE_CHARGED)) {
                super.onClick(client, room, objects);

                if (this.getExtraData().equalsIgnoreCase(STATE_EXPLOSION)) {
                    this.reCharge(room);
                }
            }

            return;
        }

        if (client == null)
            return;

        // Habbos without rights have to walk to an adjecent tile to be able to detonate the fireworks
        if (!this.canToggle(client.getHabbo(), room)) {
            RoomTile closestTile = null;
            for (RoomTile tile : room.getLayout().getTilesAround(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()))) {
                if (tile.isWalkable() && (closestTile == null || closestTile.distance(client.getHabbo().getRoomUnit().getCurrentPosition()) > tile.distance(client.getHabbo().getRoomUnit().getCurrentPosition()))) {
                    closestTile = tile;
                }
            }

            if (closestTile != null && !closestTile.equals(client.getHabbo().getRoomUnit().getCurrentPosition())) {
                List<Runnable> onSuccess = new ArrayList<>();
                onSuccess.add(() -> {
                    try {
                        this.onClick(client, room, objects);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                client.getHabbo().getRoomUnit().walkTo(closestTile);
                Emulator.getThreading().run(new RoomUnitWalkToLocation(client.getHabbo().getRoomUnit(), closestTile, room, onSuccess, new ArrayList<>()));
            }
        }

        if (this.getExtraData().equalsIgnoreCase(STATE_CHARGED)) {
            super.onClick(client, room, objects);

            if (this.getExtraData().equalsIgnoreCase(STATE_EXPLOSION))
            {
                this.reCharge(room);
                AchievementManager.progressAchievement(client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("FireworksCharger"));
            }
        }
    }

    @Override
    public boolean allowWiredResetState() {
        return false;
    }

    @Override
    public void onPlace(Room room) {
        super.onPlace(room);
        this.setExtraData(STATE_CHARGED);
    }

    @Override
    public boolean canToggle(Habbo habbo, Room room) {
        return room.getRoomRightsManager().hasRights(habbo) || RoomLayout.tilesAdjecent(
                room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()),
                habbo.getRoomUnit().getCurrentPosition()
        );
    }

    private void reCharge(Room room) {
        // Default = 5000, Nuclear Firework should have 10000 in its custom params according to Habbo
        int explodeDuration = 5000;
        if (!this.getBaseItem().getCustomParams().isEmpty()) {
            try {
                explodeDuration = Integer.parseInt(this.getBaseItem().getCustomParams());
            } catch (NumberFormatException e) {
                log.error("Incorrect customparams (" + this.getBaseItem().getCustomParams() + ") for base item ID (" + this.getBaseItem().getId() + ") of type (" + this.getBaseItem().getName() + ")");
            }
        }

        Emulator.getThreading().run(() -> {
            this.setExtraData(STATE_CHARGED);
            this.setSqlUpdateNeeded(true);
            room.updateItemState(this);
        }, explodeDuration);
    }

    @Override
    public void removeThisItem(RoomItemManager roomItemManager) {
        synchronized (roomItemManager.getUndefinedSpecials()) {
            roomItemManager.getUndefinedSpecials().remove(getId());
        }
    }

    @Override
    public void addThisItem(RoomItemManager roomItemManager) {
        synchronized (roomItemManager.getUndefinedSpecials()) {
            roomItemManager.getUndefinedSpecials().put(getId(), this);
        }
    }
}
