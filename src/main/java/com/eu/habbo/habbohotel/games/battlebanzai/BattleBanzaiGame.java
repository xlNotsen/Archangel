package com.eu.habbo.habbohotel.games.battlebanzai;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.games.*;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTimer;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.InteractionBattleBanzaiSphere;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.InteractionBattleBanzaiTile;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.gates.InteractionBattleBanzaiGate;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.scoreboards.InteractionBattleBanzaiScoreboard;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.constants.RoomUserAction;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.ExpressionMessageComposer;
import com.eu.habbo.threading.runnables.BattleBanzaiTilesFlicker;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class BattleBanzaiGame extends Game {
    
    public static final int effectId = 32;
    
    public static final int POINTS_HIJACK_TILE = Emulator.getConfig().getInt("hotel.banzai.points.tile.steal", 0);
    
    public static final int POINTS_FILL_TILE = Emulator.getConfig().getInt("hotel.banzai.points.tile.fill", 0);
    
    public static final int POINTS_LOCK_TILE = Emulator.getConfig().getInt("hotel.banzai.points.tile.lock", 1);

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Emulator.getConfig().getInt("hotel.banzai.fill.threads", 2));
    private final THashMap<GameTeamColors, THashSet<RoomItem>> lockedTiles;
    private final THashMap<Integer, RoomItem> gameTiles;
    private int tileCount;
    private int countDown;
    private int countDown2;

    public BattleBanzaiGame(Room room) {
        super(BattleBanzaiGameTeam.class, BattleBanzaiGamePlayer.class, room, true);

        this.lockedTiles = new THashMap<>();
        this.gameTiles = new THashMap<>();

        room.setAllowEffects(true);
    }

    @Override
    public void initialise() {
        if (!this.state.equals(GameState.IDLE))
            return;
        
        /* The first countdown is activated for the first two seconds emitting only the blue light (second interaction),
            the second, after another two seconds, completely activates the sphere (third interaction).
         */
        this.countDown = 3;
        this.countDown2 = 2;

        this.resetMap();

        synchronized (this.teams) {
            for (GameTeam t : this.teams.values()) {
                t.initialise();
            }
        }

        for (RoomItem item : this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class)) {
            item.setExtraData("1");
            this.room.updateItemState(item);
        }

        this.start();
    }

    @Override
    public boolean addHabbo(Habbo habbo, GameTeamColors teamColor) {
        return super.addHabbo(habbo, teamColor);
    }

    @Override
    public void start() {
        if (!this.state.equals(GameState.IDLE))
            return;

        super.start();

        this.refreshGates();

        Emulator.getThreading().run(this, 0);
    }

    @Override
    public void run() {
        try {
            if (this.state.equals(GameState.IDLE))
                return;

            if (this.countDown > 0) {
                this.countDown--;

                if (this.countDown == 0) {
                    for (RoomItem item : this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class)) {
                        item.setExtraData("1");
                        this.room.updateItemState(item);
                        if(this.countDown2 > 0) {
                            this.countDown2--;
                            if(this.countDown2 == 0) {
                                item.setExtraData("2");
                                this.room.updateItemState(item);
                            }
                        }
                    }
                }

                if (this.countDown > 1) {
                    Emulator.getThreading().run(this, 500);

                    return;
                }
            }

            Emulator.getThreading().run(this, 1000);

            if (this.state.equals(GameState.PAUSED)) return;

            int total = 0;
            synchronized (this.lockedTiles) {
                for (Map.Entry<GameTeamColors, THashSet<RoomItem>> set : this.lockedTiles.entrySet()) {
                    total += set.getValue().size();
                }
            }

            GameTeam highestScore = null;

            synchronized (this.teams) {
                for (Map.Entry<GameTeamColors, GameTeam> set : this.teams.entrySet()) {
                    if (highestScore == null || highestScore.getTotalScore() < set.getValue().getTotalScore()) {
                        highestScore = set.getValue();
                    }
                }
            }

            if (highestScore != null) {
                for (RoomItem item : this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class)) {
                    item.setExtraData((highestScore.teamColor.type + 2) + "");
                    this.room.updateItemState(item);
                }
            }

            if (total >= this.tileCount && this.tileCount != 0) {
                for (InteractionGameTimer timer : room.getRoomSpecialTypes().getGameTimers().values()) {
                    if (timer.isRunning()) {
                        timer.endGame(room);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Caught exception", e);
        }
    }

    @Override
    public void onEnd() {
        GameTeam winningTeam = null;

        boolean singleTeamGame = this.teams.values().stream().filter(t -> t.getMembers().size() > 0).count() == 1;

        for (GameTeam team : this.teams.values()) {
            if (!singleTeamGame) {
                for (GamePlayer player : team.getMembers()) {
                    if (player.getScoreAchievementValue() > 0) {
                        AchievementManager.progressAchievement(player.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("BattleBallPlayer"));
                    }
                }
            }

            if (winningTeam == null || team.getTotalScore() > winningTeam.getTotalScore()) {
                winningTeam = team;
            }
        }

        if (winningTeam != null) {
            if (!singleTeamGame) {
                for (GamePlayer player : winningTeam.getMembers()) {
                    if (player.getScoreAchievementValue() > 0) {
                        this.room.sendComposer(new ExpressionMessageComposer(player.getHabbo().getRoomUnit(), RoomUserAction.WAVE).compose());
                        AchievementManager.progressAchievement(player.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("BattleBallWinner"));
                    }
                }
            }

            for (RoomItem item : this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class)) {
                item.setExtraData((6 + winningTeam.teamColor.type) + "");
                this.room.updateItemState(item);
            }
            synchronized (this.lockedTiles) {
                Emulator.getThreading().run(new BattleBanzaiTilesFlicker(this.lockedTiles.get(winningTeam.teamColor), winningTeam.teamColor, this.room));
            }
        }

        super.onEnd();
    }

    @Override
    public void stop() {
        super.stop();

        this.refreshGates();

        for (RoomItem tile : this.gameTiles.values()) {
            if (tile.getExtraData().equals("1")) {
                tile.setExtraData("0");
                this.room.updateItem(tile);
            }
        }
        synchronized (this.lockedTiles) {
            this.lockedTiles.clear();
        }
    }


    private synchronized void resetMap() {
        this.tileCount = 0;
        for (RoomItem item : this.room.getRoomItemManager().getFloorItems().values()) {
            if (item instanceof InteractionBattleBanzaiTile) {
                item.setExtraData("1");
                this.room.updateItemState(item);
                this.tileCount++;
                this.gameTiles.put(item.getId(), item);
            }

            if (item instanceof InteractionBattleBanzaiScoreboard) {
                item.setExtraData("0");
                this.room.updateItemState(item);
            }
        }
    }


    public void tileLocked(GameTeamColors teamColor, RoomItem item, Habbo habbo) {
        this.tileLocked(teamColor, item, habbo, false);
    }

    public void tileLocked(GameTeamColors teamColor, RoomItem item, Habbo habbo, boolean doNotCheckFill) {
        synchronized (this.lockedTiles) {
            if (item instanceof InteractionBattleBanzaiTile) {
                if (!this.lockedTiles.containsKey(teamColor)) {
                    this.lockedTiles.put(teamColor, new THashSet<>());
                }

                this.lockedTiles.get(teamColor).add(item);
            }

            if (habbo != null) {
                AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("BattleBallTilesLocked"));
            }

            if (doNotCheckFill) return;

            final int x = item.getCurrentPosition().getX();
            final int y = item.getCurrentPosition().getY();

            final List<List<RoomTile>> filledAreas = new ArrayList<>();
            final THashSet<RoomItem> lockedTiles = new THashSet<>(this.lockedTiles.get(teamColor));

            executor.execute(() -> {
                filledAreas.add(this.floodFill(x, y - 1, lockedTiles, new ArrayList<>(), teamColor));
                filledAreas.add(this.floodFill(x, y + 1, lockedTiles, new ArrayList<>(), teamColor));
                filledAreas.add(this.floodFill(x - 1, y, lockedTiles, new ArrayList<>(), teamColor));
                filledAreas.add(this.floodFill(x + 1, y, lockedTiles, new ArrayList<>(), teamColor));

                Optional<List<RoomTile>> largestAreaOfAll = filledAreas.stream().filter(Objects::nonNull).max(Comparator.comparing(List::size));

                if (largestAreaOfAll.isPresent()) {
                    for (RoomTile tile : largestAreaOfAll.get()) {
                        Optional<RoomItem> tileItem = this.gameTiles.values().stream().filter(i -> i.getCurrentPosition().getX() == tile.getX() && i.getCurrentPosition().getY() == tile.getY() && i instanceof InteractionBattleBanzaiTile).findAny();

                        tileItem.ifPresent(habboItem -> {
                            this.tileLocked(teamColor, habboItem, habbo, true);

                            habboItem.setExtraData((2 + (teamColor.type * 3)) + "");
                            this.room.updateItem(habboItem);
                        });
                    }

                    this.refreshCounters(teamColor);
                    if (habbo != null) {
                        habbo.getHabboInfo().getGamePlayer().addScore(BattleBanzaiGame.POINTS_LOCK_TILE * largestAreaOfAll.get().size());
                    }
                }
            });
        }
    }

    private List<RoomTile> floodFill(int x, int y, THashSet<RoomItem> lockedTiles, List<RoomTile> stack, GameTeamColors color) {
        if (this.isOutOfBounds(x, y) || this.isForeignLockedTile(x, y, color)) return null;

        RoomTile tile = this.room.getLayout().getTile((short) x, (short) y);

        if (this.hasLockedTileAtCoordinates(x, y, lockedTiles) || stack.contains(tile)) return stack;

        stack.add(tile);

        List<List<RoomTile>> result = new ArrayList<>();
        result.add(this.floodFill(x, y - 1, lockedTiles, stack, color));
        result.add(this.floodFill(x, y + 1, lockedTiles, stack, color));
        result.add(this.floodFill(x - 1, y, lockedTiles, stack, color));
        result.add(this.floodFill(x + 1, y, lockedTiles, stack, color));

        if (result.contains(null)) return null;

        Optional<List<RoomTile>> biggestArea = result.stream().max(Comparator.comparing(List::size));

        return biggestArea.orElse(null);

    }

    private boolean hasLockedTileAtCoordinates(int x, int y, THashSet<RoomItem> lockedTiles) {
        for (RoomItem item : lockedTiles) {
            if (item.getCurrentPosition().getX() == x && item.getCurrentPosition().getY() == y) return true;
        }

        return false;
    }

    private boolean isOutOfBounds(int x, int y) {
        for (RoomItem item : this.gameTiles.values()) {
            if (item.getCurrentPosition().getX() == x && item.getCurrentPosition().getY() == y) return false;
        }

        return true;
    }

    private boolean isForeignLockedTile(int x, int y, GameTeamColors color) {
        for (HashMap.Entry<GameTeamColors, THashSet<RoomItem>> lockedTilesForColor : this.lockedTiles.entrySet()) {
            if (lockedTilesForColor.getKey() == color) continue;

            for (RoomItem item : lockedTilesForColor.getValue()) {
                if (item.getCurrentPosition().getX() == x && item.getCurrentPosition().getY() == y) return true;
            }
        }

        return false;
    }

    public void refreshCounters() {
        for (GameTeam team : this.teams.values()) {
            if (team.getMembers().isEmpty())
                continue;

            this.refreshCounters(team.teamColor);
        }
    }


    public void refreshCounters(GameTeamColors teamColors) {
        if (!this.teams.containsKey(teamColors)) return;

        int totalScore = this.teams.get(teamColors).getTotalScore();

        THashMap<Integer, InteractionBattleBanzaiScoreboard> scoreBoards = this.room.getRoomSpecialTypes().getBattleBanzaiScoreboards(teamColors);

        for (InteractionBattleBanzaiScoreboard scoreboard : scoreBoards.values()) {
            if (scoreboard.getExtraData().isEmpty()) {
                scoreboard.setExtraData("0");
            }

            int oldScore = Integer.parseInt(scoreboard.getExtraData());

            if (oldScore == totalScore)
                continue;

            scoreboard.setExtraData(totalScore + "");
            this.room.updateItemState(scoreboard);
        }
    }

    private void refreshGates() {
        Collection<InteractionBattleBanzaiGate> gates = this.room.getRoomSpecialTypes().getBattleBanzaiGates().values();
        THashSet<RoomTile> tilesToUpdate = new THashSet<>(gates.size());
        for (RoomItem item : gates) {
            tilesToUpdate.add(this.room.getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY()));
        }

        this.room.updateTiles(tilesToUpdate);
    }

    public void markTile(Habbo habbo, InteractionBattleBanzaiTile tile, int state) {
        if (!this.gameTiles.contains(tile.getId())) return;

        int check = state - (habbo.getHabboInfo().getGamePlayer().getTeamColor().type * 3);
        if (check == 0 || check == 1) {
            state++;

            if (state % 3 == 2) {
                habbo.getHabboInfo().getGamePlayer().addScore(BattleBanzaiGame.POINTS_LOCK_TILE);
                this.tileLocked(habbo.getHabboInfo().getGamePlayer().getTeamColor(), tile, habbo);
            } else {
                habbo.getHabboInfo().getGamePlayer().addScore(BattleBanzaiGame.POINTS_FILL_TILE);
            }
        } else {
            state = habbo.getHabboInfo().getGamePlayer().getTeamColor().type * 3;

            habbo.getHabboInfo().getGamePlayer().addScore(BattleBanzaiGame.POINTS_HIJACK_TILE);
        }

        this.refreshCounters(habbo.getHabboInfo().getGamePlayer().getTeamColor());
        tile.setExtraData(state + "");
        this.room.updateItem(tile);
    }
}
