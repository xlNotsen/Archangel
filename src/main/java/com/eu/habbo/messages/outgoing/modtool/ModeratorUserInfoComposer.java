package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.database.DatabaseConstants;
import com.eu.habbo.habbohotel.modtool.ModToolSanctionItem;
import com.eu.habbo.habbohotel.modtool.ModToolSanctionLevelItem;
import com.eu.habbo.habbohotel.modtool.ModToolSanctions;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.map.hash.THashMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.eu.habbo.database.DatabaseConstants.CAUGHT_SQL_EXCEPTION;

@Slf4j
@AllArgsConstructor
public class ModeratorUserInfoComposer extends MessageComposer {

    private final ResultSet set;

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.moderatorUserInfoComposer);
        try {
            int totalBans = 0;

            totalBans = getTotalBansFromDB();

            this.response.appendInt(this.set.getInt(DatabaseConstants.USER_ID));
            this.response.appendString(this.set.getString("username"));
            this.response.appendString(this.set.getString("look"));
            this.response.appendInt((Emulator.getIntUnixTimestamp() - this.set.getInt("account_created")) / 60);
            this.response.appendInt((this.set.getInt("online") == 1 ? 0 : Emulator.getIntUnixTimestamp() - this.set.getInt("last_online")) / 60);
            this.response.appendBoolean(this.set.getInt("online") == 1);
            this.response.appendInt(this.set.getInt("cfh_send"));
            this.response.appendInt(this.set.getInt("cfh_abusive"));
            this.response.appendInt(this.set.getInt("cfh_warnings"));
            this.response.appendInt(totalBans); // Number of bans
            this.response.appendInt(this.set.getInt("tradelock_amount"));
            this.response.appendString(""); //Trading lock expiry timestamp
            this.response.appendString(""); //Last Purchase Timestamp
            this.response.appendInt(this.set.getInt(DatabaseConstants.USER_ID)); //Personal Identification #
            this.response.appendInt(0); // Number of account bans
            this.response.appendString(this.set.getBoolean("hide_mail") ? "" : this.set.getString("mail"));
            this.response.appendString("Rank (" + this.set.getInt("rank_id") + "): " + this.set.getString("rank_name")); //user_class_txt

            ModToolSanctions modToolSanctions = Emulator.getGameEnvironment().getModToolSanctions();

            if (Emulator.getConfig().getBoolean("hotel.sanctions.enabled")) {
                THashMap<Integer, ArrayList<ModToolSanctionItem>> modToolSanctionItemsHashMap = Emulator.getGameEnvironment().getModToolSanctions().getSanctions(this.set.getInt(DatabaseConstants.USER_ID));
                ArrayList<ModToolSanctionItem> modToolSanctionItems = modToolSanctionItemsHashMap.get(this.set.getInt(DatabaseConstants.USER_ID));

                if (modToolSanctionItems != null && !modToolSanctionItems.isEmpty()) //has sanction
                {
                    ModToolSanctionItem item = modToolSanctionItems.get(modToolSanctionItems.size() - 1);
                    ModToolSanctionLevelItem modToolSanctionLevelItem = modToolSanctions.getSanctionLevelItem(item.getSanctionLevel());

                    this.response.appendString(modToolSanctions.getSanctionType(modToolSanctionLevelItem));
                    this.response.appendInt(31);
                }

            }

            return this.response;
        } catch (SQLException e) {
            log.error(CAUGHT_SQL_EXCEPTION, e);
        }
        return null;
    }

    private int getTotalBansFromDB() {
        int totalBans = 0;
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS amount FROM bans WHERE user_id = ?")) {
            statement.setInt(1, this.set.getInt(DatabaseConstants.USER_ID));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    totalBans = resultSet.getInt("amount");
                }
            } catch (SQLException e) {
                log.error(CAUGHT_SQL_EXCEPTION, e);
            }
        } catch (SQLException e) {
            log.error(CAUGHT_SQL_EXCEPTION, e);
        }
        return totalBans;
    }
}
