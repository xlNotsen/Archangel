package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Slf4j
public class InteractionGift extends RoomItem {
    public boolean explode = false;
    private int[] itemId;
    @Getter
    private int colorId = 0;
    @Getter
    private int ribbonId = 0;
    private boolean showSender = false;
    private String message = "";
    private String sender = "";
    private String look = "";

    public InteractionGift(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);

        try {
            this.loadData();
        } catch (Exception e) {
            log.warn("Incorrect extradata for gift with ID " + this.getId());
        }
    }

    public InteractionGift(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);

        try {
            this.loadData();
        } catch (Exception e) {
            log.warn("Incorrect extradata for gift with ID " + this.getId());
        }
    }

    @Override
    public void serializeExtradata(ServerMessage serverMessage) {
        //serverMessage.appendInt(this.colorId * 1000 + this.ribbonId);
        serverMessage.appendInt(1);
        serverMessage.appendInt(6);
        serverMessage.appendString("EXTRA_PARAM");
        serverMessage.appendString("");
        serverMessage.appendString("MESSAGE");
        serverMessage.appendString(this.message);
        serverMessage.appendString("PURCHASER_NAME");
        serverMessage.appendString(this.showSender ? this.sender : "");
        serverMessage.appendString("PURCHASER_FIGURE");
        serverMessage.appendString(this.showSender ? this.look : "");
        serverMessage.appendString("PRODUCT_CODE");
        serverMessage.appendString(""); //this.gift.getItemId()
        serverMessage.appendString("state");
        serverMessage.appendString(this.explode ? "1" : "0");

        super.serializeExtradata(serverMessage);
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) {

    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return false;
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {

    }

    private void loadData() throws NumberFormatException {
        String[] data = null;

        if (this.getExtraData().contains("\t"))
            data = this.getExtraData().split("\t");

        if (data != null && data.length >= 5) {
            int count = Integer.parseInt(data[0]);

            this.itemId = new int[count];

            for (int i = 0; i < count; i++) {
                this.itemId[i] = Integer.parseInt(data[i + 1]);
            }

            this.colorId = Integer.parseInt(data[count + 1]);
            this.ribbonId = Integer.parseInt(data[count + 2]);
            this.showSender = data[count + 3].equalsIgnoreCase("1");
            this.message = data[count + 4];

            if (data.length - count >= 7 && this.showSender) {
                this.sender = data[count + 5];
                this.look = data[count + 6];
            }
        } else {
            this.itemId = new int[0];
            this.colorId = 0;
            this.ribbonId = 0;
            this.showSender = false;
            this.message = "Please delete this present. Thanks!";
        }
    }

    public HashSet<RoomItem> loadItems() {
        HashSet<RoomItem> items = new HashSet<>();
        for (int anItemId : this.itemId) {
            if (anItemId == 0)
                continue;

            items.add(Emulator.getGameEnvironment().getItemManager().loadHabboItem(anItemId));
        }

        return items;
    }
}
