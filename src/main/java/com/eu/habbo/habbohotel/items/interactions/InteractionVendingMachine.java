package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.constants.RoomTileState;
import com.eu.habbo.habbohotel.rooms.constants.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.entities.RoomRotation;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.rooms.entities.units.types.RoomAvatar;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.RoomUnitGiveHanditem;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import com.eu.habbo.util.pathfinding.Rotation;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InteractionVendingMachine extends RoomItem {
    public InteractionVendingMachine(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.setExtraData("0");
    }

    public InteractionVendingMachine(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
        this.setExtraData("0");
    }
    
    public THashSet<RoomTile> getActivatorTiles(Room room) {
        THashSet<RoomTile> tiles = new THashSet<>();
        RoomTile tileInFront = getSquareInFront(room.getLayout(), this);

        if (tileInFront != null)
            tiles.add(tileInFront);

        tiles.add(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()));
        return tiles;
    }

    @Override
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt((this.isLimited() ? 256 : 0));
        serverMessage.appendString(this.getExtraData());

        super.serializeExtradata(serverMessage);
    }

    private void tryInteract(GameClient client, Room room, RoomUnit unit) {
        THashSet<RoomTile> activatorTiles = getActivatorTiles(room);

        if(activatorTiles.size() == 0)
            return;

        boolean inActivatorSpace = false;

        for(RoomTile ignored : activatorTiles) {
            if(unit.getCurrentPosition().is(unit.getCurrentPosition().getX(), unit.getCurrentPosition().getY())) {
                inActivatorSpace = true;
            }
        }

        if(inActivatorSpace) {
            useVendingMachine(client, room, unit);
        }
    }

    private void useVendingMachine(GameClient client, Room room, RoomUnit unit) {
        if(!(unit instanceof RoomAvatar roomAvatar)) {
            return;
        }

        this.setExtraData("1");
        room.updateItem(this);

        try {
            super.onClick(client, room, new Object[]{"TOGGLE_OVERRIDE"});
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!roomAvatar.isWalking() && !roomAvatar.hasStatus(RoomUnitStatus.SIT) && !roomAvatar.hasStatus(RoomUnitStatus.LAY)) {
            this.rotateToMachine(roomAvatar);
        }

        Emulator.getThreading().run(() -> {

            giveVendingMachineItem(room, roomAvatar);

            if (this.getBaseItem().getEffectM() > 0 && client.getHabbo().getHabboInfo().getGender() == HabboGender.M)
                client.getHabbo().getRoomUnit().giveEffect(this.getBaseItem().getEffectM(), -1);
            if (this.getBaseItem().getEffectF() > 0 && client.getHabbo().getHabboInfo().getGender() == HabboGender.F)
                client.getHabbo().getRoomUnit().giveEffect(this.getBaseItem().getEffectF(), -1);

            Emulator.getThreading().run(this, 500);
        }, 1500);
    }

    public void giveVendingMachineItem(Room room, RoomAvatar roomAvatar) {
        Emulator.getThreading().run(new RoomUnitGiveHanditem(roomAvatar, room, this.getBaseItem().getRandomVendingItem()));
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) {
        if (client == null) {
            return;
        }

        RoomUnit unit = client.getHabbo().getRoomUnit();

        THashSet<RoomTile> activatorTiles = getActivatorTiles(room);

        if(activatorTiles.size() == 0)
            return;

        boolean inActivatorSpace = false;

        for(RoomTile tile : activatorTiles) {
            if(unit.getCurrentPosition().is(tile.getX(), tile.getY())) {
                inActivatorSpace = true;
            }
        }

        if(!inActivatorSpace) {
            RoomTile tileToWalkTo = null;
            for(RoomTile tile : activatorTiles) {
                if((tile.getState() == RoomTileState.OPEN || tile.getState() == RoomTileState.SIT) && (tileToWalkTo == null || tileToWalkTo.distance(unit.getCurrentPosition()) > tile.distance(unit.getCurrentPosition()))) {
                    tileToWalkTo = tile;
                }
            }

            if(tileToWalkTo != null) {
                List<Runnable> onSuccess = new ArrayList<>();
                List<Runnable> onFail = new ArrayList<>();

                onSuccess.add(() -> tryInteract(client, room, unit));

                unit.walkTo(tileToWalkTo);
                Emulator.getThreading().run(new RoomUnitWalkToLocation(unit, tileToWalkTo, room, onSuccess, onFail));
            }
        }
        else {
            useVendingMachine(client, room, unit);
        }
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {

    }

    @Override
    public void run() {
        super.run();
        if (this.getExtraData().equals("1")) {
            this.setExtraData("0");
            Room room = Emulator.getGameEnvironment().getRoomManager().getActiveRoomById(this.getRoomId());
            if (room != null) {
                room.updateItem(this);
            }
        }
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return true;
    }

    @Override
    public boolean isWalkable() {
        return this.getBaseItem().allowWalk();
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    private void rotateToMachine(RoomUnit unit) {
        RoomRotation rotation = RoomRotation.values()[Rotation.Calculate(unit.getCurrentPosition().getX(), unit.getCurrentPosition().getY(), this.getCurrentPosition().getX(), this.getCurrentPosition().getY())];

        if(Math.abs(unit.getBodyRotation().getValue() - rotation.getValue()) > 1) {
            unit.setRotation(rotation);
            unit.setStatusUpdateNeeded(true);
        }
    }
}
