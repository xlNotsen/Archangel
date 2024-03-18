package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.RoomItemManager;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.constants.RoomTileState;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.outgoing.rooms.items.ObjectsMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class InteractionBuildArea extends InteractionCustomValues {
    protected static final THashMap<String, String> defaultValues = new THashMap<>();

    private final THashSet<RoomTile> tiles;

    public InteractionBuildArea(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem, defaultValues);
        defaultValues.put("tilesLeft", "0");
        defaultValues.put("tilesRight", "0");
        defaultValues.put("tilesFront", "0");
        defaultValues.put("tilesBack", "0");
        defaultValues.put("builders", "");

        tiles = new THashSet<>();
    }

    public InteractionBuildArea(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells, defaultValues);
        defaultValues.put("tilesLeft", "0");
        defaultValues.put("tilesRight", "0");
        defaultValues.put("tilesFront", "0");
        defaultValues.put("tilesBack", "0");
        defaultValues.put("builders", "");

        tiles = new THashSet<>();
    }

    @Override
    public void onPlace(Room room) {
        super.onPlace(room);
        this.regenAffectedTiles(room);
    }

    @Override
    public void onPickUp(Room room) {
        super.onPickUp(room);

        ArrayList<String> builderNames = new ArrayList<>(Arrays.asList(this.values.get("builders").split(";")));
        THashSet<Integer> canBuild = new THashSet<>();

        for (String builderName : builderNames) {
            Habbo builder = Emulator.getGameEnvironment().getHabboManager().getHabbo(builderName);
            HabboInfo builderInfo;
            if (builder != null) {
                builderInfo = builder.getHabboInfo();
            } else {
                builderInfo = Emulator.getGameEnvironment().getHabboManager().getOfflineHabboInfo(builderName);
            }
            if (builderInfo != null) {
                canBuild.add(builderInfo.getId());
            }
        }

        if (!canBuild.isEmpty()) {
            for (RoomTile tile : this.tiles) {
                THashSet<RoomItem> tileItems = room.getRoomItemManager().getItemsAt(tile);
                for (RoomItem tileItem : tileItems) {
                    if (canBuild.contains(tileItem.getOwnerInfo().getId()) && tileItem != this) {
                        room.getRoomItemManager().pickUpItem(tileItem, null);
                    }
                }
            }
        }

        this.tiles.clear();
    }

    @Override
    public void onMove(Room room, RoomTile oldLocation, RoomTile newLocation) {
        super.onMove(room, oldLocation, newLocation);

        ArrayList<String> builderNames = new ArrayList<>(Arrays.asList(this.values.get("builders").split(";")));
        THashSet<Integer> canBuild = new THashSet<>();

        for (String builderName : builderNames) {
            Habbo builder = Emulator.getGameEnvironment().getHabboManager().getHabbo(builderName);
            HabboInfo builderInfo;
            if (builder != null) {
                builderInfo = builder.getHabboInfo();
            } else {
                builderInfo = Emulator.getGameEnvironment().getHabboManager().getOfflineHabboInfo(builderName);
            }
            if (builderInfo != null) {
                canBuild.add(builderInfo.getId());
            }
        }

        THashSet<RoomTile> newTiles = new THashSet<>();

        int minX = Math.max(0, newLocation.getX() - Integer.parseInt(this.values.get("tilesBack")));
        int minY = Math.max(0, newLocation.getY() - Integer.parseInt(this.values.get("tilesRight")));
        int maxX = Math.min(room.getLayout().getMapSizeX(), newLocation.getX() + Integer.parseInt(this.values.get("tilesFront")));
        int maxY = Math.min(room.getLayout().getMapSizeY(), newLocation.getY() + Integer.parseInt(this.values.get("tilesLeft")));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                RoomTile tile = room.getLayout().getTile((short) x, (short) y);
                if (tile != null && tile.getState() != RoomTileState.INVALID)
                    newTiles.add(tile);
            }
        }

        if (!canBuild.isEmpty()) {
            for (RoomTile tile : this.tiles) {
                THashSet<RoomItem> tileItems = room.getRoomItemManager().getItemsAt(tile);
                if (newTiles.contains(tile)) continue;
                for (RoomItem tileItem : tileItems) {
                    if (canBuild.contains(tileItem.getOwnerInfo().getId()) && tileItem != this) {
                        room.getRoomItemManager().pickUpItem(tileItem, null);
                    }
                }
            }
        }
        this.regenAffectedTiles(room);
    }

    public boolean inSquare(RoomTile location) {
        Room room = this.getRoom();

        if (room != null && this.tiles.size() == 0) {
            regenAffectedTiles(room);
        }

        return this.tiles.contains(location);

    }

    private void regenAffectedTiles(Room room) {
        int minX = Math.max(0, this.getCurrentPosition().getX() - Integer.parseInt(this.values.get("tilesBack")));
        int minY = Math.max(0, this.getCurrentPosition().getY() - Integer.parseInt(this.values.get("tilesRight")));
        int maxX = Math.min(room.getLayout().getMapSizeX(), this.getCurrentPosition().getX() + Integer.parseInt(this.values.get("tilesFront")));
        int maxY = Math.min(room.getLayout().getMapSizeY(), this.getCurrentPosition().getY() + Integer.parseInt(this.values.get("tilesLeft")));

        this.tiles.clear();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                RoomTile tile = room.getLayout().getTile((short) x, (short) y);
                if (tile != null && tile.getState() != RoomTileState.INVALID)
                    this.tiles.add(tile);
            }
        }
    }

    @Override
    public void onCustomValuesSaved(Room room, GameClient client, THashMap<String, String> oldValues) {
        regenAffectedTiles(room);
        ArrayList<String> builderNames = new ArrayList<>(Arrays.asList(this.values.get("builders").split(";")));
        THashSet<Integer> canBuild = new THashSet<>();

        for (String builderName : builderNames) {
            Habbo builder = Emulator.getGameEnvironment().getHabboManager().getHabbo(builderName);
            HabboInfo builderInfo;
            if (builder != null) {
                builderInfo = builder.getHabboInfo();
            } else {
                builderInfo = Emulator.getGameEnvironment().getHabboManager().getOfflineHabboInfo(builderName);
            }
            if (builderInfo != null) {
                canBuild.add(builderInfo.getId());
            }
        }

        THashSet<RoomTile> oldTiles = new THashSet<>();

        int minX = Math.max(0, this.getCurrentPosition().getX() - Integer.parseInt(oldValues.get("tilesBack")));
        int minY = Math.max(0, this.getCurrentPosition().getY() - Integer.parseInt(oldValues.get("tilesRight")));
        int maxX = Math.min(room.getLayout().getMapSizeX(), this.getCurrentPosition().getX() + Integer.parseInt(oldValues.get("tilesFront")));
        int maxY = Math.min(room.getLayout().getMapSizeY(), this.getCurrentPosition().getY() + Integer.parseInt(oldValues.get("tilesLeft")));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                RoomTile tile = room.getLayout().getTile((short) x, (short) y);
                if (tile != null && tile.getState() != RoomTileState.INVALID && !this.tiles.contains(tile))
                    oldTiles.add(tile);
            }
        }
        if (!canBuild.isEmpty()) {
            for (RoomTile tile : oldTiles) {
                THashSet<RoomItem> tileItems = room.getRoomItemManager().getItemsAt(tile);
                for (RoomItem tileItem : tileItems) {
                    if (canBuild.contains(tileItem.getOwnerInfo().getId()) && tileItem != this) {
                        room.getRoomItemManager().pickUpItem(tileItem, null);
                    }
                }
            }
        }

        // show the effect
        Item effectItem = Emulator.getGameEnvironment().getItemManager().getItem("mutearea_sign2");

        if (effectItem != null) {
            TIntObjectMap<String> ownerNames = TCollections.synchronizedMap(new TIntObjectHashMap<>(0));
            ownerNames.put(-1, "System");
            THashSet<RoomItem> items = new THashSet<>();

            int id = 0;
            for (RoomTile tile : this.tiles) {
                id--;
                RoomItem item = new InteractionDefault(id, null, effectItem, "1", 0, 0);

                item.setCurrentPosition(tile);
                item.setCurrentZ(tile.relativeHeight());
                items.add(item);
            }

            client.sendResponse(new ObjectsMessageComposer(ownerNames, items));
            Emulator.getThreading().run(() -> {
                for (RoomItem item : items) {
                    client.sendResponse(new RemoveFloorItemComposer(item, true));
                }
            }, 3000);
        }
    }

    public boolean isBuilder(String Username) {
        return Arrays.asList(this.values.get("builders").split(";")).contains(Username);
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
