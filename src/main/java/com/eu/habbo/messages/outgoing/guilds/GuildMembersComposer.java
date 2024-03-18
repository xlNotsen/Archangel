package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

@AllArgsConstructor
public class GuildMembersComposer extends MessageComposer {
    private final Guild guild;
    private final ArrayList<GuildMember> members;
    private final Habbo session;
    private final int pageId;
    private final int level;
    private final String searchValue;
    private final boolean isAdmin;
    private final int totalCount;


    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.guildMembersComposer);
        this.response.appendInt(this.guild.getId());
        this.response.appendString(this.guild.getName());
        this.response.appendInt(this.guild.getRoomId());
        this.response.appendString(this.guild.getBadge());
        this.response.appendInt(this.totalCount);
        this.response.appendInt(this.members.size());

        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        for (GuildMember member : this.members) {
            cal.setTimeInMillis(member.getJoinDate() * 1000L);
            this.response.appendInt(member.getRank().getType());
            this.response.appendInt(member.getUserId());
            this.response.appendString(member.getUsername());
            this.response.appendString(member.getLook());
            this.response.appendString(member.getRank().getType() < 3 ? cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR) : "");
        }

        this.response.appendBoolean(this.isAdmin);
        this.response.appendInt(14);
        this.response.appendInt(this.pageId);
        this.response.appendInt(this.level);
        this.response.appendString(this.searchValue);
        return this.response;
    }
}
