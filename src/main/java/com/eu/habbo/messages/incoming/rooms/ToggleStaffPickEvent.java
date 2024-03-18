package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.navigation.NavigatorPublicCategory;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.GetGuestRoomResultComposer;

public class ToggleStaffPickEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        if (this.client.getHabbo().hasPermissionRight(Permission.ACC_STAFF_PICK)) {
            int roomId = this.packet.readInt();

            Room room = Emulator.getGameEnvironment().getRoomManager().getActiveRoomById(roomId);

            if (room != null) {
                boolean staffPromotedRoom = !room.getRoomInfo().isStaffPicked();
                room.getRoomInfo().setStaffPicked(staffPromotedRoom);
                room.setNeedsUpdate(true);

                NavigatorPublicCategory publicCategory = Emulator.getGameEnvironment().getNavigatorManager().publicCategories.get(Emulator.getConfig().getInt("hotel.navigator.staffpicks.categoryid"));
                if (room.getRoomInfo().isStaffPicked()) {
                    Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(room.getRoomInfo().getOwnerInfo().getId());

                    if (habbo != null) {
                        AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("Spr"));
                    }

                    if (publicCategory != null) {
                        publicCategory.addRoom(room);
                    }
                } else {
                    if (publicCategory != null) {
                        publicCategory.removeRoom(room);
                    }
                }

                this.client.sendResponse(new GetGuestRoomResultComposer(room, this.client.getHabbo(), true, false));
            }
        }
    }
}
