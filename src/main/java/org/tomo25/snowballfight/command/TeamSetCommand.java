package org.tomo25.snowballfight.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tomo25.snowballfight.SnowballFightManager;
import org.tomo25.snowballfight.TeamScoreManager;
import org.tomo25.snowballfight.GameTeam;

public class TeamSetCommand implements CommandExecutor {

    private final SnowballFightManager snowballFightManager;
    private final TeamScoreManager teamScoreManager;

    public TeamSetCommand(SnowballFightManager snowballFightManager) {
        this.snowballFightManager = snowballFightManager;
        this.teamScoreManager = snowballFightManager.getTeamScoreManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみが実行できます。");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1 && args[0].equalsIgnoreCase("teamset")) {
            int redTeamY = 3;
            Material redTeamGlass = Material.RED_STAINED_GLASS;

            int blueTeamY = 3;
            Material blueTeamGlass = Material.BLUE_STAINED_GLASS;

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.getLocation().getBlock().getType() == redTeamGlass && target.getLocation().getY() <= redTeamY) {
                    snowballFightManager.addPlayerToRedTeam(target, GameTeam.RED);
                    player.sendMessage(ChatColor.GREEN + target.getName() + " を赤チームに追加しました。");
                    teamScoreManager.increaseTeamScore(GameTeam.RED);
                } else if (target.getLocation().getBlock().getType() == blueTeamGlass && target.getLocation().getY() <= blueTeamY) {
                    snowballFightManager.addPlayerToBlueTeam(target, GameTeam.BLUE);
                    player.sendMessage(ChatColor.GREEN + target.getName() + " を青チームに追加しました。");
                    teamScoreManager.increaseTeamScore(GameTeam.BLUE);
                } else {
                    snowballFightManager.addPlayerToSpectator(target);
                    player.sendMessage(ChatColor.GREEN + target.getName() + " を観戦者に設定しました。");
                }
            }
            return true;
        }

        return false;
    }
}
