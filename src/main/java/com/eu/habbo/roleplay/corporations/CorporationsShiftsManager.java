package com.eu.habbo.roleplay.corporations;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CorporationsShiftsManager {

    private static CorporationsShiftsManager instance;

    public static CorporationsShiftsManager getInstance() {
        if (instance == null) {
            instance = new CorporationsShiftsManager();
        }
        return instance;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CorporationsShiftsManager.class);

    private final Map<Integer, CorporationShift> activeUserShifts;
    private final ScheduledExecutorService scheduler;

    public CorporationsShiftsManager() {
        long millis = System.currentTimeMillis();
        this.activeUserShifts = new HashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.startShiftManager();
        LOGGER.info("Corporations Shift Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
    }


    public void startUserShift(Habbo habbo) {
        this.activeUserShifts.put(habbo.getHabboInfo().getId(), new CorporationShift(habbo));
    }

    public void stopUserShift(Habbo habbo) {
        this.stopUserShift(habbo, false, false);
    }

    public void stopUserShift(Habbo habbo, boolean shiftCompleted, boolean startNewShift) {
        CorporationShift userShift = this.activeUserShifts.get(habbo.getHabboInfo().getId());
        if (!this.isUserWorking(habbo) || userShift == null) {
            habbo.whisper(Emulator.getTexts().getValue("commands.roleplay.cmd_stop_work_no_shift"));
            return;
        }
        Corporation userEmployer = CorporationsManager.getInstance().getCorporationByID(habbo.getHabboRoleplayStats().getCorporationID());
        if (userEmployer == null) {
            habbo.whisper(Emulator.getTexts().getValue("commands.roleplay.corporation_shift_cannot_pay_no_job"));
            return;
        }
        CorporationPosition userPosition =  userEmployer.getPositionByID(habbo.getHabboRoleplayStats().getCorporationPositionID());
        if (userPosition == null) {
            habbo.whisper(Emulator.getTexts().getValue("commands.roleplay.corporation_shift_cannot_pay_no_job"));
            return;
        }
        if (!shiftCompleted) {
            habbo.whisper(Emulator.getTexts().getValue("commands.roleplay.corporation_shift_not_complete"));
            return;
        }
        habbo.giveCredits(userPosition.getSalary());
        this.activeUserShifts.remove(habbo.getHabboInfo().getId());
        if (startNewShift) {
            this.startUserShift(habbo);
        }
    }

    public boolean isUserWorking(Habbo habbo) {
        return this.getUserShift(habbo) != null;
    }

    public CorporationShift getUserShift(Habbo habbo) {
        return this.activeUserShifts.get(habbo.getHabboInfo().getId());
    }

    public void startShiftManager() {
        Runnable userShift = () -> {
            for (CorporationShift shift : activeUserShifts.values()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime >= shift.getEndTime()) {
                    this.stopUserShift(shift.getHabbo(), true, true);
                    return;
                }

                long ONE_MINUTE_IN_MS = 60000;

                long timeLeft = (shift.getEndTime() - currentTime) / ONE_MINUTE_IN_MS;
                long shiftLength = CorporationShift.SHIFT_LENGTH_IN_MS / ONE_MINUTE_IN_MS;

                String shiftTimeLeftMsg = Emulator.getTexts().getValue("commands.roleplay.corporation_shift_time_left")
                        .replace("%timeLeft%", Long.toString(timeLeft))
                        .replace("%shiftLength%", Long.toString(shiftLength));
                shift.getHabbo().shout(shiftTimeLeftMsg);
            }
        };

        // Schedule the task to run every 5 seconds
        scheduler.scheduleAtFixedRate(userShift, 0, 60, TimeUnit.SECONDS);
    }

    public void stopShiftManager() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    public void dispose() {
        this.stopShiftManager();
    }
}