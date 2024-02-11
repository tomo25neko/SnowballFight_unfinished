package org.tomo25.snowballfight.command;

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
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("start")) {
            if (snowballFightManager.getTime() > 0) {
                player.sendMessage(ChatColor.RED + "ゲームは既に開始されています！");
            } else {
                snowballFightManager.startGame();
                player.sendMessage(ChatColor.GREEN + "Snowball Fightゲームを開始しました！");
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "正しい使い方: /snowballfightstart");
            return false;
        }
    }
}
