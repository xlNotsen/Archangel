package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.friends.MessengerErrorComposer;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class AcceptFriendEvent extends MessageHandler {

    @Override
    public void handle() {
        int count = this.packet.readInt();
        int userId;

        for (int i = 0; i < count; i++) {
            userId = this.packet.readInt();

            if (userId == 0)
                return;

            if (this.client.getHabbo().getMessenger().getFriends().containsKey(userId)) {
                this.client.getHabbo().getMessenger().deleteFriendRequests(userId, this.client.getHabbo().getHabboInfo().getId());
                continue;
            }

            Habbo target = Emulator.getGameEnvironment().getHabboManager().getHabbo(userId);

            if(target == null) {
                HabboInfo habboInfo = Emulator.getGameEnvironment().getHabboManager().getOfflineHabboInfo(userId);

                if(habboInfo == null) {
                    this.client.sendResponse(new MessengerErrorComposer(MessengerErrorComposer.TARGET_NOT_FOUND));
                    this.client.getHabbo().getMessenger().deleteFriendRequests(userId, this.client.getHabbo().getHabboInfo().getId());
                    continue;
                }

                try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT users.*, users_settings.block_friendrequests FROM users INNER JOIN users_settings ON users.id = users_settings.user_id WHERE username = ? LIMIT 1")) {
                    statement.setString(1, habboInfo.getUsername());
                    try (ResultSet set = statement.executeQuery()) {
                        while (set.next()) {
                            target = new Habbo(set);
                        }
                    }
                } catch (SQLException e) {
                    log.error("Caught SQL exception", e);
                    return;
                }
            }

            if(target == null) {
                this.client.sendResponse(new MessengerErrorComposer(MessengerErrorComposer.TARGET_NOT_FOUND));
                this.client.getHabbo().getMessenger().deleteFriendRequests(userId, this.client.getHabbo().getHabboInfo().getId());
                continue;
            }

            if(this.client.getHabbo().getMessenger().getFriends().size() >= this.client.getHabbo().getHabboStats().getMaxFriends() && !this.client.getHabbo().hasPermissionRight(Permission.ACC_INFINITE_FRIENDS)) {
                this.client.sendResponse(new MessengerErrorComposer(MessengerErrorComposer.FRIEND_LIST_OWN_FULL));
                break;
            }

            if(target.getMessenger().getFriends().size() >= target.getHabboStats().getMaxFriends() && !target.hasPermissionRight(Permission.ACC_INFINITE_FRIENDS)) {
                this.client.sendResponse(new MessengerErrorComposer(MessengerErrorComposer.FRIEND_LIST_TARGET_FULL));
                continue;
            }

            this.client.getHabbo().getMessenger().acceptFriendRequest(userId, this.client.getHabbo().getHabboInfo().getId());

            Messenger.checkFriendSizeProgress(this.client.getHabbo());
            Messenger.checkFriendSizeProgress(target);
        }
    }
}
