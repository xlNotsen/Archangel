package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.interfaces.InteractionWiredMatchFurniSettings;
import com.eu.habbo.habbohotel.rooms.constants.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.constants.RoomTileState;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredMatchFurniSetting;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WiredEffectMatchFurni extends InteractionWiredEffect implements InteractionWiredMatchFurniSettings {
    private final boolean checkForWiredResetPermission = true;
    public final int PARAM_STATE = 0;
    public final int PARAM_ROTATION = 1;
    public final int PARAM_POSITION = 2;
    @Getter
    private List<WiredMatchFurniSetting> matchSettings;

    public WiredEffectMatchFurni(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectMatchFurni(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if(this.getWiredSettings().getItemIds().isEmpty() && this.getWiredSettings().getMatchParams().isEmpty()) {
            return true;
        }

        boolean state = this.getWiredSettings().getIntegerParams().get(PARAM_STATE) == 1;
        boolean position = this.getWiredSettings().getIntegerParams().get(PARAM_POSITION) == 1;
        boolean rotation = this.getWiredSettings().getIntegerParams().get(PARAM_ROTATION) == 1;
        this.matchSettings = this.getWiredSettings().getMatchParams();

        for(RoomItem item : this.getWiredSettings().getItems(room)) {
            WiredMatchFurniSetting furniSettings = this.matchSettings.stream().filter(settings -> settings.getItem_id() == item.getId()).findAny().orElse(null);

            if(furniSettings == null) {
                continue;
            }

            if (state && (this.checkForWiredResetPermission && item.allowWiredResetState())) {
                if (!furniSettings.getState().equals(" ") && !item.getExtraData().equals(furniSettings.getState())) {
                    item.setExtraData(furniSettings.getState());
                    room.updateItemState(item);
                }
            }

            RoomTile oldLocation = room.getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY());
            double oldZ = item.getCurrentZ();

            if(rotation && !position) {
                if (item.getRotation() != furniSettings.getRotation() && room.getRoomItemManager().furnitureFitsAt(oldLocation, item, furniSettings.getRotation(), false) == FurnitureMovementError.NONE) {
                    room.getRoomItemManager().moveItemTo(item, oldLocation, furniSettings.getRotation(), null, true, true);
                }
            }
            else if(position) {
                boolean slideAnimation = !rotation || item.getRotation() == furniSettings.getRotation();
                RoomTile newLocation = room.getLayout().getTile((short) furniSettings.getX(), (short) furniSettings.getY());
                int newRotation = rotation ? furniSettings.getRotation() : item.getRotation();

                if (newLocation != null && newLocation.getState() != RoomTileState.INVALID && (newLocation != oldLocation || newRotation != item.getRotation()) && room.getRoomItemManager().furnitureFitsAt(newLocation, item, newRotation, true) == FurnitureMovementError.NONE) {
                    boolean sendUpdates = !slideAnimation;
                    if (room.getRoomItemManager().moveItemTo(item, newLocation, newRotation, null, sendUpdates, true) == FurnitureMovementError.NONE) {
                        if (slideAnimation) {
                            room.sendComposer(new FloorItemOnRollerComposer(item, null, oldLocation, oldZ, newLocation, item.getCurrentZ(), 0, room).compose());
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void loadDefaultIntegerParams() {
        if(this.getWiredSettings().getIntegerParams().isEmpty()) {
            this.getWiredSettings().getIntegerParams().add(0);
            this.getWiredSettings().getIntegerParams().add(0);
            this.getWiredSettings().getIntegerParams().add(0);
        }
    }

    @Override
    public void saveAdditionalData(Room room) {
        List<WiredMatchFurniSetting> matchSettings = new ArrayList<>();

        for (RoomItem item : this.getWiredSettings().getItems(room)) {
            WiredMatchFurniSetting settings = new WiredMatchFurniSetting(item.getId(), this.checkForWiredResetPermission && item.allowWiredResetState() ? item.getExtraData() : " ", item.getRotation(), item.getCurrentPosition().getX(), item.getCurrentPosition().getY());
            matchSettings.add(settings);
        }

        this.getWiredSettings().setMatchParams(matchSettings);
    }

    @Override
    public boolean shouldMatchState() {
        return this.getWiredSettings().getIntegerParams().get(PARAM_STATE) == 1;
    }

    @Override
    public boolean shouldMatchRotation() {
        return this.getWiredSettings().getIntegerParams().get(PARAM_ROTATION) == 1;
    }

    @Override
    public boolean shouldMatchPosition() {
        return this.getWiredSettings().getIntegerParams().get(PARAM_POSITION) == 1;
    }

    @Override
    public WiredEffectType getType() {
        return WiredEffectType.MATCH_SSHOT;
    }
}
