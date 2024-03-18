package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class CreditsScheduler extends Scheduler {


    public static boolean IGNORE_HOTEL_VIEW;
    public static boolean IGNORE_IDLED;
    public static double HC_MODIFIER;

    public CreditsScheduler() {

        super(Emulator.getConfig().getInt("hotel.auto.credits.interval"));
        this.reloadConfig();
    }

    public void reloadConfig() {
        if (Emulator.getConfig().getBoolean("hotel.auto.credits.enabled")) {
            IGNORE_HOTEL_VIEW = Emulator.getConfig().getBoolean("hotel.auto.credits.ignore.hotelview");
            IGNORE_IDLED = Emulator.getConfig().getBoolean("hotel.auto.credits.ignore.idled");
            HC_MODIFIER = Emulator.getConfig().getDouble("hotel.auto.credits.hc_modifier", 1.0);

            if (this.disposed) {
                this.disposed = false;
                this.run();
            }
        } else {
            this.disposed = true;
        }
    }

    @Override
    public void run() {
        super.run();

        Habbo habbo;
        for (Map.Entry<Integer, Habbo> map : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
            habbo = map.getValue();

            try {
                if (habbo != null) {
                    if (habbo.getRoomUnit().getRoom() == null && IGNORE_HOTEL_VIEW)
                        continue;

                    if (habbo.getRoomUnit().isIdle() && IGNORE_IDLED)
                        continue;

                    habbo.giveCredits((int) (habbo.getHabboInfo().getPermissionGroup().getTimerAmount(-1) * (habbo.getHabboStats().hasActiveClub() ? HC_MODIFIER : 1.0)));
                }
            } catch (Exception e) {
                log.error("Caught exception", e);
            }
        }
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    public void setDisposed(boolean disposed) {
        this.disposed = disposed;
    }
}
