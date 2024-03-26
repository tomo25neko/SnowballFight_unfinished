package org.tomo25.snowballfight;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

public final class SnowballFight extends JavaPlugin implements Listener {

    private SnowballFightManager snowballFightManager;

    @Override
    public void onEnable() {
        getLogger().info("プラグインが有効になりました");

        // SnowballFightManagerのインスタンスを作成し、リスナーを登録
        snowballFightManager = new SnowballFightManager(this);
        new SnowballListener(snowballFightManager, this);
        new SnowballFightListener(snowballFightManager);

        // オンラインのプレイヤーにプラグインのステータスメッセージを送信
        Bukkit.getOnlinePlayers().forEach(this::sendPluginStatusMessage);

        // コマンドの処理クラスを登録
        if (getCommand("settime") != null) {
            getCommand("settime").setExecutor(new TimeCommand(snowballFightManager));
        } else {
            getLogger().warning("コマンド settime が見つかりませんでした。");
        }

        if (getCommand("gamestart") != null) {
            getCommand("gamestart").setExecutor(new StartCommand(snowballFightManager));
        } else {
            getLogger().warning("コマンド gamestart が見つかりませんでした。");
        }

        if (getCommand("setteam") != null) {
            getCommand("setteam").setExecutor(new TeamSetCommand(snowballFightManager));
        } else {
            getLogger().warning("コマンド setteam が見つかりませんでした。");
        }

        if (getCommand("setspawn") != null) {
            getCommand("setspawn").setExecutor(new SetSpawnCommand(snowballFightManager));
        } else {
            getLogger().warning("コマンド setspawn が見つかりませんでした。");
        }

        // ゲームが開始していない場合に1秒ごとにプレイヤーのスコアボードを更新
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!snowballFightManager.isGameStarted()) {
                    Bukkit.getOnlinePlayers().forEach(SnowballFight.this::updatePlayerTeamDisplay);
                }
            }
        }.runTaskTimer(this, 0L, 20L); // 1秒ごとに実行
    }

    // プレイヤーがサーバーに参加したときの処理
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayerTeamDisplay(event.getPlayer());
    }

    // プレイヤーがサーバーから退出したときの処理
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        updatePlayerTeamDisplay(event.getPlayer());
    }

    // プレイヤーのスコアボードを更新するメソッド
    private void updatePlayerTeamDisplay(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("snowballFight", "dummy", "Snowball Fight");

        objective.getScore(" ").setScore(0); // スコアボードを見やすくするための空行

        String teamDisplay = snowballFightManager.getPlayerTeamDisplay(player);
        objective.getScore(ChatColor.GREEN + "所属: " + teamDisplay).setScore(1);
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

        // プラグインが無効になったときに全プレイヤーのスコアボードをリセット
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard playerScoreboard = player.getScoreboard();
            playerScoreboard.getEntries().forEach(playerScoreboard::resetScores);
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    // プラグインのステータスメッセージをプレイヤーに送信するメソッド
    private void sendPluginStatusMessage(Player player) {
        player.sendMessage("SnowballFightプラグインが" + (isEnabled() ? "有効" : "無効") + "になりました！");
    }
}
