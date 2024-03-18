package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionMoodLight;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomMoodlightData;
import com.eu.habbo.habbohotel.rooms.constants.RoomRightLevels;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.RoomDimmerPresetsComposer;
import gnu.trove.map.TIntObjectMap;

import java.util.Arrays;
import java.util.List;

public class RoomDimmerSavePresetEvent extends MessageHandler {
    private static final List<String> MOODLIGHT_AVAILABLE_COLORS = Arrays.asList("#74F5F5,#0053F7,#E759DE,#EA4532,#F2F851,#82F349,#000000".split(","));
    private static final int MIN_BRIGHTNESS = (int) Math.floor(0.3 * 0xFF);

    @Override
    public void handle() {
        Room room = this.client.getHabbo().getRoomUnit().getRoom();

        if ((room.getRoomInfo().getGuild().getId() <= 0 && room.getGuildRightLevel(this.client.getHabbo()).isLessThan(RoomRightLevels.GUILD_RIGHTS)) && !room.getRoomRightsManager().hasRights(this.client.getHabbo()))
            return;

        int id = this.packet.readInt();
        int backgroundOnly = this.packet.readInt();
        String color = this.packet.readString();
        int brightness = this.packet.readInt();
        boolean apply = this.packet.readBoolean();

        if (Emulator.getConfig().getBoolean("moodlight.color_check.enabled", true) && !MOODLIGHT_AVAILABLE_COLORS.contains(color)) {
            ScripterManager.scripterDetected(this.client, "User tried to set a moodlight to a non-whitelisted color: " + color);
            return;
        }

        if (brightness > 0xFF || brightness < MIN_BRIGHTNESS) {
            ScripterManager.scripterDetected(this.client, "User tried to set a moodlight's brightness to out-of-bounds ([76, 255]): " + brightness);
            return;
        }

        for (RoomMoodlightData data : ((TIntObjectMap<RoomMoodlightData>) room.getRoomInfo().getMoodLightData()).valueCollection()) {
            if (data.getId() == id) {
                data.setBackgroundOnly(backgroundOnly == 2);
                data.setColor(color);
                data.setIntensity(brightness);
                if (apply) data.enable();

                for (RoomItem item : room.getRoomSpecialTypes().getItemsOfType(InteractionMoodLight.class)) {
                    item.setExtraData(data.toString());
                    item.setSqlUpdateNeeded(true);
                    room.updateItem(item);
                    Emulator.getThreading().run(item);
                }
            } else if (apply) {
                data.disable();
            }
        }

        room.setNeedsUpdate(true);
        this.client.sendResponse(new RoomDimmerPresetsComposer((TIntObjectMap<RoomMoodlightData>) room.getRoomInfo().getMoodLightData()));
    }
}
