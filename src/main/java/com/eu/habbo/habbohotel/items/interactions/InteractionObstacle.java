package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.ICycleable;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.pets.HorsePet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.constants.RoomTileState;
import com.eu.habbo.habbohotel.rooms.constants.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.entities.RoomRotation;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class InteractionObstacle extends RoomItem implements ICycleable {

    private final THashSet<RoomTile> middleTiles;

    public InteractionObstacle(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.setExtraData("0");
        this.middleTiles = new THashSet<>();
    }

    public InteractionObstacle(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
        this.setExtraData("0");
        this.middleTiles = new THashSet<>();
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
        /*Pet pet = room.getPet(roomUnit);

        if (pet instanceof HorsePet && ((HorsePet) pet).getRider() != null) {
            if (pet.getTask() != null && pet.getTask().equals(PetTasks.RIDE)) {
                if (pet.getRoomUnit().hasStatus(RoomUnitStatus.JUMP)) {
                    pet.getRoomUnit().removeStatus(RoomUnitStatus.JUMP);
                    Emulator.getThreading().run(new HabboItemNewState(this, room, "0"), 2000);
                } else {
                    int state = 0;
                    for (int i = 0; i < 2; i++) {
                        state = Emulator.getRandom().nextInt(4) + 1;

                        if (state == 4)
                            break;
                    }

                    this.setExtradata(state + "");
                    pet.getRoomUnit().setStatus(RoomUnitStatus.JUMP, "0");

                    AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("HorseConsecutiveJumpsCount"));
                    AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("HorseJumping"));
                }

                room.updateItemState(this);
            }
        }*/
    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOn(roomUnit, room, objects);

        Habbo habbo = room.getRoomUnitManager().getHabboByRoomUnit(roomUnit);

        if (habbo == null) {
            Pet pet = room.getRoomUnitManager().getRoomPetManager().getPetByRoomUnit(roomUnit);

            if (pet instanceof HorsePet && ((HorsePet) pet).getRider() != null) {
                if (roomUnit.getBodyRotation().getValue() % 2 == 0) {
                    if (this.getRotation() == 2) {
                        if (roomUnit.getBodyRotation().equals(RoomRotation.WEST)) {
                            ((HorsePet) pet).getRider().getRoomUnit().walkTo(room.getLayout().getTile((short) (roomUnit.getCurrentPosition().getX() - 3), roomUnit.getCurrentPosition().getY()));
                        } else if (roomUnit.getBodyRotation().equals(RoomRotation.EAST)) {
                            ((HorsePet) pet).getRider().getRoomUnit().walkTo(room.getLayout().getTile((short) (roomUnit.getCurrentPosition().getX() + 3), roomUnit.getCurrentPosition().getY()));
                        }
                    } else if (this.getRotation() == 4) {
                        if (roomUnit.getBodyRotation().equals(RoomRotation.NORTH)) {
                            ((HorsePet) pet).getRider().getRoomUnit().walkTo(room.getLayout().getTile(roomUnit.getCurrentPosition().getX(), (short) (roomUnit.getCurrentPosition().getY() - 3)));
                        } else if (roomUnit.getBodyRotation().equals(RoomRotation.SOUTH)) {
                            ((HorsePet) pet).getRider().getRoomUnit().walkTo(room.getLayout().getTile(roomUnit.getCurrentPosition().getX(), (short) (roomUnit.getCurrentPosition().getY() + 3)));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOff(roomUnit, room, objects);

        Habbo habbo = room.getRoomUnitManager().getHabboByRoomUnit(roomUnit);

        if (habbo == null) {
            Pet pet = room.getRoomUnitManager().getRoomPetManager().getPetByRoomUnit(roomUnit);

            if (pet instanceof HorsePet && ((HorsePet) pet).getRider() != null) {
                pet.getRoomUnit().removeStatus(RoomUnitStatus.JUMP);
            }
        }
    }

    @Override
    public void onPlace(Room room) {
        super.onPlace(room);
        this.calculateMiddleTiles(room);
    }

    @Override
    public void onPickUp(Room room) {
        super.onPickUp(room);
        middleTiles.clear();
    }

    @Override
    public void onMove(Room room, RoomTile oldLocation, RoomTile newLocation) {
        super.onMove(room, oldLocation, newLocation);
        this.calculateMiddleTiles(room);
    }

    private void calculateMiddleTiles(Room room) {
        middleTiles.clear();

        if(this.getRotation() == 2) {
            middleTiles.add(room.getLayout().getTile((short)(this.getCurrentPosition().getX() + 1), this.getCurrentPosition().getY()));
            middleTiles.add(room.getLayout().getTile((short)(this.getCurrentPosition().getX() + 1), (short)(this.getCurrentPosition().getY() + 1)));
        }
        else if(this.getRotation() == 4) {
            middleTiles.add(room.getLayout().getTile(this.getCurrentPosition().getX(), (short)(this.getCurrentPosition().getY() + 1)));
            middleTiles.add(room.getLayout().getTile((short)(this.getCurrentPosition().getX() + 1), (short)(this.getCurrentPosition().getY() + 1)));
        }
    }

    @Override
    public RoomTileState getOverrideTileState(RoomTile tile, Room room) {
        if(this.middleTiles.contains(tile))
            return RoomTileState.BLOCKED;

        return null;
    }

    @Override
    public void cycle(Room room) {
        if(this.middleTiles.size() == 0) {
            this.calculateMiddleTiles(room);
        }

        for(RoomTile tile : this.middleTiles) {
            for(RoomUnit roomUnit : room.getRoomUnitManager().getRoomUnitsAt(tile)) {
                if(roomUnit == null) {
                    continue;
                }

                if(roomUnit.getPath().size() == 0 && !roomUnit.hasStatus(RoomUnitStatus.MOVE)) {
                    if(roomUnit.getBodyRotation().getValue() != this.getRotation() && Objects.requireNonNull(roomUnit.getBodyRotation().getOpposite()).getValue() != this.getRotation())
                        continue;

                    RoomTile tileInfront = room.getLayout().getTileInFront(roomUnit.getCurrentPosition(), roomUnit.getBodyRotation().getValue());
                    if(tileInfront.getState() != RoomTileState.INVALID && tileInfront.getState() != RoomTileState.BLOCKED && room.getRoomUnitManager().getRoomUnitsAt(tileInfront).size() == 0) {
                        roomUnit.walkTo(tileInfront);
                    }
                    else {
                        RoomTile tileBehind = room.getLayout().getTileInFront(roomUnit.getCurrentPosition(), Objects.requireNonNull(roomUnit.getBodyRotation().getOpposite()).getValue());
                        if(tileBehind.getState() != RoomTileState.INVALID && tileBehind.getState() != RoomTileState.BLOCKED && room.getRoomUnitManager().getRoomUnitsAt(tileBehind).size() == 0) {
                            roomUnit.walkTo(tileBehind);
                        }
                    }
                }
            }
        }
    }
}
