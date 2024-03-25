package org.tomo25.snowballfight;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class SnowballDamageHandler implements Listener {
    private final SnowballFightManager snowballFightManager;
    private final TeamScoreManager teamScoreManager;

    public SnowballDamageHandler(SnowballFightManager snowballFightManager) {
        this.snowballFightManager = snowballFightManager;
        this.teamScoreManager = snowballFightManager.getTeamScoreManager();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || !snowballFightManager.isGameStarted()) {
            return;
        }

        // イベントが EntityDamageByEntityEvent でない場合、処理を終了
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
        Entity damager = entityEvent.getDamager();

        // ダメージを与えたエンティティが Projectile でない場合、処理を終了
        if (!(damager instanceof Projectile)) {
            return;
        }

        Projectile projectile = (Projectile) damager;

        // ダメージを与えた Projectile が雪玉でない場合、処理を終了
        if (!(projectile instanceof Snowball)) {
            return;
        }

        // ダメージを与えたエンティティがプレイヤーかどうかを確認
        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) projectile.getShooter();
        Player player = (Player) event.getEntity();
        GameTeam shooterTeam = teamScoreManager.getPlayerTeam(shooter);
        GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);

        // ダメージを与えたプレイヤーが相手チームのメンバーであるかどうかを確認
        if (shooterTeam != null && playerTeam != null && shooterTeam != playerTeam) {
            // 相手チームのメンバーにポイントを加算
            if (playerTeam == GameTeam.RED) {
                teamScoreManager.increaseTeamScore(GameTeam.BLUE);
            } else {
                teamScoreManager.increaseTeamScore(GameTeam.RED);
            }
            // プレイヤーをキルする
            player.setHealth(0);
        }
    }
}
