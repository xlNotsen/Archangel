package com.eu.habbo.messages.incoming.guild;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guild.GuildMembersComposer;
import com.eu.habbo.messages.outgoing.guild.GuildMembershipRejectedMessageComposer;
import com.eu.habbo.messages.outgoing.guild.HabboGroupDetailsMessageComposer;
import com.eu.habbo.plugin.events.guilds.GuildDeclinedMembershipEvent;

public class RejectMembershipRequestEvent extends MessageHandler {
    @Override
    public void handle() {
        int guildId = this.packet.readInt();
        int userId = this.packet.readInt();

        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(guildId);

        if (guild != null) {
            GuildMember member = Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, this.client.getHabbo());
            if (userId == this.client.getHabbo().getHabboInfo().getId() || guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || member.getRank().equals(GuildRank.ADMIN)|| member.getRank().equals(GuildRank.OWNER) || this.client.getHabbo().hasPermissionRight(Permission.ACC_GUILD_ADMIN)) {
                guild.decreaseRequestCount();
                Emulator.getGameEnvironment().getGuildManager().removeMember(guild, userId);
                this.client.sendResponse(new GuildMembersComposer(guild, Emulator.getGameEnvironment().getGuildManager().getGuildMembers(guild, 0, 0, ""), this.client.getHabbo(), 0, 0, "", true, Emulator.getGameEnvironment().getGuildManager().getGuildMembersCount(guild, 0, "")));
                this.client.sendResponse(new GuildMembershipRejectedMessageComposer(guild, userId));

                Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(userId);
                Emulator.getPluginManager().fireEvent(new GuildDeclinedMembershipEvent(guild, userId, habbo, this.client.getHabbo()));

                if (habbo != null) {
                    Room room = habbo.getRoomUnit().getRoom();
                    if (room != null) {
                        if (room.getRoomInfo().getGuild().getId() == guildId) {
                            habbo.getClient().sendResponse(new HabboGroupDetailsMessageComposer(guild, habbo.getClient(), false, null));
                        }
                    }
                }
            }
        }
    }
}
