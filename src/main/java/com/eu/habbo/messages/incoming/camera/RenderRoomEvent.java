package com.eu.habbo.messages.incoming.camera;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.networking.camera.CameraClient;
import com.eu.habbo.networking.camera.messages.outgoing.CameraRenderImageComposer;
import com.eu.habbo.util.crypto.ZIP;

public class RenderRoomEvent extends MessageHandler {
    @Override
    public void handle() {
        if (!this.client.getHabbo().hasPermissionRight(Permission.ACC_CAMERA)) {
            this.client.getHabbo().alert(Emulator.getTexts().getValue("camera.permission"));
            return;
        }

        if (CameraClient.isLoggedIn) {
            this.packet.getBuffer().readFloat();

            byte[] data = this.packet.getBuffer().readBytes(this.packet.getBuffer().readableBytes()).array();

            String content = new String(ZIP.inflate(data));
            CameraRenderImageComposer composer = new CameraRenderImageComposer(this.client.getHabbo().getHabboInfo().getId(), this.client.getHabbo().getRoomUnit().getRoom().getBackgroundTonerColor().getRGB(), 320, 320, content);
            this.client.getHabbo().getHabboInfo().setPhotoJSON(Emulator.getConfig().getValue("camera.extradata").replace("%timestamp%", composer.timestamp + ""));
            this.client.getHabbo().getHabboInfo().setPhotoTimestamp(composer.timestamp);

            if (this.client.getHabbo().getRoomUnit().getRoom() != null) {
                this.client.getHabbo().getHabboInfo().setPhotoRoomId(this.client.getHabbo().getRoomUnit().getRoom().getRoomInfo().getId());
            }

            Emulator.getCameraClient().sendMessage(composer);
        } else {
            this.client.getHabbo().alert(Emulator.getTexts().getValue("camera.disabled"));
        }

    }
}
