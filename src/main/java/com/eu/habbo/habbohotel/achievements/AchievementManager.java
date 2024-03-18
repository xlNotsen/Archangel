package com.eu.habbo.habbohotel.achievements;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.messages.outgoing.achievements.AchievementComposer;
import com.eu.habbo.messages.outgoing.achievements.AchievementUnlockedComposer;
import com.eu.habbo.messages.outgoing.achievements.talenttrack.TalentLevelUpComposer;
import com.eu.habbo.messages.outgoing.inventory.FurniListInvalidateComposer;
import com.eu.habbo.messages.outgoing.inventory.UnseenItemsComposer;
import com.eu.habbo.messages.outgoing.rooms.users.UserChangeMessageComposer;
import com.eu.habbo.messages.outgoing.users.BadgeReceivedComposer;
import com.eu.habbo.messages.outgoing.users.UserBadgesComposer;
import com.eu.habbo.plugin.Event;
import com.eu.habbo.plugin.events.users.achievements.UserAchievementLeveledEvent;
import com.eu.habbo.plugin.events.users.achievements.UserAchievementProgressEvent;
import gnu.trove.map.hash.THashMap;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.eu.habbo.database.DatabaseConstants.CAUGHT_SQL_EXCEPTION;

@Slf4j
public class AchievementManager {
    public static boolean TALENT_TRACK_ENABLED = false;

    private final THashMap<String, Achievement> achievements;
    private final THashMap<TalentTrackType, LinkedHashMap<Integer, TalentTrackLevel>> talentTrackLevels;

    public AchievementManager() {
        this.achievements = new THashMap<>();
        this.talentTrackLevels = new THashMap<>();
    }

    public static void progressAchievement(int habboId, Achievement achievement) {
        progressAchievement(habboId, achievement, 1);
    }

