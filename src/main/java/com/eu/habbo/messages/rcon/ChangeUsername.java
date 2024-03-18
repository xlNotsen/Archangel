package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.users.UserObjectComposer;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
public class ChangeUsername extends RCONMessage<ChangeUsername.JSON> {

    public ChangeUsername() {
        super(ChangeUsername.JSON.class);
    }

    @Override
    public void handle(Gson gson, JSON json) {
        try {
            if (json.user_id <= 0) {
                this.status = RCONMessage.HABBO_NOT_FOUND;
                this.message = "User not found";
                return;
            }

            boolean success = true;

            Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(json.user_id);
            if (habbo != null) {
                if (json.canChange)
                    habbo.alert(Emulator.getTexts().getValue("rcon.alert.user.change_username"));

                habbo.getHabboStats().setAllowNameChange(json.canChange);
                habbo.getClient().sendResponse(new UserObjectComposer(habbo));
            } else {
                try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
                    try (PreparedStatement statement = connection.prepareStatement("UPDATE users_settings SET allow_name_change = ? WHERE user_id = ? LIMIT 1")) {
                        statement.setBoolean(1, json.canChange);
                        statement.setInt(2, json.user_id);

                        success = statement.executeUpdate() >= 1;
                    } catch (SQLException sqlException) {
                        this.message = "SQL Exception occurred";
                        log.error(this.message, sqlException);
                    }
                } catch (SQLException sqlException) {
                    this.message = "SQL Exception occurred";
                    log.error(this.message, sqlException);
                }
            }

            this.status = success ? RCONMessage.STATUS_OK : RCONMessage.STATUS_ERROR;
            this.message = success ? "Sent successfully." : "There was an error updating this user.";
        }
        catch (Exception e) {
            this.status = RCONMessage.SYSTEM_ERROR;
            this.message = "Exception occurred";
            log.error(message, e);
        }
    }

    static class JSON {
        public int user_id;

        public boolean canChange;
    }
}