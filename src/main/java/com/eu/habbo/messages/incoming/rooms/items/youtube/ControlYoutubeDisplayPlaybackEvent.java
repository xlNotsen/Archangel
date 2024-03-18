package com.eu.habbo.messages.incoming.rooms.items.youtube;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionYoutubeTV;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.items.youtube.YoutubeControlVideoMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.items.youtube.YoutubeDisplayVideoMessageComposer;
import com.eu.habbo.threading.runnables.YoutubeAdvanceVideo;
import lombok.AllArgsConstructor;
import lombok.Getter;


public class ControlYoutubeDisplayPlaybackEvent extends YoutubeEvent {
    @Getter
    @AllArgsConstructor
    public enum YoutubeState {
        PREVIOUS(0),
        NEXT(1),
        PAUSE(2),
        RESUME(3);

        private final int state;

        public static YoutubeState getByState(int state) {
            return switch (state) {
                case 0 -> PREVIOUS;
                case 1 -> NEXT;
                case 2 -> PAUSE;
                case 3 -> RESUME;
                default -> null;
            };
        }
    }

    @Override
    public void handle() {
        int itemId = this.packet.readInt();
        YoutubeState state = YoutubeState.getByState(this.packet.readInt());

        if (state == null) return;

        Habbo habbo = this.client.getHabbo();

        if(!validate(habbo)) return;

        Room room = habbo.getRoomUnit().getRoom();
        RoomItem item = this.client.getHabbo().getRoomUnit().getRoom().getRoomItemManager().getRoomItemById(itemId);

        if (!(item instanceof InteractionYoutubeTV tv)) return;

        if(tv.currentPlaylist == null || tv.currentPlaylist.getVideos().isEmpty()) return;

        switch (state) {
            case PAUSE -> {
                tv.playing = false;
                tv.offset += Emulator.getIntUnixTimestamp() - tv.startedWatchingAt;
                if (tv.autoAdvance != null) tv.autoAdvance.cancel(true);
                room.sendComposer(new YoutubeControlVideoMessageComposer(tv.getId(), 2).compose());
            }
            case RESUME -> {
                tv.playing = true;
                tv.startedWatchingAt = Emulator.getIntUnixTimestamp();
                tv.autoAdvance = Emulator.getThreading().run(new YoutubeAdvanceVideo(tv), (tv.currentVideo.getDuration() - tv.offset) * 1000L);
                room.sendComposer(new YoutubeControlVideoMessageComposer(tv.getId(), 1).compose());
            }
            case PREVIOUS -> {
                int previousIndex = tv.currentPlaylist.getVideos().indexOf(tv.currentVideo) - 1;
                if (previousIndex < 0) previousIndex = tv.currentPlaylist.getVideos().size() - 1;
                tv.currentVideo = tv.currentPlaylist.getVideos().get(previousIndex);
            }
            case NEXT -> {
                int nextIndex = tv.currentPlaylist.getVideos().indexOf(tv.currentVideo) + 1;
                if (nextIndex >= tv.currentPlaylist.getVideos().size()) nextIndex = 0;
                tv.currentVideo = tv.currentPlaylist.getVideos().get(nextIndex);
            }
        }

        if (state == YoutubeState.PREVIOUS || state == YoutubeState.NEXT) {
            room.sendComposer(new YoutubeDisplayVideoMessageComposer(tv.getId(), tv.currentVideo, true, 0).compose());

            tv.cancelAdvancement();
            tv.autoAdvance = Emulator.getThreading().run(new YoutubeAdvanceVideo(tv), tv.currentVideo.getDuration() * 1000L);
            tv.startedWatchingAt = Emulator.getIntUnixTimestamp();
            tv.offset = 0;
            tv.playing = true;
            room.updateItem(tv);
        }
    }
}
