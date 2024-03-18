package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class InteractionSwitchRemoteControl extends InteractionDefault {
    public InteractionSwitchRemoteControl(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionSwitchRemoteControl(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean canToggle(Habbo habbo, Room room) {
        return RoomLayout.tilesAdjecent(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), habbo.getRoomUnit().getCurrentPosition());
    }

    @Override
    public boolean allowWiredResetState() {
        return true;
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        if (room != null) {
            super.onClick(client, room, objects);

                    if (this.getExtraData().length() == 0)
                        this.setExtraData("0");

                    if (this.getBaseItem().getStateCount() > 0) {
                        int currentState = 0;

                        try {
                            currentState = Integer.parseInt(this.getExtraData());
                        } catch (NumberFormatException e) {
                            log.error("Incorrect extradata (" + this.getExtraData() + ") for item ID (" + this.getId() + ") of type (" + this.getBaseItem().getName() + ")");
                        }

                        this.setExtraData("" + (currentState + 1) % this.getBaseItem().getStateCount());
                        this.setSqlUpdateNeeded(true);

                        room.updateItemState(this);
                    }
        }
    }
}