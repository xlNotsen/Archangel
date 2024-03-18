package com.eu.habbo.habbohotel.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionMultiHeight;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.list.array.TIntArrayList;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Item implements ISerialize {
    @Getter
    private int id;
    @Getter
    private int spriteId;
    @Getter
    private String name;
    @Getter
    private String fullName;
    @Getter
    private FurnitureType type;
    @Getter
    private short width;
    @Getter
    private short length;
    @Getter
    private double height;
    private boolean allowStack;
    private boolean allowWalk;
    private boolean allowSit;
    private boolean allowLay;
    private boolean allowRecycle;
    private boolean allowTrade;
    private boolean allowMarketplace;
    private boolean allowGift;
    private boolean allowInventoryStack;
    @Getter
    private short stateCount;
    @Getter
    private short effectM;
    @Getter
    private short effectF;
    @Getter
    private TIntArrayList vendingItems;
    @Getter
    private double[] multiHeights;
    @Getter
    private String customParams;
    @Getter
    private String clothingOnWalk;
    @Getter
    private ItemInteraction interactionType;
    @Getter
    private int rotations;

    public Item(ResultSet set) throws SQLException {
        this.load(set);
    }

    public static boolean isPet(Item item) {
        return item.getName().toLowerCase().startsWith("a0 pet");
    }

    public static double getCurrentHeight(RoomItem item) {
        if (item instanceof InteractionMultiHeight && item.getBaseItem().getMultiHeights().length > 0) {
            if (item.getExtraData().isEmpty()) {
                item.setExtraData("0");
            }

            try {
                int index = Integer.parseInt(item.getExtraData()) % (item.getBaseItem().getMultiHeights().length);
                return item.getBaseItem().getMultiHeights()[(item.getExtraData().isEmpty() ? 0 : index)];
            } catch (NumberFormatException ignored) {

            }
        }

        return item.getBaseItem().getHeight();
    }

    public void update(ResultSet set) throws SQLException {
        this.load(set);
    }

    private void load(ResultSet set) throws SQLException {
        this.id = set.getInt("id");
        this.spriteId = set.getInt("sprite_id");
        this.name = set.getString("item_name");
        this.fullName = set.getString("public_name");
        this.type = FurnitureType.fromString(set.getString("type"));
        this.width = set.getShort("width");
        this.length = set.getShort("length");
        this.height = set.getDouble("stack_height");
        if (this.height == 0) {
            this.height = 1e-6;
        }
        this.allowStack = set.getBoolean("allow_stack");
        this.allowWalk = set.getBoolean("allow_walk");
        this.allowSit = set.getBoolean("allow_sit");
        this.allowLay = set.getBoolean("allow_lay");
        this.allowRecycle = set.getBoolean("allow_recycle");
        this.allowTrade = set.getBoolean("allow_trade");
        this.allowMarketplace = set.getBoolean("allow_marketplace_sell");
        this.allowGift = set.getBoolean("allow_gift");
        this.allowInventoryStack = set.getBoolean("allow_inventory_stack");

        this.interactionType = Emulator.getGameEnvironment().getItemManager().getItemInteraction(set.getString("interaction_type").toLowerCase());

        this.stateCount = set.getShort("interaction_modes_count");
        this.effectM = set.getShort("effect_id_male");
        this.effectF = set.getShort("effect_id_female");
        this.customParams = set.getString("customparams");
        this.clothingOnWalk = set.getString("clothing_on_walk");

        if (!set.getString("vending_ids").isEmpty()) {
            this.vendingItems = new TIntArrayList();
            String[] vendingIds = set.getString("vending_ids").replace(";", ",").split(",");
            for (String s : vendingIds) {
                this.vendingItems.add(Integer.parseInt(s.replace(" ", "")));
            }
        }

        //if(this.interactionType.getType() == InteractionMultiHeight.class || this.interactionType.getType().isAssignableFrom(InteractionMultiHeight.class))
        {
            if (set.getString("multiheight").contains(";")) {
                String[] s = set.getString("multiheight").split(";");
                this.multiHeights = new double[s.length];

                for (int i = 0; i < s.length; i++) {
                    this.multiHeights[i] = Double.parseDouble(s[i]);
                }
            } else {
                this.multiHeights = new double[0];
            }
        }

        this.rotations = 4;

        try {
            this.rotations = set.getInt("rotations");
        }
        catch (SQLException ignored) { }
    }

    public boolean allowStack() {
        return this.allowStack;
    }

    public boolean allowWalk() {
        return this.allowWalk;
    }

    public boolean allowSit() {
        return this.allowSit;
    }

    public boolean allowLay() {
        return this.allowLay;
    }

    public boolean allowRecyle() {
        return this.allowRecycle;
    }

    public boolean allowTrade() {
        return this.allowTrade;
    }

    public boolean allowMarketplace() {
        return this.allowMarketplace;
    }

    public boolean allowGift() {
        return this.allowGift;
    }

    public boolean allowInventoryStack() {
        return this.allowInventoryStack;
    }

    public int getRandomVendingItem() {
        return this.vendingItems.get(Emulator.getRandom().nextInt(this.vendingItems.size()));
    }

    @Override
    public void serialize(ServerMessage message) {
        message.appendString(this.type.code.toLowerCase());

        if (type == FurnitureType.BADGE) {
            message.appendString(this.customParams);
        } else {
            message.appendInt(this.spriteId);

            if (this.getName().contains("wallpaper_single") || this.getName().contains("floor_single") || this.getName().contains("landscape_single")) {
                message.appendString(this.name.split("_")[2]);
            } else if (type == FurnitureType.ROBOT) {
                message.appendString(this.customParams);
            } else if (name.equalsIgnoreCase("poster")) {
                message.appendString(this.customParams);
            } else if (name.startsWith("SONG ")) {
                message.appendString(this.customParams);
            } else {
                message.appendString("");
            }

            message.appendInt(1); // productCount
            message.appendBoolean(false);
        }
    }
}
