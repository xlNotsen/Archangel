package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.InteractionBattleBanzaiSphere;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.outgoing.rooms.items.ItemsDataUpdateComposer;
import gnu.trove.set.hash.THashSet;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BattleBanzaiTilesFlicker implements Runnable {
    private final THashSet<RoomItem> items;
    private final GameTeamColors color;
    private final Room room;

    private boolean on = false;
    private int count = 0;


    @Override
    public void run() {
        if (this.items == null || this.room == null)
            return;

        int state = 0;
        if (this.on) {
            state = (this.color.type * 3) + 2;
            this.on = false;
        } else {
            this.on = true;
        }

        for (RoomItem item : this.items) {
            item.setExtraData(state + "");
        }

        this.room.sendComposer(new ItemsDataUpdateComposer(this.items).compose());

        if (this.count == 9) {
            for (RoomItem item : this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class)) {
                item.setExtraData("0");
                this.room.updateItemState(item);
            }
            return;
        }

        this.count++;

        Emulator.getThreading().run(this, 500);
    }
}
