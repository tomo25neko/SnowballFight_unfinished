package org.tomo25.snowballfight;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowballDistributionManager {

    private final SnowballFight plugin;
    private final int snowballGiveInterval = 15; // 雪玉を配布する間隔（秒）
    private final int maxSnowballs = 16; // プレイヤーが持てる雪玉の最大数
    private final SnowballFightManager snowballFightManager; // SnowballFightManagerのインスタンスを保持するフィールド


    public SnowballDistributionManager(SnowballFight plugin, SnowballFightManager snowballFightManager) {
        this.plugin = plugin;
        this.snowballFightManager = snowballFightManager; // SnowballFightManagerのインスタンスを設定
    }

    public void startSnowballDistribution() {
        // メソッドの中身を変更せず、引数なしで呼び出すように修正
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !isPlayerSpectator(player, snowballFightManager))
                        .forEach(player -> {
                            int currentSnowballs = getPlayerSnowballCount(player);
                            if (currentSnowballs < maxSnowballs && snowballFightManager.isGameStarted()) {
                                int snowballsToAdd = Math.min(maxSnowballs - currentSnowballs, 3);
                                giveSnowballs(player, snowballsToAdd);
                            }
                        });
            }
        }.runTaskTimer(plugin, 0L, 20L * snowballGiveInterval);
    }


    private void giveSnowballs(Player player, int amount) {
        ItemStack snowballStack = new ItemStack(Material.SNOWBALL, amount);
        player.getInventory().addItem(snowballStack);
    }

    private int getPlayerSnowballCount(Player player) {
        int count = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack itemStack : contents) {
            if (itemStack != null && itemStack.getType() == Material.SNOWBALL) {
                count += itemStack.getAmount();
            }
        }
        return count;
    }

    private boolean isPlayerSpectator(Player player, SnowballFightManager snowballFightManager) {
        GameTeam playerTeam = snowballFightManager.getTeamScoreManager().getPlayerTeam(player);
        return playerTeam == null; // プレイヤーのチームが null の場合は観戦者と判断
    }
}
