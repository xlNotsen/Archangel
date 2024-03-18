package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.CrackableReward;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.subscriptions.SubscriptionHabboClub;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.CrackableExplode;
import com.eu.habbo.util.pathfinding.Rotation;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionCrackable extends RoomItem {
    private final Object lock = new Object();
    public boolean cracked = false;
    protected int ticks = 0;

    public InteractionCrackable(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionCrackable(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void serializeExtradata(ServerMessage serverMessage) {
        if (this.getExtraData().length() == 0)
            this.setExtraData("0");

        serverMessage.appendInt(7 + (this.isLimited() ? 256 : 0));

        serverMessage.appendString(Emulator.getGameEnvironment().getItemManager().calculateCrackState(Integer.parseInt(this.getExtraData()), Emulator.getGameEnvironment().getItemManager().getCrackableCount(this.getBaseItem().getId()), this.getBaseItem()) + "");
        serverMessage.appendInt(Integer.parseInt(this.getExtraData()));
        serverMessage.appendInt(Emulator.getGameEnvironment().getItemManager().getCrackableCount(this.getBaseItem().getId()));

        super.serializeExtradata(serverMessage);
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return true;
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        if (client == null) {
            return;
        }

        super.onClick(client, room, objects);
        synchronized (this.lock) {
            if (this.getRoom() == null)
                return;

            if (this.cracked)
                return;

            if (this.userRequiredToBeAdjacent() && client.getHabbo().getRoomUnit().getCurrentPosition().distance(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY())) > 1.5) {
                client.getHabbo().getRoomUnit().walkTo(room.getLayout().getTileInFront(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), Rotation.Calculate(client.getHabbo().getRoomUnit().getCurrentPosition().getX(), client.getHabbo().getRoomUnit().getCurrentPosition().getY(), this.getCurrentPosition().getX(), this.getCurrentPosition().getY())));
                return;
            }

            if (this.getExtraData().length() == 0)
                this.setExtraData("0");

            if (this.getBaseItem().getEffectF() > 0)
                if (client.getHabbo().getHabboInfo().getGender().equals(HabboGender.F) && this.getBaseItem().getEffectF() == client.getHabbo().getRoomUnit().getEffectId())
                    return;

            if (this.getBaseItem().getEffectM() > 0)
                if (client.getHabbo().getHabboInfo().getGender().equals(HabboGender.M) && this.getBaseItem().getEffectM() == client.getHabbo().getRoomUnit().getEffectId())
                    return;

            this.onTick(client.getHabbo(), room);
        }
    }

    public void onTick(Habbo habbo, Room room) {
        if (this.cracked) return;

        if (this.allowAnyone() || this.getOwnerInfo().getId() == habbo.getHabboInfo().getId()) {
            CrackableReward rewardData = Emulator.getGameEnvironment().getItemManager().getCrackableData(this.getBaseItem().getId());

            if (rewardData != null) {
                if (rewardData.getRequiredEffect() > 0 && habbo.getRoomUnit().getEffectId() != rewardData.getRequiredEffect())
                    return;

                if(this.ticks < 1)
                {
                    // If there are no ticks (for example because the room has been reloaded), check the current extradata of the item and update the ticks.
                    this.ticks = Integer.parseInt(this.getExtraData());
                }
                this.ticks++;
                this.setExtraData("" + (this.ticks));
                this.setSqlUpdateNeeded(true);
                room.updateItem(this);

                if (!rewardData.getAchievementTick().isEmpty()) {
                    AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement(rewardData.getAchievementTick()));
                }
                if (!this.cracked && this.ticks == Emulator.getGameEnvironment().getItemManager().getCrackableCount(this.getBaseItem().getId())) {
                    this.cracked = true;
                    Emulator.getThreading().run(new CrackableExplode(room, this, habbo, !this.placeInRoom(), this.getCurrentPosition()), 1500);

                    if (!rewardData.getAchievementCracked().isEmpty()) {
                        AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement(rewardData.getAchievementCracked()));
                    }

                    if (rewardData.getSubscriptionType() != null && rewardData.getSubscriptionDuration() > 0) {
                        // subscriptions are given immediately upon cracking
                        switch (rewardData.getSubscriptionType()) {
                            case HABBO_CLUB ->
                                    habbo.getHabboStats().createSubscription(SubscriptionHabboClub.HABBO_CLUB, rewardData.getSubscriptionDuration() * 86400);
                            case BUILDERS_CLUB ->
                                    habbo.getHabboStats().createSubscription("BUILDERS_CLUB", rewardData.getSubscriptionDuration() * 86400);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {

    }

    @Override
    public void onWalkOn(RoomUnit client, Room room, Object[] objects) {

    }

    @Override
    public void onWalkOff(RoomUnit client, Room room, Object[] objects) {

    }

    public boolean allowAnyone() {
        return false;
    }

    protected boolean placeInRoom() {
        return true;
    }

    public boolean resetable() {
        return false;
    }

    public boolean userRequiredToBeAdjacent() {
        return true;
    }

    public void reset(Room room) {
        this.cracked = false;
        this.ticks = 0;
        this.setExtraData("0");
        room.updateItem(this);
    }

    @Override
    public boolean isUsable() {
        return false;
    }
}
