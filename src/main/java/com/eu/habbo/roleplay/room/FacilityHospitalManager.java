package com.eu.habbo.roleplay.room;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FacilityHospitalManager {

    private static FacilityHospitalManager instance;

    public static FacilityHospitalManager getInstance() {
        if (instance == null) {
            instance = new FacilityHospitalManager();
        }
        return instance;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(FacilityHospitalManager.class);
    private final List<Habbo> usersToHeal;
    private FacilityHospitalManager() {
        long millis = System.currentTimeMillis();
        this.usersToHeal = new CopyOnWriteArrayList<>();
        LOGGER.info("Hospital Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
    }
    public Room getHospital() {
        return FacilityManager.getFirstRoomWithTag(RoomType.HOSPITAL);
    }

    public void addUserToHeal(Habbo user) {
        this.usersToHeal.add(user);
        user.shout(Emulator.getTexts().getValue("roleplay.hospital.starts_healing"));
    }

    public void removeUserToHeal(Habbo user) {
        if (this.usersToHeal.isEmpty()) {
            return;
        }
        if (!user.getHabboRoleplayStats().isCuffed() && !user.getHabboRoleplayStats().isStunned()) {
            user.getRoomUnit().setCanWalk(true);
        }
        this.usersToHeal.remove(user);
        user.shout(Emulator.getTexts().getValue("roleplay.hospital.stops_healing"));
    }

    public void cycle() {
        for (Habbo user : usersToHeal) {
            if ((user.getHabboRoleplayStats().getHealthNow() + 1) > user.getHabboRoleplayStats().getHealthMax()) {
                this.removeUserToHeal(user);
                return;
            }
            user.getHabboRoleplayStats().setHealth(user.getHabboRoleplayStats().getHealthNow() + 1);
        }
    }

    public void dispose() {
    }
}