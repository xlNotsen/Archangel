package com.eu.habbo.habbohotel.items.interactions.wired.extra;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.battlebanzai.BattleBanzaiGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.items.RoomItemManager;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class WiredBlob extends InteractionDefault {
    

    @Getter
    @AllArgsConstructor
    enum WiredBlobState {
        ACTIVE("0"),
        USED("1");

        private final String state;
    }

    private int POINTS_REWARD = 0;
    private boolean RESETS_WITH_GAME = true;

    public WiredBlob(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);

        this.parseCustomParams();
    }

    public WiredBlob(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);

        this.parseCustomParams();
    }

    @Override
    public void onPlace(Room room) {
        super.onPlace(room);

        this.setExtraData(WiredBlobState.USED.getState());
        room.updateItem(this);
    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOn(roomUnit, room, objects);

        if (!this.getExtraData().equals(WiredBlobState.ACTIVE.getState())) return;

        Habbo habbo = room.getRoomUnitManager().getHabboByRoomUnit(roomUnit);

        if (habbo != null) {
            GamePlayer player = habbo.getHabboInfo().getGamePlayer();

            if (player != null) {
                player.addScore(this.POINTS_REWARD, true);

                BattleBanzaiGame battleBanzaiGame = (BattleBanzaiGame) room.getGame(BattleBanzaiGame.class);

                if (battleBanzaiGame != null && battleBanzaiGame.getState() != GameState.IDLE) {
                    battleBanzaiGame.refreshCounters(habbo.getHabboInfo().getGamePlayer().getTeamColor());
                }

                this.setExtraData(WiredBlobState.USED.getState());
                room.updateItem(this);
            }
        }
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) {
        if (!this.RESETS_WITH_GAME && objects != null && objects.length == 2 && objects[1].equals(WiredEffectType.TOGGLE_STATE) && room.getGames().stream().anyMatch(game -> game.getState().equals(GameState.RUNNING) || game.getState().equals(GameState.PAUSED))) {
            this.setExtraData(this.getExtraData().equals(WiredBlobState.ACTIVE.getState()) ? WiredBlobState.USED.getState() : WiredBlobState.ACTIVE.getState());
            room.updateItem(this);
        }
    }

    public void onGameStart(Room room) {
        if (this.RESETS_WITH_GAME) {
            this.setExtraData(WiredBlobState.ACTIVE.getState());
            room.updateItem(this);
        }
    }

    public void onGameEnd(Room room) {
        this.setExtraData(WiredBlobState.USED.getState());
        room.updateItem(this);
    }

    private void parseCustomParams() {
        String[] params = this.getBaseItem().getCustomParams().split(",");

        if (params.length != 2) {
            log.error("Wired blobs should have customparams with two parameters (points,resetsWithGame)");
            return;
        }

        try {
            this.POINTS_REWARD = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            log.error("Wired blobs should have customparams with the first parameter being the amount of points (number)");
            return;
        }

        this.RESETS_WITH_GAME = params[1].equalsIgnoreCase("true");
    }

    @Override
    public void removeThisItem(RoomItemManager roomItemManager) {
        synchronized (roomItemManager.getUndefinedSpecials()) {
            roomItemManager.getUndefinedSpecials().remove(getId());
        }
    }

    @Override
    public void addThisItem(RoomItemManager roomItemManager) {
        synchronized (roomItemManager.getUndefinedSpecials()) {
            roomItemManager.getUndefinedSpecials().put(getId(), this);
        }
    }
}
