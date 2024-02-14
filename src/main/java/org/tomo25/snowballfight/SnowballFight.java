package org.tomo25.snowballfight;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.tomo25.snowballfight.command.SetSpawnCommand;
import org.tomo25.snowballfight.command.StartCommand;
import org.tomo25.snowballfight.command.TeamSetCommand;
import org.tomo25.snowballfight.command.TimeCommand;

public final class SnowballFight extends JavaPlugin {

    private SnowballFightManager snowballFightManager;

    @Override
    public void onEnable() {
        getLogger().info("プラグインが有効になりました");

        snowballFightManager = new SnowballFightManager(this);
        new SnowballListener(snowballFightManager, this);
        // SnowballFightListenerの呼び出し
        new SnowballFightListener(snowballFightManager);


        Bukkit.getOnlinePlayers().forEach(this::sendPluginStatusMessage);

        getCommand("snowballfighttime").setExecutor(new TimeCommand(snowballFightManager));
        getCommand("snowballfightstart").setExecutor(new StartCommand(snowballFightManager));
        getCommand("snowballfightteam").setExecutor(new TeamSetCommand(snowballFightManager));
        // SetSpawnCommandを登録
        getCommand("snowballfight").setExecutor(new SetSpawnCommand(snowballFightManager));
    }

    private void updatePlayerTeamDisplay(Player player) {
        int totalPlayers = Bukkit.getOnlinePlayers().size();
        int redTeamPlayers = snowballFightManager.getTeamScoreManager().getRedTeamSize();
        int blueTeamPlayers = snowballFightManager.getTeamScoreManager().getBlueTeamSize();

        String teamDisplay = snowballFightManager.getPlayerTeamDisplay(player);
        String playerCountDisplay = ChatColor.GOLD + "プレイヤー数: " + totalPlayers + ChatColor.RESET;

        player.sendTitle("", teamDisplay, 10, 70, 20);
        player.sendMessage(ChatColor.GREEN + "あなたの所属: " + teamDisplay);
        player.sendMessage(playerCountDisplay);
        player.sendMessage(ChatColor.RED + "赤チーム人数: " + redTeamPlayers);
        player.sendMessage(ChatColor.BLUE + "青チーム人数: " + blueTeamPlayers);
    }

    @Override
    public void onDisable() {
        getLogger().info("プラグインが無効になりました");
    }

    private void sendPluginStatusMessage(Player player) {
        player.sendMessage("SnowballFightプラグインが" + (isEnabled() ? "有効" : "無効") + "になりました！");
    }
}
