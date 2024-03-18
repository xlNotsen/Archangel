package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.RoomItemManager;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.constants.RoomTileState;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.messages.outgoing.rooms.items.ObjectDataUpdateMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.items.ObjectsMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class InteractionMuteArea extends InteractionCustomValues {
    public static final THashMap<String, String> defaultValues = new THashMap<>(
            Map.of(
                "tilesLeft", "0",
                    "tilesRight", "0",
                    "tilesFront", "0",
                    "tilesBack", "0",
                    "state", "0"
            )
    );

    private final THashSet<RoomTile> tiles;

    public InteractionMuteArea(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem, defaultValues);
        tiles = new THashSet<>();
    }

    public InteractionMuteArea(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells, defaultValues);
        tiles = new THashSet<>();
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        super.onClick(client, room, objects);

        if((objects.length >= 2 && objects[1] instanceof WiredEffectType) || (client != null && room.getRoomRightsManager().hasRights(client.getHabbo()))) {
            this.values.put("state", this.values.get("state").equals("0") ? "1" : "0");
            room.sendComposer(new ObjectDataUpdateMessageComposer(this).compose());
        }
    }

    @Override
    public void onPlace(Room room) {
        super.onPlace(room);
        this.regenAffectedTiles(room);
    }

    @Override
    public void onPickUp(Room room) {
        super.onPickUp(room);
        this.tiles.clear();
    }

    @Override
    public void onMove(Room room, RoomTile oldLocation, RoomTile newLocation) {
        super.onMove(room, oldLocation, newLocation);
        this.regenAffectedTiles(room);
    }

    public boolean inSquare(RoomTile location) {
        Room room = this.getRoom();

        if(!this.values.get("state").equals("1"))
            return false;

        if(room != null && this.tiles.size() == 0) {
            regenAffectedTiles(room);
        }

        return this.tiles.contains(location);

        /*try {
            return new Rectangle(
                    this.getX() - Integer.parseInt(this.values.get("tilesBack")),
                    this.getY() + Integer.parseInt(this.values.get("tilesLeft")) - (Integer.parseInt(this.values.get("tilesLeft")) + Integer.parseInt(this.values.get("tilesRight"))),
                    Integer.parseInt(this.values.get("tilesLeft")) + Integer.parseInt(this.values.get("tilesRight")) + 1,
                    Integer.parseInt(this.values.get("tilesFront")) + Integer.parseInt(this.values.get("tilesBack")) + 1).contains(location.x, location.y);
        } catch (Exception e) {
            return false;
        }*/
    }

    private void regenAffectedTiles(Room room) {
        int minX = Math.max(0, this.getCurrentPosition().getX() - Integer.parseInt(this.values.get("tilesBack")));
        int minY = Math.max(0, this.getCurrentPosition().getY() - Integer.parseInt(this.values.get("tilesRight")));
        int maxX = Math.min(room.getLayout().getMapSizeX(), this.getCurrentPosition().getX() + Integer.parseInt(this.values.get("tilesFront")));
        int maxY = Math.min(room.getLayout().getMapSizeY(), this.getCurrentPosition().getY() + Integer.parseInt(this.values.get("tilesLeft")));

        this.tiles.clear();

        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                RoomTile tile = room.getLayout().getTile((short)x, (short)y);
                if(tile != null && tile.getState() != RoomTileState.INVALID)
                    this.tiles.add(tile);
            }
        }
    }

    @Override
    public void onCustomValuesSaved(Room room, GameClient client, THashMap<String, String> oldValues) {
        super.onCustomValuesSaved(room, client, oldValues);

        this.regenAffectedTiles(room);

        // show the effect
        Item effectItem = Emulator.getGameEnvironment().getItemManager().getItem("mutearea_sign2");

        if(effectItem != null) {
            TIntObjectMap<String> ownerNames = TCollections.synchronizedMap(new TIntObjectHashMap<>(0));
            ownerNames.put(-1, "System");
            THashSet<RoomItem> items = new THashSet<>();

            int id = 0;
            for(RoomTile tile : this.tiles) {
                id--;
                RoomItem item = new InteractionDefault(id, null, effectItem, "1", 0, 0);

                item.setCurrentPosition(tile);
                item.setCurrentZ(tile.relativeHeight());
                items.add(item);
            }

            client.sendResponse(new ObjectsMessageComposer(ownerNames, items));
            Emulator.getThreading().run(() -> {
                for(RoomItem item : items) {
                    client.sendResponse(new RemoveFloorItemComposer(item, true));
                }
            }, 3000);
        }
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
