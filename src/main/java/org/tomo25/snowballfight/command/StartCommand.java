package org.tomo25.snowballfight.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
            // チームもしくは観戦者に追加されていないプレイヤーがいる場合
            if (!validatePlayers()) {
                player.sendMessage(ChatColor.RED + "エラー: チームもしくは観戦者に追加されていないプレイヤーがいます。/snowballfight teamset を使用してプレイヤーを追加してください。");
                return false; // 条件を満たさない場合はキャンセル
            }

            if (snowballFightManager.getTime() <= 0) {
                player.sendMessage(ChatColor.RED + "ゲームの時間が設定されていません！");
            } else if (snowballFightManager.isGameStarted()) {
                player.sendMessage(ChatColor.RED + "ゲームは既に開始されています！");
                return false; // ゲームが既に開始されている場合はコマンドの実行をキャンセル
            } else {
                snowballFightManager.startGame();
                player.sendMessage(ChatColor.GREEN + "Snowball Fightゲームを開始しました！");
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "正しい使い方: /snowballfightstart start");
            return false;
        }
    }




    private boolean validatePlayers() {
        // チームもしくは観戦者に追加されていないプレイヤーがいるか確認
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (snowballFightManager.getTeamScoreManager().getPlayerTeam(onlinePlayer) == null) {
                return false;
            }
        }
        return true;
    }
}
