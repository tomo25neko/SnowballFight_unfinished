package org.tomo25.snowballfight;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowballDistributionManager {

    // プラグインとSnowballFightManagerのインスタンスを保持するフィールド
    private final SnowballFight plugin;
    private final SnowballFightManager snowballFightManager;

    // 雪玉を配布する間隔（秒）とプレイヤーが持てる雪玉の最大数
    private final int snowballGiveInterval = 15;
    private final int maxSnowballs = 16;

    // コンストラクタ
    public SnowballDistributionManager(SnowballFight plugin, SnowballFightManager snowballFightManager) {
        this.plugin = plugin;
        this.snowballFightManager = snowballFightManager;
    }

    // 雪玉の配布を開始するメソッド
    public void startSnowballDistribution() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // ゲームが開始されていない場合は処理を中止する
                if (!snowballFightManager.isGameStarted()) {
                    return;
                }

                // オンラインのプレイヤーに対して雪玉を配布する
                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !isPlayerSpectator(player))
                        .forEach(player -> {
                            int currentSnowballs = getPlayerSnowballCount(player);
                            int snowballsToAdd = Math.min(maxSnowballs - currentSnowballs, 3);
                            giveSnowballs(player, snowballsToAdd);
                        });
            }
        }.runTaskTimer(plugin, 0L, 20L * snowballGiveInterval); // 定期的なタイマータスクを実行
    }

    // プレイヤーに雪玉を配布するメソッド
    private void giveSnowballs(Player player, int amount) {
        ItemStack snowballStack = new ItemStack(Material.SNOWBALL, amount);
        player.getInventory().addItem(snowballStack);
    }

    // プレイヤーが持っている雪玉の数を取得するメソッド
    private int getPlayerSnowballCount(Player player) {
        return player.getInventory().all(Material.SNOWBALL).values().stream()
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    // プレイヤーが観戦者かどうかを判定するメソッド
    private boolean isPlayerSpectator(Player player) {
        // プレイヤーの所属チームが null の場合は観戦者とみなす
        return snowballFightManager.getTeamScoreManager().getPlayerTeam(player) == null;
    }
}
