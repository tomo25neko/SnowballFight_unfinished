package org.tomo25.snowballfight;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.tomo25.snowballfight.command.SetSpawnCommand;
import org.tomo25.snowballfight.command.StartCommand;
import org.tomo25.snowballfight.command.TeamSetCommand;
import org.tomo25.snowballfight.command.TimeCommand;

public final class SnowballFight extends JavaPlugin {

    private SnowballFightManager snowballFightManager;

    @Override
    public void onEnable() {
        getLogger().info("プラグインが有効になりました");

        snowballFightManager = new SnowballFightManager(this);
        new SnowballListener(snowballFightManager, this);
        new SnowballFightListener(snowballFightManager);

        Bukkit.getOnlinePlayers().forEach(this::sendPluginStatusMessage);

        getCommand("snowballfighttime").setExecutor(new TimeCommand(snowballFightManager));
        getCommand("snowballfightstart").setExecutor(new StartCommand(snowballFightManager));
        getCommand("snowballfightteam").setExecutor(new TeamSetCommand(snowballFightManager));
        getCommand("snowballfightsetspawn").setExecutor(new SetSpawnCommand(snowballFightManager));

        // ゲームがスタートしていない場合も1秒ごとにスコアボードを更新
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!snowballFightManager.isGameRunning()) {
                    Bukkit.getOnlinePlayers().forEach(SnowballFight.this::updatePlayerTeamDisplay);
                }
            }
        }.runTaskTimer(this, 0L, 20L); // 1秒ごとに実行
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayerTeamDisplay(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        updatePlayerTeamDisplay(event.getPlayer());
    }

    private void updatePlayerTeamDisplay(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("snowballFight", "dummy", "Snowball Fight");

        objective.getScore(" ").setScore(0); // Add blank space for better visibility

        String teamDisplay = snowballFightManager.getPlayerTeamDisplay(player);
        objective.getScore(ChatColor.GREEN + "あなたの所属: " + teamDisplay).setScore(1);
        objective.getScore(ChatColor.GOLD + "プレイヤー数: " + Bukkit.getOnlinePlayers().size()).setScore(2);

        if (snowballFightManager.isPlayerSpectator(player)) {
            objective.getScore(ChatColor.GRAY + "観戦者").setScore(3);
        } else {
            int redTeamPlayers = snowballFightManager.getTeamScoreManager().getRedTeamSize();
            int blueTeamPlayers = snowballFightManager.getTeamScoreManager().getBlueTeamSize();
            objective.getScore(ChatColor.RED + "赤チーム人数: " + redTeamPlayers).setScore(4);
            objective.getScore(ChatColor.BLUE + "青チーム人数: " + blueTeamPlayers).setScore(5);
        }
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
    }

    @Override
    public void onDisable() {
        getLogger().info("プラグインが無効になりました");

        // プラグインが無効になったときに全プレイヤーのスコアボードとスコアを削除
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard playerScoreboard = player.getScoreboard();
            playerScoreboard.getEntries().forEach(playerScoreboard::resetScores);
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    private void sendPluginStatusMessage(Player player) {
        player.sendMessage("SnowballFightプラグインが" + (isEnabled() ? "有効" : "無効") + "になりました！");
    }
}
