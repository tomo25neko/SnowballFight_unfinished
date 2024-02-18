package org.tomo25.snowballfight.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

    // エラーメッセージの定数化
    private static final String PLAYER_ONLY_COMMAND = ChatColor.RED + "このコマンドはプレイヤーのみが実行できます。";
    private static final String ERROR_MISSING_SUBCOMMAND = ChatColor.RED + "エラー: サブコマンドが必要です。使用法: /snowballfight [teamset|addplayer] [Red|Blue]";
    private static final String ERROR_INVALID_TEAM = ChatColor.RED + "エラー: 無効なチーム名です。有効な値は 'red' または 'blue' です。";
    private static final String ERROR_PLAYER_NOT_FOUND = ChatColor.RED + "エラー: プレイヤー %s が見つかりません。";

    // 他のエラーメッセージも定数として追加...

    public TeamSetCommand(SnowballFightManager snowballFightManager) {
        this.snowballFightManager = snowballFightManager;
        this.teamScoreManager = snowballFightManager.getTeamScoreManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PLAYER_ONLY_COMMAND);
            return true;
        }

        Player player = (Player) sender;

        if (args.length >= 1) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "teamset":
                    if (!checkGlassExists(player.getLocation(), Material.RED_STAINED_GLASS) || !checkGlassExists(player.getLocation(), Material.BLUE_STAINED_GLASS)) {
                        player.sendMessage(ChatColor.RED + "エラー: 赤と青の色付きガラスが両方存在していません。");
                        return true;
                    }

                    for (Player target : Bukkit.getOnlinePlayers()) {
                        String message = ChatColor.GREEN + target.getName() + " を%sチームに追加しました.";

                        if (target.getLocation().getBlock().getType() == Material.RED_STAINED_GLASS && target.getLocation().getY() <= 3) {
                            processPlayerTeamAssignment(target, GameTeam.RED, message, "赤");
                        } else if (target.getLocation().getBlock().getType() == Material.BLUE_STAINED_GLASS && target.getLocation().getY() <= 3) {
                            processPlayerTeamAssignment(target, GameTeam.BLUE, message, "青");
                        } else {
                            snowballFightManager.addPlayerToSpectator(target);
                            player.sendMessage(ChatColor.GREEN + target.getName() + " を観戦者に設定しました。");
                        }
                    }
                    return true;
                // 他のケースの処理も続く...
            }
        }
        player.sendMessage(ERROR_MISSING_SUBCOMMAND);
        return true;
    }

    public void processPlayerTeamAssignment(Player target, GameTeam team, String messageFormat, String teamName) {
        snowballFightManager.addPlayerToTeam(target, team);  // 修正
        target.sendMessage(String.format(messageFormat, teamName));  // 修正
        teamScoreManager.increaseTeamScore(team);
    }

    public boolean checkGlassExists(Location location, Material material) {
        return location.getBlock().getType() == material;
    }
}