package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.database.DatabaseConstants;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.messenger.MessengerCategory;
import com.eu.habbo.habbohotel.navigation.NavigatorSavedSearch;
import com.eu.habbo.habbohotel.permissions.PermissionGroup;
import gnu.trove.map.hash.TIntIntHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class HabboInfo implements Runnable {
    private final int id;
    private String username;
    private String motto;
    private String look;
    private HabboGender gender;
    private String mail;
    private String sso;
    private String ipRegister;
    private String ipLogin;
    private int accountCreated;
    private PermissionGroup permissionGroup;
    private int credits;
    private int lastOnline;
    private int homeRoom;
    private boolean online;
    private int roomQueueId;
    private Class<? extends Game> currentGame;
    private TIntIntHashMap currencies;
    private GamePlayer gamePlayer;
    private int photoRoomId;
    private int photoTimestamp;
    private String photoURL;
    private String photoJSON;
    private int webPublishTimestamp;
    private String machineID;
    private List<NavigatorSavedSearch> savedSearches = new ArrayList<>();
    private List<MessengerCategory> messengerCategories = new ArrayList<>();
    public boolean firstVisit = false;

    public HabboInfo(ResultSet set) throws SQLException {
        this.id = set.getInt("id");
        this.username = set.getString("username");
        this.motto = set.getString("motto");
        this.look = set.getString("look");
        this.gender = HabboGender.valueOf(set.getString("gender"));
        this.mail = set.getString("mail");
        this.sso = set.getString("auth_ticket");
        this.ipRegister = set.getString("ip_register");
        this.ipLogin = set.getString("ip_current");
        this.permissionGroup = Emulator.getGameEnvironment().getPermissionsManager().getGroup(set.getInt("rank"));

        if (this.permissionGroup == null) {
            log.error("No existing rank found with id " + set.getInt("rank") + ". Make sure an entry in the permissions table exists.");
            log.warn(this.username + " has an invalid rank with id " + set.getInt("rank") + ". Make sure an entry in the permissions table exists.");
            this.permissionGroup = Emulator.getGameEnvironment().getPermissionsManager().getGroup(1);
        }

        this.accountCreated = set.getInt("account_created");
        this.credits = set.getInt("credits");
        this.homeRoom = set.getInt("home_room");
        this.lastOnline = set.getInt("last_online");
        this.machineID = set.getString("machine_id");
        this.online = false;

        this.loadCurrencies();
        this.loadSavedSearches();
        this.loadMessengerCategories();

        Emulator.getGameEnvironment().getHabboManager().getHabboInfoCache().getData().remove(this.id);
    }

    private void loadCurrencies() {
        this.currencies = new TIntIntHashMap();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_currency WHERE user_id = ?")) {
            statement.setInt(1, this.id);
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    this.currencies.put(set.getInt("type"), set.getInt("amount"));
                }
            }
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    private void saveCurrencies() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO users_currency (user_id, type, amount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = ?")) {
            this.currencies.forEachEntry((a, b) -> {
                try {
                    statement.setInt(1, HabboInfo.this.getId());
                    statement.setInt(2, a);
                    statement.setInt(3, b);
                    statement.setInt(4, b);
                    statement.addBatch();
                } catch (SQLException e) {
                    log.error("Caught SQL exception", e);
                }
                return true;
            });
            statement.executeBatch();
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    private void loadSavedSearches() {
        this.savedSearches = new ArrayList<>();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_saved_searches WHERE user_id = ?")) {
            statement.setInt(1, this.id);
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    this.savedSearches.add(new NavigatorSavedSearch(set.getString("search_code"), set.getString("filter"), set.getInt("id")));
                }
            }
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    public void addSavedSearch(NavigatorSavedSearch search) {
        this.savedSearches.add(search);

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO users_saved_searches (search_code, filter, user_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, search.getSearchCode());
            statement.setString(2, search.getFilter());
            statement.setInt(3, this.id);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating saved search failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    search.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating saved search failed, no ID found.");
                }
            }
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    public void deleteSavedSearch(NavigatorSavedSearch search) {
        this.savedSearches.remove(search);

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM users_saved_searches WHERE id = ?")) {
            statement.setInt(1, search.getId());
            statement.execute();
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    private void loadMessengerCategories() {
        this.messengerCategories = new ArrayList<>();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM messenger_categories WHERE user_id = ?")) {
            statement.setInt(1, this.id);
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    this.messengerCategories.add(new MessengerCategory(set.getString("name"), set.getInt(DatabaseConstants.USER_ID), set.getInt("id")));
                }
            }
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    public void addMessengerCategory(MessengerCategory category) {
        this.messengerCategories.add(category);

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO messenger_categories (name, user_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.getName());
            statement.setInt(2, this.id);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating messenger category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating messenger category failed, no ID found.");
                }
            }
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    public void deleteMessengerCategory(MessengerCategory category) {
        this.messengerCategories.remove(category);

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM messenger_categories WHERE id = ?")) {
            statement.setInt(1, category.getId());
            statement.execute();
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    public int getCurrencyAmount(int type) {
        return this.currencies.get(type);
    }

    public TIntIntHashMap getCurrencies() {
        return this.currencies;
    }

    public void addCurrencyAmount(int type, int amount) {
        this.currencies.adjustOrPutValue(type, amount, amount);
        this.run();
    }

    public void setCurrencyAmount(int type, int amount) {
        this.currencies.put(type, amount);
        this.run();
    }

    public void changeClothes(String newLook) {
        String[] parts = newLook.split("\\.");

        String head = parts[0];
        String body = parts[1];

        StringBuilder newLookBuilder = new StringBuilder(head).append(".").append(body);

        for (int i = 2; i < parts.length; i++) {
            newLookBuilder.append(".").append(parts[i]);
        }
        this.look = newLookBuilder.toString();
    }

    public boolean canBuy(CatalogItem item) {
        return this.credits >= item.getCredits() && this.getCurrencies().get(item.getPointsType()) >= item.getPoints();
    }

    public void setCredits(int credits) {
        this.credits = credits;
        this.run();
    }

    public void addCredits(int credits) {
        this.credits += credits;
        this.run();
    }

    public int getPixels() {
        return this.getCurrencyAmount(0);
    }

    public void setPixels(int pixels) {
        this.setCurrencyAmount(0, pixels);
        this.run();
    }

    public void addPixels(int pixels) {
        this.addCurrencyAmount(0, pixels);
        this.run();
    }

    public boolean isInGame() {
        return this.currentGame != null;
    }

    @Override
    public void run() {
        this.saveCurrencies();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE users SET motto = ?, online = ?, look = ?, gender = ?, credits = ?, last_login = ?, last_online = ?, home_room = ?, ip_current = ?, `rank` = ?, machine_id = ?, username = ? WHERE id = ?")) {
            statement.setString(1, this.motto);
            statement.setString(2, this.online ? "1" : "0");
            statement.setString(3, this.look);
            statement.setString(4, this.gender.name());
            statement.setInt(5, this.credits);
            statement.setInt(7, this.lastOnline);
            statement.setInt(6, Emulator.getIntUnixTimestamp());
            statement.setInt(8, this.homeRoom);
            statement.setString(9, this.ipLogin);
            statement.setInt(10, this.permissionGroup != null ? this.permissionGroup.getId() : 1);
            statement.setString(11, this.machineID);
            statement.setString(12, this.username);
            statement.setInt(13, this.id);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    public int getBonusRarePoints() {
        return this.getCurrencyAmount(Emulator.getConfig().getInt("hotelview.promotional.points.type"));
    }

    public HabboStats getHabboStats() {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.getId());
        if (habbo != null) {
            return habbo.getHabboStats();
        }

        return HabboStats.load(this);
    }
}
