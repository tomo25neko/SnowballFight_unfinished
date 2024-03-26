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
    private static final String ERROR_PLAYER_ALREADY_IN_TEAM = ChatColor.RED + "エラー: プレイヤー %s は既にチームに所属しています。";

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
                case "addplayer":
                    if (args.length >= 3) {
                        String teamName = args[1].toUpperCase();
                        String playerName = args[2];

                        GameTeam team = GameTeam.getTeamByName(teamName);
                        if (team == null) {
                            player.sendMessage(ERROR_INVALID_TEAM);
                            return true;
                        }

                        Player targetPlayer = Bukkit.getPlayer(playerName);
                        if (targetPlayer == null) {
                            player.sendMessage(String.format(ERROR_PLAYER_NOT_FOUND, playerName));
                            return true;
                        }

                        if (snowballFightManager.isPlayerInTeam(targetPlayer, team)) {
                            player.sendMessage(String.format(ERROR_PLAYER_ALREADY_IN_TEAM, playerName));
                            return true;
                        }

                        processPlayerTeamAssignment(targetPlayer, team, ChatColor.GREEN + playerName + " を%sチームに追加しました.");
                        return true;
                    } else {
                        player.sendMessage(ERROR_MISSING_SUBCOMMAND);
                        return true;
                    }
                case "teamset":
                    assignPlayerToTeamByGlass(player);
                    return true;
            }
        }
        player.sendMessage(ERROR_MISSING_SUBCOMMAND);
        return true;
    }


    public void processPlayerTeamAssignment(Player target, GameTeam team, String messageFormat) {
        snowballFightManager.addPlayerToTeam(target, team);
        target.sendMessage(String.format(messageFormat, team.getTeamName()));
        teamScoreManager.increaseTeamScore(team);
    }

    public boolean checkGlassExists(Location location, Material material) {
        return location.getBlock().getType() == material;
    }

    public void assignPlayerToTeamByGlass(Player player) {
        Location location = player.getLocation();

        if (checkGlassExists(location.clone().add(0, -1, 0), Material.BLUE_STAINED_GLASS)) {
            processPlayerTeamAssignment(player, GameTeam.BLUE, ChatColor.GREEN + player.getName() + " を青チームに追加しました.");
        } else if (checkGlassExists(location.clone().add(0, -1, 0), Material.RED_STAINED_GLASS)) {
            processPlayerTeamAssignment(player, GameTeam.RED, ChatColor.GREEN + player.getName() + " を赤チームに追加しました.");
        } else {
            // どちらの上にもいない場合は観戦者として追加

        }
    }
}
