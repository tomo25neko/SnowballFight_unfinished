package org.tomo25.snowballfight.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tomo25.snowballfight.GameTeam;
import org.tomo25.snowballfight.SnowballFightManager;

public class SetStartPointCommand implements CommandExecutor {

    private final SnowballFightManager snowballFightManager;

    public SetStartPointCommand(SnowballFightManager snowballFightManager) {
        this.snowballFightManager = snowballFightManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "プレイヤーのみがこのコマンドを実行できます。");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "使用法: /setstartpoint <red|blue>");
            return true;
        }

        String teamArg = args[0].toLowerCase();

        if (teamArg.equals("red")) {
            setStartPoint(player, GameTeam.RED);
        } else if (teamArg.equals("blue")) {
            setStartPoint(player, GameTeam.BLUE);
        } else {
            player.sendMessage(ChatColor.RED + "使用法: /setstartpoint <red|blue>");
        }

        return true;
    }

    private void setStartPoint(Player player, GameTeam team) {
        snowballFightManager.setStartPoint(player, team);
        player.sendMessage(ChatColor.GREEN + "スタート地点を設定しました。");
    }
}