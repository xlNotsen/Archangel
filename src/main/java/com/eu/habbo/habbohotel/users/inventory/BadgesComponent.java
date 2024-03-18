package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.PermissionGroup;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import gnu.trove.set.hash.THashSet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

@Slf4j
public class BadgesComponent {

    @Getter
    private final THashSet<HabboBadge> badges = new THashSet<>();

    public BadgesComponent(Habbo habbo) {
        this.badges.addAll(loadBadges(habbo));
    }

    private static THashSet<HabboBadge> loadBadges(Habbo habbo) {
        THashSet<HabboBadge> badgesList = new THashSet<>();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_badges WHERE user_id = ?")) {
            statement.setInt(1, habbo.getHabboInfo().getId());

            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    HabboBadge badge = new HabboBadge(set, habbo);

                    if(!(habbo.getHabboInfo().getPermissionGroup().hasBadge() && habbo.getHabboInfo().getPermissionGroup().getBadge() == badge.getCode())) {
                        deleteBadge(habbo.getHabboInfo().getId(), badge.getCode());
                        continue;
                    }

                    badgesList.add(badge);
                }
            }
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }

        return badgesList;
    }

    public static void resetSlots(Habbo habbo) {
        for (HabboBadge badge : habbo.getInventory().getBadgesComponent().getBadges()) {
            if (badge.getSlot() == 0)
                continue;

            badge.setSlot(0);
            badge.needsUpdate(true);
            Emulator.getThreading().run(badge);
        }
    }

    public static ArrayList<HabboBadge> getBadgesOfflineHabbo(int userId) {
        ArrayList<HabboBadge> badgesList = new ArrayList<>();
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_badges WHERE slot_id > 0 AND user_id = ? ORDER BY slot_id ASC")) {
            statement.setInt(1, userId);
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    badgesList.add(new HabboBadge(set, null));
                }
            }
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
        return badgesList;
    }

    public static HabboBadge createBadge(String code, Habbo habbo) {
        HabboBadge badge = new HabboBadge(0, code, 0, habbo);
        badge.run();
        habbo.getInventory().getBadgesComponent().addBadge(badge);
        return badge;
    }

    public static void deleteBadge(int userId, String badge) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE users_badges FROM users_badges WHERE user_id = ? AND badge_code LIKE ?")) {
            statement.setInt(1, userId);
            statement.setString(2, badge);
            statement.execute();
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
        }
    }

    public ArrayList<HabboBadge> getWearingBadges() {
        synchronized (this.badges) {
            ArrayList<HabboBadge> badgesList = new ArrayList<>();
            for (HabboBadge badge : this.badges) {
                if (badge.getSlot() == 0)
                    continue;

                badgesList.add(badge);
            }

            badgesList.sort(Comparator.comparingInt(HabboBadge::getSlot));
            return badgesList;
        }
    }

    public boolean hasBadge(String badge) {
        return this.getBadge(badge) != null;
    }

    public HabboBadge getBadge(String badgeCode) {
        synchronized (this.badges) {
            for (HabboBadge badge : this.badges) {
                if (badge.getCode().equalsIgnoreCase(badgeCode))
                    return badge;
            }
            return null;
        }
    }

    public void addBadge(HabboBadge badge) {
        synchronized (this.badges) {
            this.badges.add(badge);
        }
    }

    public HabboBadge removeBadge(String badge) {
        synchronized (this.badges) {
            for (HabboBadge b : this.badges) {
                if (b.getCode().equalsIgnoreCase(badge)) {
                    this.badges.remove(b);
                    return b;
                }
            }
        }

        return null;
    }

    public void removeBadge(HabboBadge badge) {
        synchronized (this.badges) {
            this.badges.remove(badge);
        }
    }

    public void dispose() {
        synchronized (this.badges) {
            this.badges.clear();
        }
    }
}
