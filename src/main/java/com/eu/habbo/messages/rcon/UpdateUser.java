package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.UserChangeMessageComposer;
import com.eu.habbo.messages.outgoing.users.AccountPreferencesComposer;
import com.eu.habbo.messages.outgoing.users.FigureUpdateComposer;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
public class UpdateUser extends RCONMessage<UpdateUser.JSON> {

    public UpdateUser() {
        super(UpdateUser.JSON.class);
    }

    @Override
    public void handle(Gson gson, JSON json) {
        if (json.user_id > 0) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(json.user_id);

            if (habbo != null) {
                habbo.getHabboStats().addAchievementScore(json.achievement_score);

                if (json.block_following != -1) {
                    habbo.getHabboStats().setBlockFollowing(json.block_following == 1);
                }

                if (json.block_friendrequests != -1) {
                    habbo.getHabboStats().setBlockFriendRequests(json.block_friendrequests == 1);
                }

                if (json.block_roominvites != -1) {
                    habbo.getHabboStats().setBlockRoomInvites(json.block_roominvites == 1);
                }

                if (json.old_chat != -1) {
                    habbo.getHabboStats().setPreferOldChat(json.old_chat == 1);
                }

                if (json.block_camera_follow != -1) {
                    habbo.getHabboStats().setBlockCameraFollow(json.block_camera_follow == 1);
                }

                if (!json.look.isEmpty()) {
                    habbo.getHabboInfo().setLook(json.look);
                    if (habbo.getClient() != null) {
                        habbo.getClient().sendResponse(new FigureUpdateComposer(habbo).compose());
                    }

                    if (habbo.getRoomUnit().getRoom() != null) {
                        habbo.getRoomUnit().getRoom().sendComposer(new UserChangeMessageComposer(habbo).compose());
                    }
                }

                habbo.getHabboStats().run();
                habbo.getClient().sendResponse(new AccountPreferencesComposer(habbo));
            } else {
                try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
                    try (PreparedStatement statement = connection.prepareStatement("UPDATE users_settings SET achievement_score = achievement_score + ? " +
                            (json.block_following != -1 ? ", block_following = ?" : "") +
                            (json.block_friendrequests != -1 ? ", block_friendrequests = ?" : "") +
                            (json.block_roominvites != -1 ? ", block_roominvites = ?" : "") +
                            (json.old_chat != -1 ? ", old_chat = ?" : "") +
                            (json.block_camera_follow != -1 ? ", block_camera_follow = ?" : "") +
                            " WHERE user_id = ? LIMIT 1"))

                    {
                        int index = 1;
                        statement.setInt(index, json.achievement_score);
                        index++;

                        if (json.block_following != -1) {
                            statement.setString(index, json.block_following == 1 ? "1" : "0");
                            index++;
                        }
                        if (json.block_friendrequests != -1) {
                            statement.setString(index, json.block_friendrequests == 1 ? "1" : "0");
                            index++;
                        }
                        if (json.block_roominvites != -1) {
                            statement.setString(index, json.block_roominvites == 1 ? "1" : "0");
                            index++;
                        }
                        if (json.old_chat != -1) {
                            statement.setString(index, json.old_chat == 1 ? "1" : "0");
                            index++;
                        }
                        if (json.block_camera_follow != -1) {
                            statement.setString(index, json.block_camera_follow == 1 ? "1" : "0");
                            index++;
                        }
                        statement.setInt(index, json.user_id);
                        statement.execute();
                    }

                    if (!json.look.isEmpty()) {
                        try (PreparedStatement statement = connection.prepareStatement("UPDATE users SET look = ? WHERE id = ? LIMIT 1")) {
                            statement.setString(1, json.look);
                            statement.setInt(2, json.user_id);
                            statement.execute();
                        }
                    }
                } catch (SQLException e) {
                    log.error("Caught SQL exception", e);
                }
            }
        }
    }

    static class JSON {

        public int user_id;


        public int achievement_score = 0;


        public int block_following = -1;


        public int block_friendrequests = -1;


        public int block_roominvites = -1;


        public int old_chat = -1;


        public int block_camera_follow = -1;


        public String look = "";

        public boolean strip_unredeemed_clothing = false;
        //More could be added in the future.
    }
}