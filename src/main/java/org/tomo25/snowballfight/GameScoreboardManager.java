package org.tomo25.snowballfight;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class GameScoreboardManager {

    private final SnowballFight plugin;
    private final TeamScoreManager teamScoreManager;

    public GameScoreboardManager(SnowballFight plugin, TeamScoreManager teamScoreManager) {
        this.plugin = plugin;
        this.teamScoreManager = teamScoreManager;
    }

    public void updatePlayerTeamDisplay(Player player, int time) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("snowballFight");

        if (objective == null) {
            objective = scoreboard.registerNewObjective("snowballFight", "dummy");
            objective.setDisplayName(ChatColor.BOLD + "Snowball Fight");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        int redTeamScore = teamScoreManager.getTeamScore(GameTeam.RED);
        int blueTeamScore = teamScoreManager.getTeamScore(GameTeam.BLUE);
        int playerCount = Bukkit.getOnlinePlayers().size();
        int redTeamSize = teamScoreManager.getRedTeamSize();
        int blueTeamSize = teamScoreManager.getBlueTeamSize();

        Score timeScore = objective.getScore(ChatColor.AQUA + "残り時間: " + ChatColor.YELLOW + time);
        timeScore.setScore(7);
        Score redTeamPlayerScore = objective.getScore(ChatColor.RED + "赤チームスコア: " + redTeamScore);
        redTeamPlayerScore.setScore(6);
        Score blueTeamPlayerScore = objective.getScore(ChatColor.BLUE + "青チームスコア: " + blueTeamScore);
        blueTeamPlayerScore.setScore(5);
        Score serverPlayerCountScore = objective.getScore(ChatColor.GREEN + "総プレイヤー数: " + playerCount);
        serverPlayerCountScore.setScore(4);
        Score redTeamSizeScore = objective.getScore(ChatColor.GREEN + "赤チーム人数: " + redTeamSize);
        redTeamSizeScore.setScore(3);
        Score blueTeamSizeScore = objective.getScore(ChatColor.GREEN + "青チーム人数: " + blueTeamSize);
        blueTeamSizeScore.setScore(2);

        player.setScoreboard(scoreboard);
    }
}
