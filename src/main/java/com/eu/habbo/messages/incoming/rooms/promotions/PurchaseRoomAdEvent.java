package com.eu.habbo.messages.incoming.rooms.promotions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.constants.RoomRightLevels;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.PurchaseErrorMessageComposer;
import com.eu.habbo.messages.outgoing.catalog.PurchaseOKMessageComposer;
import com.eu.habbo.messages.outgoing.navigator.UserEventCatsComposer;
import com.eu.habbo.messages.outgoing.rooms.promotions.RoomEventComposer;

public class PurchaseRoomAdEvent extends MessageHandler {
    public static String ROOM_PROMOTION_BADGE = "RADZZ";

    @Override
    public void handle() {
        int pageId = this.packet.readInt();
        int itemId = this.packet.readInt();
        int roomId = this.packet.readInt();
        String title = this.packet.readString();
        boolean extendedPromotion = this.packet.readBoolean();
        String description = this.packet.readString();
        int categoryId = this.packet.readInt();

        if (UserEventCatsComposer.CATEGORIES.stream().noneMatch(c -> c.getId() == categoryId))
            return;

        CatalogPage page = Emulator.getGameEnvironment().getCatalogManager().getCatalogPage(pageId);

        if (page == null || !page.getLayout().equals("roomads"))
            return;

        CatalogItem item = page.getCatalogItem(itemId);
        if (item != null) {
            if (this.client.getHabbo().getHabboInfo().canBuy(item)) {
                Room room = Emulator.getGameEnvironment().getRoomManager().getActiveRoomById(roomId);

                if (!(room.getRoomInfo().isRoomOwner(this.client.getHabbo()) || room.getRoomRightsManager().hasRights(this.client.getHabbo()) || room.getGuildRightLevel(this.client.getHabbo()).equals(RoomRightLevels.GUILD_ADMIN))) {
                    return;
                }

                if (room.getRoomPromotionManager().isPromoted()) {
                    room.getRoomPromotionManager().getPromotion().addEndTimestamp(120 * 60);
                } else {
                    room.getRoomPromotionManager().createPromotion(title, description, categoryId);
                }

                if (room.getRoomPromotionManager().isPromoted()) {
                    if (!this.client.getHabbo().hasPermissionRight(Permission.ACC_INFINITE_CREDITS)) {
                        this.client.getHabbo().giveCredits(-item.getCredits());
                    }

                    if (!this.client.getHabbo().hasPermissionRight(Permission.ACC_INFINITE_POINTS)) {
                        this.client.getHabbo().givePoints(item.getPointsType(), -item.getPoints());
                    }

                    this.client.sendResponse(new PurchaseOKMessageComposer());
                    room.sendComposer(new RoomEventComposer(room, room.getRoomPromotionManager().getPromotion()).compose());

                    if (!this.client.getHabbo().getInventory().getBadgesComponent().hasBadge(PurchaseRoomAdEvent.ROOM_PROMOTION_BADGE)) {
                        this.client.getHabbo().addBadge(PurchaseRoomAdEvent.ROOM_PROMOTION_BADGE);
                    }
                } else {
                    this.client.sendResponse(new PurchaseErrorMessageComposer(PurchaseErrorMessageComposer.SERVER_ERROR));
                }
            }
        }
    }
}
