package org.tomo25.snowballfight;

import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class SnowballDamageHandler implements Listener {
    private final TeamScoreManager teamScoreManager;
    private final SnowballFightManager snowballFightManager;

    // コンストラクタ
    public SnowballDamageHandler(SnowballFightManager snowballFightManager) {
        this.teamScoreManager = snowballFightManager.getTeamScoreManager();
        this.snowballFightManager = snowballFightManager;
    }

    // 雪玉がプレイヤーに当たった時のイベント処理
    @EventHandler
    public void onSnowballHit(EntityDamageByEntityEvent event) {
        // 雪玉がダメージを与える場合のみ処理を続行
        if (!(event.getDamager() instanceof Snowball)) {
            event.setCancelled(true);
            return;
        }

        // 雪玉の発射者がプレイヤーでない場合は処理を終了
        Snowball snowball = (Snowball) event.getDamager();
        ProjectileSource shooter = snowball.getShooter();
        if (!(shooter instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        // 攻撃者と被攻撃者を取得
        Player attacker = (Player) shooter;
        if (!(event.getEntity() instanceof Player)) {
            return; // 被攻撃者がプレイヤーでない場合は処理を終了
        }
        Player victim = (Player) event.getEntity();

        // いずれかの条件が満たされていない場合はイベントをキャンセル
        if (victim == null || attacker.equals(victim) || !snowballFightManager.isGameStarted()) {
            event.setCancelled(true); // イベントをキャンセル
            return;
        }

        // 攻撃者と被攻撃者が適切な距離にいる場合のみ処理を実行
        if (isValidSnowballDistance(attacker, victim)) {
            // 攻撃者のチームと被攻撃者のチームが異なる場合のみスコアを更新
            GameTeam attackerTeam = teamScoreManager.getPlayerTeam(attacker);
            GameTeam victimTeam = teamScoreManager.getPlayerTeam(victim);
            if (attackerTeam != null && victimTeam != null && !attackerTeam.equals(victimTeam)) {
                updateGameScore(attackerTeam, victim);
            }
        }
    }

    // 攻撃者と被攻撃者の距離が有効な範囲内にあるかどうかを確認する
    private boolean isValidSnowballDistance(Player attacker, Player victim) {
        double maxDistanceSquared = 25.0; // 最大距離の2乗
        return attacker.getLocation().distanceSquared(victim.getLocation()) <= maxDistanceSquared;
    }

    // ゲームスコアを更新する
    private void updateGameScore(GameTeam attackerTeam, Player victim) {
        // 攻撃者のチームのスコアを更新
        teamScoreManager.increaseTeamScore(attackerTeam);
        // 被攻撃者をキルする
        victim.setHealth(0);
    }
}

