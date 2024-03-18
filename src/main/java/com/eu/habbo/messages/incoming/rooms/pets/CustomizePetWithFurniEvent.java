package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.pets.HorsePet;
import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.constants.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.PetFigureUpdateComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.PetStatusUpdateComposer;
import com.eu.habbo.messages.outgoing.rooms.users.UserUpdateComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;

public class CustomizePetWithFurniEvent extends MessageHandler {
    @Override
    public void handle() {
        int itemId = this.packet.readInt();

        Room room = this.client.getHabbo().getRoomUnit().getRoom();
        if (room == null)
            return;

        RoomItem item = this.client.getHabbo().getRoomUnit().getRoom().getRoomItemManager().getRoomItemById(itemId);

        if (item == null)
            return;

        int petId = this.packet.readInt();
        Pet pet = this.client.getHabbo().getRoomUnit().getRoom().getRoomUnitManager().getRoomPetManager().getRoomPetById(petId);

        if (pet instanceof HorsePet) {
            if (item.getBaseItem().getName().toLowerCase().startsWith("horse_dye")) {
                int race = Integer.parseInt(item.getBaseItem().getName().split("_")[2]);
                int raceType = (race * 4) - 2;

                if (race >= 13 && race <= 17)
                    raceType = ((2 + race) * 4) + 1;

                if (race == 0)
                    raceType = 0;

                pet.setRace(raceType);
                pet.setSqlUpdateNeeded(true);
            } else if (item.getBaseItem().getName().toLowerCase().startsWith("horse_hairdye")) {
                int splittedHairdye = Integer.parseInt(item.getBaseItem().getName().toLowerCase().split("_")[2]);
                int newHairdye = 48;

                if (splittedHairdye == 0) {
                    newHairdye = -1;
                } else if (splittedHairdye == 1) {
                    newHairdye = 1;
                } else if (splittedHairdye >= 13 && splittedHairdye <= 17) {
                    newHairdye = 68 + splittedHairdye;
                } else {
                    newHairdye += splittedHairdye;
                }

                ((HorsePet) pet).setHairColor(newHairdye);
                pet.setSqlUpdateNeeded(true);
            } else if (item.getBaseItem().getName().toLowerCase().startsWith("horse_hairstyle")) {
                int splittedHairstyle = Integer.parseInt(item.getBaseItem().getName().toLowerCase().split("_")[2]);
                int newHairstyle = 100;

                if (splittedHairstyle == 0) {
                    newHairstyle = -1;
                } else {
                    newHairstyle += splittedHairstyle;
                }

                ((HorsePet) pet).setHairStyle(newHairstyle);
                pet.setSqlUpdateNeeded(true);
            } else if (item.getBaseItem().getName().toLowerCase().startsWith("horse_saddle")) {
                ((HorsePet) pet).hasSaddle(true);
                ((HorsePet) pet).setSaddleItemId(item.getBaseItem().getId());
                pet.setSqlUpdateNeeded(true);
            }

            if (pet.isSqlUpdateNeeded()) {
                Emulator.getThreading().run(pet);
                this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new PetFigureUpdateComposer((HorsePet) pet).compose());

                room.getRoomItemManager().removeRoomItem(item);
                room.sendComposer(new RemoveFloorItemComposer(item, true).compose());
                //Deprecated
                item.setRoomId(0);
                item.setRoom(null);
                Emulator.getGameEnvironment().getItemManager().deleteItem(item);
            }
        } else if (pet instanceof MonsterplantPet) {
            if (item.getBaseItem().getName().equalsIgnoreCase("mnstr_revival")) {
                if (((MonsterplantPet) pet).isDead()) {
                    ((MonsterplantPet) pet).setDeathTimestamp(Emulator.getIntUnixTimestamp() + MonsterplantPet.TIME_TO_LIVE);
                    pet.getRoomUnit().clearStatuses();
                    pet.getRoomUnit().addStatus(RoomUnitStatus.GESTURE, "rev");
                    pet.setPacketUpdate(true);

                    this.client.getHabbo().getRoomUnit().getRoom().getRoomItemManager().removeRoomItem(item);
                    this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new RemoveFloorItemComposer(item).compose());
                    this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new UserUpdateComposer(pet.getRoomUnit()).compose());
                    this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new PetStatusUpdateComposer(pet).compose());
                    this.client.getHabbo().getRoomUnit().getRoom().updateTiles(room.getLayout().getTilesAt(room.getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY()), item.getBaseItem().getWidth(), item.getBaseItem().getLength(), item.getRotation()));
                    AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("MonsterPlantHealer"));
                    pet.getRoomUnit().removeStatus(RoomUnitStatus.GESTURE);
                    Emulator.getThreading().run(new QueryDeleteHabboItem(item.getId()));
                }
            } else if (item.getBaseItem().getName().equalsIgnoreCase("mnstr_fert")) {
                if (!((MonsterplantPet) pet).isFullyGrown()) {
                    pet.setCreated(pet.getCreated() - MonsterplantPet.GROW_TIME);
                    pet.getRoomUnit().clearStatuses();
                    pet.cycle();
                    pet.getRoomUnit().addStatus(RoomUnitStatus.GESTURE, "spd");
                    pet.getRoomUnit().addStatus(RoomUnitStatus.fromString("grw" + ((MonsterplantPet) pet).getGrowthStage()), "");
                    this.client.getHabbo().getRoomUnit().getRoom().getRoomItemManager().removeRoomItem(item);
                    this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new RemoveFloorItemComposer(item).compose());
                    this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new UserUpdateComposer(pet.getRoomUnit()).compose());
                    this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new PetStatusUpdateComposer(pet).compose());
                    this.client.getHabbo().getRoomUnit().getRoom().updateTiles(room.getLayout().getTilesAt(room.getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY()), item.getBaseItem().getWidth(), item.getBaseItem().getLength(), item.getRotation()));
                    pet.getRoomUnit().removeStatus(RoomUnitStatus.GESTURE);
                    pet.cycle();
                    Emulator.getThreading().run(new QueryDeleteHabboItem(item.getId()));
                }
            } else if (item.getBaseItem().getName().startsWith("mnstr_rebreed")) {
                if (((MonsterplantPet) pet).isFullyGrown() && !((MonsterplantPet) pet).canBreed()) {
                    if (
                            (item.getBaseItem().getName().equalsIgnoreCase("mnstr_rebreed") && ((MonsterplantPet) pet).getRarity() <= 5) ||
                                    (item.getBaseItem().getName().equalsIgnoreCase("mnstr_rebreed_2") && ((MonsterplantPet) pet).getRarity() >= 6 && ((MonsterplantPet) pet).getRarity() <= 8) ||
                                    (item.getBaseItem().getName().equalsIgnoreCase("mnstr_rebreed_3") && ((MonsterplantPet) pet).getRarity() >= 9)
                            )

                    {
                        ((MonsterplantPet) pet).setCanBreed(true);
                        pet.getRoomUnit().clearStatuses();
                        pet.getRoomUnit().addStatus(RoomUnitStatus.GESTURE, "reb");

                        this.client.getHabbo().getRoomUnit().getRoom().getRoomItemManager().removeRoomItem(item);
                        this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new RemoveFloorItemComposer(item).compose());
                        this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new UserUpdateComposer(pet.getRoomUnit()).compose());
                        this.client.getHabbo().getRoomUnit().getRoom().sendComposer(new PetStatusUpdateComposer(pet).compose());
                        this.client.getHabbo().getRoomUnit().getRoom().updateTiles(room.getLayout().getTilesAt(room.getLayout().getTile(item.getCurrentPosition().getX(), item.getCurrentPosition().getY()), item.getBaseItem().getWidth(), item.getBaseItem().getLength(), item.getRotation()));
                        pet.getRoomUnit().removeStatus(RoomUnitStatus.GESTURE);
                        Emulator.getThreading().run(new QueryDeleteHabboItem(item.getId()));
                    }
                }
            }
        }
    }
}