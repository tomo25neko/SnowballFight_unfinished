package org.tomo25.snowballfight;

import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class SnowballDamageHandler implements Listener {
    private final TeamScoreManager teamScoreManager;

    // コンストラクタ
    public SnowballDamageHandler(SnowballFightManager snowballFightManager) {
        this.teamScoreManager = snowballFightManager.getTeamScoreManager();
    }


    // 雪玉がプレイヤーに当たった時のイベント処理
    @EventHandler
    public void onSnowballHit(EntityDamageByEntityEvent event) {
        // ダメージを与えたエンティティが雪玉でない場合は処理を終了
        if (!(event.getDamager() instanceof Snowball)) {
            return;
        }

        // 雪玉の発射者がプレイヤーでない場合は処理を終了
        Snowball snowball = (Snowball) event.getDamager();
        ProjectileSource shooter = snowball.getShooter();
        if (!(shooter instanceof Player)) {
            return;
        }

        // 攻撃者と被攻撃者を取得
        Player attacker = (Player) shooter;
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();

        // 攻撃者と被攻撃者のチームを取得
        GameTeam attackerTeam = teamScoreManager.getPlayerTeam(attacker);
        GameTeam victimTeam = teamScoreManager.getPlayerTeam(victim);

        // 攻撃者と被攻撃者の両方がチームに所属しており、異なるチームである場合のみ処理を実行
        if (attackerTeam != null && victimTeam != null && !attackerTeam.equals(victimTeam)) {
            // 攻撃者が青チームの場合
            if (attackerTeam == GameTeam.BLUE) {
                // 青チームのスコアを1加算し、被攻撃者の体力を0に設定してキルを表現
                teamScoreManager.increaseTeamScore(GameTeam.BLUE);
                victim.setHealth(0);
            }
            // 攻撃者が赤チームの場合
            else if (attackerTeam == GameTeam.RED) {
                // 赤チームのスコアを1加算し、被攻撃者の体力を0に設定してキルを表現
                teamScoreManager.increaseTeamScore(GameTeam.RED);
                victim.setHealth(0);
            }
        }
    }
}
