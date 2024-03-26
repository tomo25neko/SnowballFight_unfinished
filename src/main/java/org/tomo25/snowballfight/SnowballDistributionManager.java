package org.tomo25.snowballfight;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowballDistributionManager {

    private final SnowballFight plugin;
    private final SnowballFightManager snowballFightManager;

    // 雪玉を配布する間隔（秒）とプレイヤーが持てる雪玉の最大数
    private static final int SNOWBALL_GIVE_INTERVAL = 15;
    private static final int MAX_SNOWBALLS = 16;

    public SnowballDistributionManager(SnowballFight plugin, SnowballFightManager snowballFightManager) {
        this.plugin = plugin;
        this.snowballFightManager = snowballFightManager;
    }

    public void startSnowballDistribution() {
        int intervalTicks = 20 * SNOWBALL_GIVE_INTERVAL;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!snowballFightManager.isGameStarted()) {
                    this.cancel();
                    return;
                }

                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !isPlayerSpectator(player))
                        .forEach(player -> giveSnowballs(player, calculateSnowballsToAdd(player)));
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    private void giveSnowballs(Player player, int amount) {
        ItemStack snowballStack = new ItemStack(Material.SNOWBALL, amount);
        player.getInventory().addItem(snowballStack);
    }

    private int getPlayerSnowballCount(Player player) {
        return player.getInventory().all(Material.SNOWBALL).values().stream()
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    private boolean isPlayerSpectator(Player player) {
        return snowballFightManager.getTeamScoreManager().getPlayerTeam(player) == null;
    }

    private int calculateSnowballsToAdd(Player player) {
        int currentSnowballs = getPlayerSnowballCount(player);
        return Math.min(MAX_SNOWBALLS - currentSnowballs, 3);
    }
}