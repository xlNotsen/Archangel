package com.eu.habbo.messages.incoming.floorplaneditor;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.*;
import com.eu.habbo.habbohotel.rooms.constants.RoomTileState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.generic.alerts.HabboBroadcastMessageComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.NotificationDialogMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomForwardMessageComposer;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

public class UpdateFloorPropertiesEvent extends MessageHandler {
    public static int MAXIMUM_FLOORPLAN_WIDTH_LENGTH = 64;
    public static int MAXIMUM_FLOORPLAN_SIZE = 64 * 64;

    @Override
    public void handle() {
        if (!this.client.getHabbo().hasPermissionRight(Permission.ACC_FLOORPLAN_EDITOR)) {
            this.client.sendResponse(new HabboBroadcastMessageComposer(Emulator.getTexts().getValue("floorplan.permission")));
            return;
        }

        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if (room == null)
            return;

        if (room.getRoomInfo().getOwnerInfo().getId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermissionRight(Permission.ACC_ANYROOMOWNER)) {
            StringJoiner errors = new StringJoiner("<br />");
            String map = this.packet.readString();
            map = map.replace("X", "x");

            String[] mapRows = map.split("\r");

            int firstRowSize = mapRows[0].length();

            if (Emulator.getConfig().getBoolean("hotel.room.floorplan.check.enabled")) {
                if (!map.matches("[a-zA-Z0-9\r]+")) errors.add("${notification.floorplan_editor.error.title}");

                Arrays.stream(mapRows)
                        .filter(line -> line.length() != firstRowSize)
                        .findAny()
                        .ifPresent(s -> errors.add("(General): Line " + (Arrays.asList(mapRows).indexOf(s) + 1) + " is of different length than line 1"));

                if (map.isEmpty() || map.replace("x", "").replace("\r", "").isEmpty()) {
                    errors.add("${notification.floorplan_editor.error.message.effective_height_is_0}");
                }

                if (map.length() > MAXIMUM_FLOORPLAN_SIZE) {
                    errors.add("${notification.floorplan_editor.error.message.too_large_area}");
                }

                if (mapRows.length > MAXIMUM_FLOORPLAN_WIDTH_LENGTH) errors.add("${notification.floorplan_editor.error.message.too_large_height}");
                else if (Arrays.stream(mapRows).anyMatch(l -> l.length() > MAXIMUM_FLOORPLAN_WIDTH_LENGTH || l.length() == 0)) errors.add("${notification.floorplan_editor.error.message.too_large_width}");

                if (errors.length() > 0) {
                    this.client.sendResponse(new NotificationDialogMessageComposer(BubbleAlertKeys.FLOORPLAN_EDITOR_ERROR.getKey(), errors.toString()));
                    return;
                }
            }

            int doorX = this.packet.readInt();
            int doorY = this.packet.readInt();

            if (doorX < 0 || doorX > firstRowSize || doorY < 0 || doorY >= mapRows.length) {
                errors.add("${notification.floorplan_editor.error.message.entry_tile_outside_map}");
            }

            if (doorY < mapRows.length && doorX < mapRows[doorY].length() && mapRows[doorY].charAt(doorX) == 'x') {
                errors.add("${notification.floorplan_editor.error.message.entry_not_on_tile}");
            }

            int doorRotation = this.packet.readInt();
            if (doorRotation < 0 || doorRotation > 7) {
                errors.add("${notification.floorplan_editor.error.message.invalid_entry_tile_direction}");
            }

            int wallSize = this.packet.readInt();
            if (wallSize < -2 || wallSize > 1) {
                errors.add("${notification.floorplan_editor.error.message.invalid_wall_thickness}");
            }
            int floorSize = this.packet.readInt();
            if (floorSize < -2 || floorSize > 1) {
                errors.add("${notification.floorplan_editor.error.message.invalid_floor_thickness}");
            }

            int wallHeight = -1;
            if (this.packet.bytesAvailable() >= 4)
                wallHeight = this.packet.readInt();

            if (wallHeight < -1 || wallHeight > 15) {
                errors.add("${notification.floorplan_editor.error.message.invalid_walls_fixed_height}");
            }

            THashSet<RoomTile> locked_tileList = room.getLockedTiles();
            THashSet<RoomTile> new_tileList = new THashSet<>();
            blockingRoomItemScan:
            for (int y = 0; y < mapRows.length; y++) {
                for (int x = 0; x < firstRowSize; x++) {

                    RoomTile tile = room.getLayout().getTile((short) x, (short) y);
                    new_tileList.add(tile);
                    String square = String.valueOf(mapRows[y].charAt(x));
                    short height;

                    if (square.equalsIgnoreCase("x") && room.getRoomItemManager().getTopItemAt(x, y) != null) {
                        errors.add("${notification.floorplan_editor.error.message.change_blocked_by_room_item}");
                        break blockingRoomItemScan;
                    } else {
                        if (square.isEmpty()) {
                            height = 0;
                        } else if (Emulator.isNumeric(square)) {
                            height = Short.parseShort(square);
                        } else {
                            height = (short) (10 + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(square.toUpperCase()));
                        }
                    }

                    if (tile != null && tile.getState() != RoomTileState.INVALID && height != tile.getZ() && room.getRoomItemManager().getTopItemAt(x, y) != null) {
                        errors.add("${notification.floorplan_editor.error.message.change_blocked_by_room_item}");
                        break blockingRoomItemScan;
                    }
                }
            }

            locked_tileList.removeAll(new_tileList);
            if (!locked_tileList.isEmpty()) {
                errors.add("${notification.floorplan_editor.error.message.change_blocked_by_room_item}");
            }



            if (errors.length() > 0) {
                this.client.sendResponse(new NotificationDialogMessageComposer(BubbleAlertKeys.FLOORPLAN_EDITOR_ERROR.getKey(), errors.toString()));
                return;
            }

            RoomLayout layout = room.getLayout();

            if (layout instanceof CustomRoomLayout) {
                layout.setDoorX((short) doorX);
                layout.setDoorY((short) doorY);
                layout.setDoorDirection(doorRotation);
                layout.setHeightmap(map);
                layout.parse();

                if (layout.getDoorTile() == null) {
                    this.client.getHabbo().alert("Error");
                    ((CustomRoomLayout) layout).needsUpdate(false);
                    room.dispose();
                    return;
                }
                ((CustomRoomLayout) layout).needsUpdate(true);
                Emulator.getThreading().run((CustomRoomLayout) layout);
            } else {
                layout = Emulator.getGameEnvironment().getRoomManager().insertCustomLayout(room, map, doorX, doorY, doorRotation);
            }

            if (layout != null) {
                room.getRoomInfo().setModelOverridden(true);
                room.setNeedsUpdate(true);
                room.setLayout(layout);
                room.getRoomInfo().setWallThickness(wallSize);
                room.getRoomInfo().setFloorThickness(floorSize);
                room.getRoomInfo().setWallHeight(wallHeight);
                room.save();
                Collection<Habbo> habbos = new ArrayList<>(room.getRoomUnitManager().getRoomHabbosCount());
                habbos.addAll(room.getRoomUnitManager().getCurrentHabbos().values());
                room.dispose();
                room = Emulator.getGameEnvironment().getRoomManager().getRoom(room.getRoomInfo().getId());
                ServerMessage message = new RoomForwardMessageComposer(room.getRoomInfo().getId()).compose();
                for (Habbo habbo : habbos) {
                    habbo.getClient().sendResponse(message);
                }
            }
        }
    }
}
