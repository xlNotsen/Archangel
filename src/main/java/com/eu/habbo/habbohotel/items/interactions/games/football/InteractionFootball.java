package com.eu.habbo.habbohotel.items.interactions.games.football;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.games.football.FootballGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionPushable;
import com.eu.habbo.habbohotel.items.interactions.games.football.goals.InteractionFootballGoal;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.constants.RoomTileState;
import com.eu.habbo.habbohotel.rooms.entities.RoomRotation;
import com.eu.habbo.habbohotel.rooms.items.entities.RoomItem;
import com.eu.habbo.habbohotel.rooms.entities.units.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.outgoing.rooms.items.OneWayDoorStatusMessageComposer;
import com.eu.habbo.util.pathfinding.Rotation;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;


public class InteractionFootball extends InteractionPushable {

    public InteractionFootball(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionFootball(int id, HabboInfo ownerInfo, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, ownerInfo, item, extradata, limitedStack, limitedSells);
    }


    @Override
    public int getWalkOnVelocity(RoomUnit roomUnit, Room room) {
        if (roomUnit.getPath().isEmpty() && roomUnit.getTilesMoved() == 2 && this.getExtraData().equals("1"))
            return 0;

        if (roomUnit.getPath().isEmpty() && roomUnit.getTilesMoved() == 1)
            return 6;

        return 1;
    }

    @Override
    public int getWalkOffVelocity(RoomUnit roomUnit, Room room) {
        if (roomUnit.getPath().isEmpty() && roomUnit.getTilesMoved() == 0)
            return 6;

        return 1;
    }

    @Override
    public int getDragVelocity(RoomUnit roomUnit, Room room) {
        if (roomUnit.getPath().isEmpty() && roomUnit.getTilesMoved() == 2)
            return 0;

        return 1;
    }

    @Override
    public int getTackleVelocity(RoomUnit roomUnit, Room room) {
        return 4;
    }


    @Override
    public RoomRotation getWalkOnDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    @Override
    public RoomRotation getWalkOffDirection(RoomUnit roomUnit, Room room) {
        RoomTile peek = roomUnit.getPath().peek();
        RoomTile nextWalkTile;
        if (peek != null) {
            nextWalkTile = room.getLayout().getTile(peek.getX(), peek.getY());
        } else {
            nextWalkTile = roomUnit.getTargetPosition();
        }
        return RoomRotation.values()[(RoomRotation.values().length + Rotation.Calculate(roomUnit.getCurrentPosition().getX(), roomUnit.getCurrentPosition().getY(), nextWalkTile.getX(), nextWalkTile.getY()) + 4) % 8];
    }