    public static void progressAchievement(int habboId, Achievement achievement, int amount) {
        if (achievement != null) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(habboId);

            if (habbo != null) {
                progressAchievement(habbo, achievement, amount);
            } else {
                try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                     PreparedStatement statement = connection.prepareStatement("" +
                             "INSERT INTO users_achievements_queue (user_id, achievement_id, amount) VALUES (?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE amount = amount + ?")) {
                    statement.setInt(1, habboId);
                    statement.setInt(2, achievement.id);
                    statement.setInt(3, amount);
                    statement.setInt(4, amount);
                    statement.execute();
                } catch (SQLException e) {
                    log.error(CAUGHT_SQL_EXCEPTION, e);
                }
            }
        }
    }

    public static void progressAchievement(Habbo habbo, Achievement achievement) {
        progressAchievement(habbo, achievement, 1);
    }

    public static void progressAchievement(Habbo habbo, Achievement achievement, int amount) {
        if (achievement == null || habbo == null || !habbo.isOnline())
            return;

        int currentProgress = habbo.getHabboStats().getAchievementProgress(achievement);

        if (currentProgress == -1) {
            currentProgress = 0;
            createUserEntry(habbo, achievement);
            habbo.getHabboStats().setProgress(achievement, 0);
        }

        if (Emulator.getPluginManager().isRegistered(UserAchievementProgressEvent.class, true)) {
            Event userAchievementProgressedEvent = new UserAchievementProgressEvent(habbo, achievement, amount);
            Emulator.getPluginManager().fireEvent(userAchievementProgressedEvent);

            if (userAchievementProgressedEvent.isCancelled())
                return;
        }

        AchievementLevel oldLevel = achievement.getLevelForProgress(currentProgress);

        if (oldLevel != null && (oldLevel.getLevel() == achievement.levels.size() && currentProgress >= oldLevel.getProgress())) //Maximum achievement gotten.
            return;

        habbo.getHabboStats().setProgress(achievement, currentProgress + amount);

        AchievementLevel newLevel = achievement.getLevelForProgress(currentProgress + amount);

        if (AchievementManager.TALENT_TRACK_ENABLED) {
            Arrays.stream(TalentTrackType.values())
                    .filter(type -> Emulator.getGameEnvironment().getAchievementManager().talentTrackLevels.containsKey(type))
                    .filter(type -> Emulator.getGameEnvironment().getAchievementManager().talentTrackLevels.get(type).entrySet().stream()
                            .anyMatch(entry -> entry.getValue().achievements.containsKey(achievement)))
                    .forEach(type -> Emulator.getGameEnvironment().getAchievementManager().handleTalentTrackAchievement(habbo, type));
        }

        if (newLevel == null ||
                (oldLevel != null && (oldLevel.getLevel() == newLevel.getLevel() && newLevel.getLevel() < achievement.levels.size()))) {
            habbo.getClient().sendResponse(new AchievementComposer(habbo, achievement));
        } else {
            if (Emulator.getPluginManager().isRegistered(UserAchievementLeveledEvent.class, true)) {
                Event userAchievementLeveledEvent = new UserAchievementLeveledEvent(habbo, achievement, oldLevel, newLevel);
                Emulator.getPluginManager().fireEvent(userAchievementLeveledEvent);

                if (userAchievementLeveledEvent.isCancelled())
                    return;
            }

            habbo.getClient().sendResponse(new AchievementComposer(habbo, achievement));
            habbo.getClient().sendResponse(new AchievementUnlockedComposer(habbo, achievement));

            //Exception could possibly arise when the user disconnects while being in tour.
            //The achievement is then progressed but the user is already disposed so fetching
            //the badge would result in an nullpointer exception. This is normal behaviour.
            HabboBadge badge = null;

            if (oldLevel != null) {
                try {
                    badge = habbo.getInventory().getBadgesComponent().getBadge(("ACH_" + achievement.name + oldLevel.getLevel()).toLowerCase());
                } catch (Exception e) {
                    log.error("Caught exception", e);
                    return;
                }
            }

            String newBadgCode = "ACH_" + achievement.name + newLevel.getLevel();

            if (badge != null) {
                badge.setCode(newBadgCode);
                badge.needsInsert(false);
                badge.needsUpdate(true);
            } else {
                if (habbo.getInventory().getBadgesComponent().hasBadge(newBadgCode))
                    return;

                badge = new HabboBadge(0, newBadgCode, 0, habbo);
                habbo.getClient().sendResponse(new BadgeReceivedComposer(badge));
                badge.needsInsert(true);
                badge.needsUpdate(true);
                habbo.getInventory().getBadgesComponent().addBadge(badge);
            }

            Emulator.getThreading().run(badge);

            if (badge.getSlot() > 0 && habbo.getRoomUnit().getRoom() != null) {
                habbo.getRoomUnit().getRoom().sendComposer(new UserBadgesComposer(habbo.getInventory().getBadgesComponent().getWearingBadges(), habbo.getHabboInfo().getId()).compose());
            }

            habbo.getClient().sendResponse(new UnseenItemsComposer(badge.getId(), UnseenItemsComposer.AddHabboItemCategory.BADGE));

            habbo.getHabboStats().addAchievementScore(newLevel.getPoints());

            if (newLevel.getRewardAmount() > 0) {
                habbo.givePoints(newLevel.getRewardType(), newLevel.getRewardAmount());
            }

            if (habbo.getRoomUnit().getRoom() != null) {
                habbo.getRoomUnit().getRoom().sendComposer(new UserChangeMessageComposer(habbo).compose());
            }
        }
    }

    public static boolean hasAchieved(Habbo habbo, Achievement achievement) {
        int currentProgress = habbo.getHabboStats().getAchievementProgress(achievement);

        if (currentProgress == -1) {
            return false;
        }

        AchievementLevel level = achievement.getLevelForProgress(currentProgress);

        if (level == null)
            return false;

        AchievementLevel nextLevel = achievement.levels.get(level.getLevel() + 1);

        return nextLevel == null && currentProgress >= level.getProgress();
    }

    public static void createUserEntry(Habbo habbo, Achievement achievement) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO users_achievements (user_id, achievement_name, progress) VALUES (?, ?, ?)")) {
            statement.setInt(1, habbo.getHabboInfo().getId());
            statement.setString(2, achievement.name);
            statement.setInt(3, 1);
            statement.execute();
        } catch (SQLException e) {
            log.error(CAUGHT_SQL_EXCEPTION, e);
        }
    }

    public static void saveAchievements(Habbo habbo) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE users_achievements SET progress = ? WHERE achievement_name = ? AND user_id = ? LIMIT 1")) {
            statement.setInt(3, habbo.getHabboInfo().getId());
            for (Map.Entry<Achievement, Integer> map : habbo.getHabboStats().getAchievementProgress().entrySet()) {
                statement.setInt(1, map.getValue());
                statement.setString(2, map.getKey().name);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            log.error(CAUGHT_SQL_EXCEPTION, e);
        }
    }

    public static int getAchievementProgressForHabbo(int userId, Achievement achievement) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT progress FROM users_achievements WHERE user_id = ? AND achievement_name = ? LIMIT 1")) {
            statement.setInt(1, userId);
            statement.setString(2, achievement.name);
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getInt("progress");
                }
            }
        } catch (SQLException e) {
            log.error(CAUGHT_SQL_EXCEPTION, e);
        }

        return 0;
    }

    public void reload() {
        long millis = System.currentTimeMillis();
        synchronized (this.achievements) {
            for (Achievement achievement : this.achievements.values()) {
                achievement.clearLevels();
            }

            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
                try (Statement statement = connection.createStatement(); ResultSet set = statement.executeQuery("SELECT * FROM achievements WHERE visible = 1")) {
                    while (set.next()) {
                        if (!this.achievements.containsKey(set.getString("name"))) {
                            this.achievements.put(set.getString("name"), new Achievement(set));
                        } else {
                            this.achievements.get(set.getString("name")).addLevel(new AchievementLevel(set));
                        }
                    }
                } catch (SQLException e) {
                    log.error(CAUGHT_SQL_EXCEPTION, e);
                } catch (Exception e) {
                    log.error("Caught exception", e);
                }


                synchronized (this.talentTrackLevels) {
                    this.talentTrackLevels.clear();

                    try (Statement statement = connection.createStatement(); ResultSet set = statement.executeQuery("SELECT * FROM achievements_talents ORDER BY level ASC")) {
                        while (set.next()) {
                            TalentTrackLevel level = new TalentTrackLevel(set);

                            if (!this.talentTrackLevels.containsKey(level.type)) {
                                this.talentTrackLevels.put(level.type, new LinkedHashMap<>());
                            }

                            this.talentTrackLevels.get(level.type).put(level.level, level);
                        }
                    }
                }
            } catch (SQLException e) {
                log.error(CAUGHT_SQL_EXCEPTION, e);
                log.error("Achievement Manager -> Failed to load!");
                return;
            }
        }

        log.info("Achievement Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
    }

    public Achievement getAchievement(String name) {
        return this.achievements.get(name);
    }

    public Achievement getAchievement(int id) {
        synchronized (this.achievements) {
            for (Map.Entry<String, Achievement> set : this.achievements.entrySet()) {
                if (set.getValue().id == id) {
                    return set.getValue();
                }
            }
        }

        return null;
    }

    public THashMap<String, Achievement> getAchievements() {
        return this.achievements;
    }

    public Map<Integer, TalentTrackLevel> getTalenTrackLevels(TalentTrackType type) {
        return this.talentTrackLevels.get(type);
    }

    public TalentTrackLevel calculateTalenTrackLevel(Habbo habbo, TalentTrackType type) {
        TalentTrackLevel level = null;

        for (Map.Entry<Integer, TalentTrackLevel> entry : this.talentTrackLevels.get(type).entrySet()) {
            final boolean[] allCompleted = {true};
            entry.getValue().achievements.forEachEntry((a, b) -> {
                if (habbo.getHabboStats().getAchievementProgress(a) < b) {
                    allCompleted[0] = false;
                }

                return allCompleted[0];
            });

            if (allCompleted[0]) {
                if (level == null || level.level < entry.getValue().level) {
                    level = entry.getValue();
                }
            } else {
                break;
            }
        }

        return level;
    }

    public void handleTalentTrackAchievement(Habbo habbo, TalentTrackType type) {
        TalentTrackLevel currentLevel = this.calculateTalenTrackLevel(habbo, type);

        if (currentLevel != null) {
            if (currentLevel.level > habbo.getHabboStats().talentTrackLevel(type)) {
                for (int i = habbo.getHabboStats().talentTrackLevel(type); i <= currentLevel.level; i++) {
                    TalentTrackLevel level = this.getTalentTrackLevel(type, i);

                    if (level != null) {
                        if (level.items != null && !level.items.isEmpty()) {
                            for (Item item : level.items) {
                                RoomItem rewardItem = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), item, 0, 0, "");
                                habbo.getInventory().getItemsComponent().addItem(rewardItem);
                                habbo.getClient().sendResponse(new UnseenItemsComposer(rewardItem));
                                habbo.getClient().sendResponse(new FurniListInvalidateComposer());
                            }
                        }

                        if (level.badges != null && level.badges.length > 0) {
                            for (String badge : level.badges) {
                                if (!badge.isEmpty()) {
                                    HabboBadge b = new HabboBadge(0, badge, 0, habbo);
                                    Emulator.getThreading().run(b);
                                    habbo.getInventory().getBadgesComponent().addBadge(b);
                                    habbo.getClient().sendResponse(new BadgeReceivedComposer(b));
                                }
                            }
                        }

                        if (level.perks != null && level.perks.length > 0) {
                            for (String perk : level.perks) {
                                if (perk.equalsIgnoreCase("TRADE")) {
                                    habbo.getHabboStats().setPerkTrade(true);
                                    break;
                                }
                            }
                        }
                        habbo.getClient().sendResponse(new TalentLevelUpComposer(type, level));
                    }
                }
            }

            habbo.getHabboStats().setTalentLevel(type, currentLevel.level);
        }
    }

    public TalentTrackLevel getTalentTrackLevel(TalentTrackType type, int level) {
        return this.talentTrackLevels.get(type).get(level);
    }
}