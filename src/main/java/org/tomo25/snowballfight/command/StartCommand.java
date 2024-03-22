package org.tomo25.snowballfight.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tomo25.snowballfight.GameTeam;
import org.tomo25.snowballfight.SnowballFightManager;

public class StartCommand implements CommandExecutor {

    private final SnowballFightManager snowballFightManager;

    public StartCommand(SnowballFightManager snowballFightManager) {
        this.snowballFightManager = snowballFightManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみが実行できます。");
            return false; // プレイヤー以外からの実行はキャンセル
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("start")) {
            // ゲームの時間が設定されていない場合はエラーメッセージを送信して終了
            if (snowballFightManager.getTime() == 0) {
                player.sendMessage(ChatColor.RED + "ゲームの時間が設定されていません！");
                return false;
            }

            // スタート地点が設定されているか確認
            if (snowballFightManager.getSpawnPointManager().getSpawnPoint(GameTeam.RED) == null || snowballFightManager.getSpawnPointManager().getSpawnPoint(GameTeam.BLUE) == null) {
                Bukkit.broadcastMessage(ChatColor.RED + "エラー: 赤チームと青チームのスタート地点が設定されていません！");
                return false;
            }

            // ゲームが既に開始されている場合はエラーメッセージを送信して終了
            if (snowballFightManager.isGameStarted()) {
                player.sendMessage(ChatColor.RED + "ゲームは既に開始されています！");
                return false; // ゲームが既に開始されている場合はコマンドの実行をキャンセル
            }

            // チームに所属していないプレイヤーを観戦者に追加し、全プレイヤーにメッセージを送信
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (snowballFightManager.getTeamScoreManager().getPlayerTeam(onlinePlayer) == null) {
                    snowballFightManager.addPlayerToSpectator(onlinePlayer);
                    onlinePlayer.sendMessage(ChatColor.YELLOW + "チームに所属していないプレイヤーは、観戦者として参加します。");
                }
            }

            // ゲームを開始して、全プレイヤーにメッセージを送信
            snowballFightManager.startGame();
            Bukkit.broadcastMessage(ChatColor.GREEN + "まもなくゲームが開始されます！");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "正しい使い方: /gamestart start");
            return false;
        }
    }
}