    public RoomRotation getDragDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    public RoomRotation getTackleDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }


    @Override
    public int getNextRollDelay(int currentStep, int totalSteps) {

        if (totalSteps > 4 && currentStep <= 4) {
            return 125;
        }

        return 500;
    }

    @Override
    public RoomRotation getBounceDirection(Room room, RoomRotation currentDirection) {
        switch (currentDirection) {
            default:
            case NORTH:
                return RoomRotation.SOUTH;

            case NORTH_EAST:
                if (this.validMove(room, room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), RoomRotation.NORTH_WEST.getValue())))
                    return RoomRotation.NORTH_WEST;
                else if (this.validMove(room, room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), RoomRotation.SOUTH_EAST.getValue())))
                    return RoomRotation.SOUTH_EAST;
                else
                    return RoomRotation.SOUTH_WEST;

            case EAST:
                return RoomRotation.WEST;

            case SOUTH_EAST:
                if (this.validMove(room, room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), RoomRotation.SOUTH_WEST.getValue())))
                    return RoomRotation.SOUTH_WEST;
                else if (this.validMove(room, room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), RoomRotation.NORTH_EAST.getValue())))
                    return RoomRotation.NORTH_EAST;
                else
                    return RoomRotation.NORTH_WEST;

            case SOUTH:
                return RoomRotation.NORTH;

            case SOUTH_WEST:
                if (this.validMove(room, room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), RoomRotation.SOUTH_EAST.getValue())))
                    return RoomRotation.SOUTH_EAST;
                else if (this.validMove(room, room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), RoomRotation.NORTH_WEST.getValue())))
                    return RoomRotation.NORTH_WEST;
                else
                    return RoomRotation.NORTH_EAST;

            case WEST:
                return RoomRotation.EAST;

            case NORTH_WEST:
                if (this.validMove(room, room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), RoomRotation.NORTH_EAST.getValue())))
                    return RoomRotation.NORTH_EAST;
                else if (this.validMove(room, room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()), RoomRotation.SOUTH_WEST.getValue())))
                    return RoomRotation.SOUTH_WEST;
                else
                    return RoomRotation.SOUTH_EAST;
        }
    }


    @Override
    public boolean validMove(Room room, RoomTile from, RoomTile to) {
        if (to == null || to.getState() == RoomTileState.INVALID) return false;
        RoomItem topItem = room.getRoomItemManager().getTopItemAt(to.getX(), to.getY(), this);

        // Move is valid if there isnt any furni yet
        if (topItem == null) {
            return true;
        }

        // If any furni on tile is not stackable, move is invalid (tested on 22-03-2022)
        if (room.getRoomItemManager().getItemsAt(to).stream().anyMatch(x -> !x.getBaseItem().allowStack())) {
            return false;
        }

        // Ball can only go up by 1.65 according to Habbo (tested using stack tile on 22-03-2022)
        BigDecimal topItemHeight = BigDecimal.valueOf(topItem.getCurrentZ() + topItem.getBaseItem().getHeight());
        BigDecimal ballHeight = BigDecimal.valueOf(this.getCurrentZ());

        if (topItemHeight.subtract(ballHeight).compareTo(BigDecimal.valueOf(1.65)) > 0) {
            return false;
        }

        // If top item is a football goal, the move is only valid if ball is coming from the front side
        // Ball shouldn't come from the back or from the sides (tested on 22-03-2022)
        if (topItem instanceof InteractionFootballGoal) {
            int ballDirection = Rotation.Calculate(from.getX(), from.getY(), to.getX(), to.getY());
            int goalRotation = topItem.getRotation();

            return switch (goalRotation) {
                case 0 -> ballDirection > 2 && ballDirection < 6;
                case 2 -> ballDirection > 4;
                case 4 -> ballDirection > 6 || ballDirection < 2;
                case 6 -> ballDirection > 0 && ballDirection < 4;
                default -> topItem.getBaseItem().allowStack();
            };
        }

        return topItem.getBaseItem().allowStack();
    }

    //Events

    @Override
    public void onDrag(Room room, RoomUnit roomUnit, int velocity, RoomRotation direction) {

    }

    @Override
    public void onKick(Room room, RoomUnit roomUnit, int velocity, RoomRotation direction) {

    }

    @Override
    public void onTackle(Room room, RoomUnit roomUnit, int velocity, RoomRotation direction) {

    }

    @Override
    public void onMove(Room room, RoomTile from, RoomTile to, RoomRotation direction, RoomUnit kicker, int nextRoll, int currentStep, int totalSteps) {
        FootballGame game = (FootballGame) room.getGame(FootballGame.class);
        if (game == null) {
            try {
                game = FootballGame.class.getDeclaredConstructor(Room.class).newInstance(room);
                room.addGame(game);
            } catch (Exception e) {
                return;
            }
        }
        RoomItem currentTopItem = room.getRoomItemManager().getTopItemAt(from.getX(), from.getY(), this);
        RoomItem topItem = room.getRoomItemManager().getTopItemAt(to.getX(), to.getY(), this);
        if ((topItem != null) && ((currentTopItem == null) || (currentTopItem.getId() != topItem.getId())) && topItem instanceof InteractionFootballGoal interactionFootballGoal) {
            GameTeamColors color = interactionFootballGoal.teamColor;
            game.onScore(kicker, color);
        }

        this.setExtraData(Math.abs(currentStep - (totalSteps + 1)) + "");
        room.sendComposer(new OneWayDoorStatusMessageComposer(this).compose());
    }

    @Override
    public void onBounce(Room room, RoomRotation oldDirection, RoomRotation newDirection, RoomUnit kicker) {

    }

    @Override
    public void onStop(Room room, RoomUnit kicker, int currentStep, int totalSteps) {
        this.setExtraData("0");
        room.sendComposer(new OneWayDoorStatusMessageComposer(this).compose());
    }

    @Override
    public boolean canStillMove(Room room, RoomTile from, RoomTile to, RoomRotation direction, RoomUnit kicker, int nextRoll, int currentStep, int totalSteps) {
        RoomItem topItem = room.getRoomItemManager().getTopItemAt(from.getX(), from.getY(), this);
        return !((Emulator.getRandom().nextInt(10) >= 3 && room.getRoomUnitManager().hasHabbosAt(to)) || (topItem != null && topItem.getBaseItem().getName().startsWith("fball_goal_") && currentStep != 1));
    }

    @Override
    public void onPickUp(Room room) {
        this.setExtraData("0");
    }

}