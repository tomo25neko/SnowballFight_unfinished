package org.tomo25.snowballfight.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
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

            // 既存のスポーン地点がある場合はアーマースタンドを削除
            snowballFightManager.getSpawnPointManager().removeArmorStand(GameTeam.valueOf(team.toUpperCase()));

            // スポーン地点設定
            snowballFightManager.setSpawnLocation(GameTeam.valueOf(team.toUpperCase()), location);

            // アーマースタンド設置
            spawnArmorStand(location, team);

            sender.sendMessage(ChatColor.GREEN + team + " チームのスポーンポイントとアーマースタンドを設定しました。");
        } else {
            sender.sendMessage(ChatColor.RED + "プレイヤーがコマンドを実行する必要があります。");
        }
    }


    private void spawnArmorStand(Location location, String team) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, 1, 0), org.bukkit.entity.EntityType.ARMOR_STAND);

        // アーマースタンドの名前をチームに応じて設定
        armorStand.setCustomName(team.toLowerCase() + "spawn");
        armorStand.setCustomNameVisible(true);

        // アーマースタンドに染色された革装備を装備させる
        GameTeam gameTeam = GameTeam.valueOf(team.toUpperCase());
        snowballFightManager.getSpawnPointManager().equipColoredLeatherArmor(armorStand, gameTeam.getColor());
    }
}
