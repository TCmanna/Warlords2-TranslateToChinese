package com.ebicep.warlords.game.option;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.option.marker.scoreboard.ScoreboardHandler;
import com.ebicep.warlords.game.option.marker.scoreboard.SimpleScoreboardHandler;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.util.warlords.GameRunnable;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class BasicScoreboardOption implements Option{

    @Override
    public void register(Game game) {
        game.registerGameMarker(ScoreboardHandler.class, getDateScoreboard(game));
        game.registerGameMarker(ScoreboardHandler.class, getVersionScoreboard(game));
        game.registerGameMarker(ScoreboardHandler.class, getStatsScoreboard(game));
        game.registerGameMarker(ScoreboardHandler.class, getSpecScoreboard(game));
    }

    private static SimpleScoreboardHandler getDateScoreboard(Game game) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy - kk:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        SimpleScoreboardHandler simpleScoreboardHandler = new SimpleScoreboardHandler(0, "date") {
            @Nonnull
            @Override
            public List<String> computeLines(@Nullable WarlordsPlayer player) {
                return Collections.singletonList(ChatColor.GRAY + format.format(new Date()));
            }
        };
        new GameRunnable(game) {
            @Override
            public void run() {
                simpleScoreboardHandler.markChanged();
            } 
        }.runTaskTimer(GameRunnable.SECOND, GameRunnable.SECOND);
        return simpleScoreboardHandler;
    }
    
    private static SimpleScoreboardHandler getVersionScoreboard(Game game) {
        return new SimpleScoreboardHandler(Integer.MAX_VALUE, "version") {
            @Nonnull
            @Override
            public List<String> computeLines(@Nullable WarlordsPlayer player) {
                return Collections.singletonList(ChatColor.YELLOW + Warlords.VERSION);
            }
        };
    }
    
    private static SimpleScoreboardHandler getStatsScoreboard(Game game) {
        // TODO trigger scoreboard update when the kills/assists changes
        return new SimpleScoreboardHandler(Integer.MAX_VALUE - 1, "player-stats") {
            @Nonnull
            @Override
            public List<String> computeLines(@Nullable WarlordsPlayer player) {
                return player == null ? Collections.singletonList("")
                        : Collections.singletonList(ChatColor.GREEN.toString()
                        + player.getMinuteStats().total().getKills() + ChatColor.RESET + " Kills "
                        + ChatColor.GREEN + player.getMinuteStats().total().getAssists() + ChatColor.RESET + " Assists");
            }
        };
    }
    
    private static SimpleScoreboardHandler getSpecScoreboard(Game game) {
        return new SimpleScoreboardHandler(Integer.MAX_VALUE - 2, "spec") {
            @Nonnull
            @Override
            public List<String> computeLines(@Nullable WarlordsPlayer player) {
                return player == null ? Collections.singletonList("")
                        : Collections.singletonList(ChatColor.WHITE + "Spec: " + ChatColor.GREEN + player.getSpec().getClass().getSimpleName());
            }
        };
    }

}
