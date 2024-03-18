package com.eu.habbo.habbohotel.games;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GamePlayer {

    private final Habbo habbo;
    private final GameTeamColors teamColor;

    private int score;
    private int wiredScore;

    public void reset() {
        this.score = 0;
        this.wiredScore = 0;
    }

    public synchronized void addScore(int amount) {
        addScore(amount, false);
    }

    public synchronized void addScore(int amount, boolean isWired) {
        if (habbo.getHabboInfo().getGamePlayer() != null && this.habbo.getHabboInfo().getCurrentGame() != null && this.habbo.getRoomUnit().getRoom().getGame(this.habbo.getHabboInfo().getCurrentGame()).getTeamForHabbo(this.habbo) != null) {
            this.score += amount;

            if (this.score < 0) this.score = 0;

            if(isWired && this.score > 0) {
                this.wiredScore += amount;
            }

            WiredHandler.handle(WiredTriggerType.SCORE_ACHIEVED, this.habbo.getRoomUnit(), this.habbo.getRoomUnit().getRoom(), new Object[]{this.habbo.getRoomUnit().getRoom().getGame(this.habbo.getHabboInfo().getCurrentGame()).getTeamForHabbo(this.habbo).getTotalScore(), amount});
        }
    }

    public Habbo getHabbo() {
        return this.habbo;
    }


    public GameTeamColors getTeamColor() {
        return this.teamColor;
    }


    public int getScore() {
        return this.score;
    }

    public int getScoreAchievementValue() {
        return this.score - this.wiredScore;
    }
}
