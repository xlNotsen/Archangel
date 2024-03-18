package com.eu.habbo.habbohotel.commands.list;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.modtool.WordFilter;
import com.eu.habbo.habbohotel.modtool.WordFilterWord;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
public class FilterWordCommand extends Command {
    public FilterWordCommand() {
        super("cmd_filterword");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params.length < 2) {
            gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_filterword.missing_word"));
            return true;
        }

        String word = params[1];

        String replacement = WordFilter.DEFAULT_REPLACEMENT;
        if (params.length == 3) {
            replacement = params[2];
        }

        WordFilterWord wordFilterWord = new WordFilterWord(word, replacement);

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO wordfilter (`key`, `replacement`) VALUES (?, ?)")) {
            statement.setString(1, word);
            statement.setString(2, replacement);
            statement.execute();
        } catch (SQLException e) {
            log.error("Caught SQL exception", e);
            gameClient.getHabbo().whisper(getTextsValue("commands.error.cmd_filterword.error"));
            return true;
        }

        gameClient.getHabbo().whisper(getTextsValue("commands.succes.cmd_filterword.added").replace("%word%", word).replace("%replacement%", replacement));
        Emulator.getGameEnvironment().getWordFilter().addWord(wordFilterWord);

        return true;
    }
}