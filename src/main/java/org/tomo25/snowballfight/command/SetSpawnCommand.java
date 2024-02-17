package org.tomo25.snowballfight.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tomo25.snowballfight.GameTeam;
import org.tomo25.snowballfight.SnowballFightManager;

public class SetSpawnCommand implements CommandExecutor {

    private final SnowballFightManager snowballFightManager;

    public SetSpawnCommand(SnowballFightManager snowballFightManager) {
        this.snowballFightManager = snowballFightManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "コマンドの使用方法: /snowballfight setspawn <RedまたはBlue>");
            return true;
        }

        String team = args[0];
        if (team.equalsIgnoreCase("Red") || team.equalsIgnoreCase("Blue")) {
            setSpawnPoint(sender, team);
        } else {
            sender.sendMessage(ChatColor.RED + "無効なチーム名です。RedまたはBlueを指定してください。");
        }

        return true;
    }

    private void setSpawnPoint(CommandSender sender, String team) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location location = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY() - 1, player.getLocation().getZ());
            snowballFightManager.setSpawnLocation(GameTeam.valueOf(team.toUpperCase()), location);
            sender.sendMessage(ChatColor.GREEN + team + " チームのスポーンポイントを設定しました。");
        } else {
            sender.sendMessage(ChatColor.RED + "プレイヤーがコマンドを実行する必要があります。");
        }
    }
}
