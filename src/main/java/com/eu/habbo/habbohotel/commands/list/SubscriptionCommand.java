package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.constants.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboStats;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Beny
 */
public class SubscriptionCommand extends Command {
    public SubscriptionCommand() {
        super("cmd_subscription");
    }

    /**
     * Allows you to give/extend/remove subscription on a given user.
     * <p>
     * Parameters:
     * [username] = Username of user to execute command on
     * [type] = Subscription type (e.g. HABBO_CLUB)
     * [add|remove] = Use add or remove to increase/decrease sub duration
     * [time] = Time string e.g. "1 week", "18 days", "4 minutes". Can be complex e.g. "1 month 5 days 2 minutes"
     * <p>
     * Examples:
     * :sub Beny habbo_club add 1 month - adds 1 month of HABBO_CLUB subscription duration on the user Beny
     * :sub Beny builders_club add 1 month - adds 1 month of BUILDERS_CLUB subscription duration on the user Beny
     * :sub Beny habbo_club remove 3 days - removes 3 days of HABBO_CLUB subscription duration on the user Beny
     * :sub Beny habbo_club remove - removes all remaining time from the HABBO_CLUB subscription (expires it) on the user Beny
     *
     * @param gameClient Client that executed the command
     * @param params     Command parameters
     * @return Boolean indicating success
     */
    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params.length < 4) {
            gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_subscription.invalid_params", "Invalid command format"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        HabboInfo info = Emulator.getGameEnvironment().getHabboManager().getOfflineHabboInfo(params[1]);

        if (info == null) {
            gameClient.getHabbo().whisper(replaceUser(getTextsValue("commands.error.cmd_subscription.user_not_found", "%user% was not found"), params[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }

        HabboStats stats = info.getHabboStats();
        String subscription = params[2].toUpperCase();
        String action = params[3];

        String message = "";
        if (params.length > 4) {
            message = IntStream.range(4, params.length).mapToObj(i -> params[i] + " ").collect(Collectors.joining());
        }

        if (!Emulator.getGameEnvironment().getSubscriptionManager().getTypes().containsKey(subscription)) {
            gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_subscription.type_not_found", "%subscription% is not a valid subscription type").replace("%subscription%", subscription), RoomChatMessageBubbles.ALERT);
            return true;
        }

        if (action.equalsIgnoreCase("add") || action.equalsIgnoreCase("+") || action.equalsIgnoreCase("a")) {
            int timeToAdd = Emulator.timeStringToSeconds(message);

            if (timeToAdd < 1) {
                gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_subscription.invalid_params_time", "Invalid time span, try: x minutes/days/weeks/months"), RoomChatMessageBubbles.ALERT);
                return true;
            }

            stats.createSubscription(subscription, timeToAdd);
            gameClient.getHabbo().whisper(replaceUser(getTextsValue("commands.error.cmd_subscription.success_add_time", "Successfully added %time% seconds to %subscription% on %user%"), params[1]).replace("%time%", timeToAdd + "").replace("%subscription%", subscription), RoomChatMessageBubbles.ALERT);
        } else if (action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("-") || action.equalsIgnoreCase("r")) {
            Subscription s = stats.getSubscription(subscription);

            if (s == null) {
                gameClient.getHabbo().whisper(replaceUser(getTextsValue("commands.error.cmd_subscription.user_not_have", "%user% does not have the %subscription% subscription"), params[1]).replace("%subscription%", subscription), RoomChatMessageBubbles.ALERT);
                return true;
            }

            if (message.length() != 0) {
                int timeToRemove = Emulator.timeStringToSeconds(message);

                if (timeToRemove < 1) {
                    gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_subscription.invalid_params_time", "Invalid time span, try: x minutes/days/weeks/months"), RoomChatMessageBubbles.ALERT);
                    return true;
                }

                s.addDuration(-timeToRemove);
                gameClient.getHabbo().whisper(replaceUser(getTextsValue("commands.error.cmd_subscription.success_remove_time", "Successfully removed %time% seconds from %subscription% on %user%"), params[1]).replace("%time%", timeToRemove + "").replace("%subscription%", subscription), RoomChatMessageBubbles.ALERT);
            } else {
                s.addDuration(-s.getRemaining());
                gameClient.getHabbo().whisper(replaceUser(getTextsValue("commands.error.cmd_subscription.success_remove_sub", "Successfully removed %subscription% sub from %user%"), params[1]).replace("%subscription%", subscription), RoomChatMessageBubbles.ALERT);
            }
        } else {
            gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_subscription.invalid_action", "Invalid action specified. Must be add, +, remove or -"), RoomChatMessageBubbles.ALERT);
        }

        return true;
    }
}
