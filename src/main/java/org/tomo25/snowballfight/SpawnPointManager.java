package org.tomo25.snowballfight;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class SpawnPointManager extends JavaPlugin {

    private final Map<GameTeam, Location> spawnPoints;
    private final Map<GameTeam, ArmorStand> armorStandMap;
    private final JavaPlugin plugin;  // プラグインを取得するための変数を追加


    public SpawnPointManager(JavaPlugin plugin) {
        this.plugin = plugin;  // プラグインを取得するための変数を初期化
        this.spawnPoints = new HashMap<>();
        this.armorStandMap = new HashMap<>();
    }

    public void setSpawnPoint(GameTeam team, Location location) {
        spawnPoints.put(team, location);
    }

    public Location getSpawnPoint(GameTeam team) {
        return spawnPoints.get(team);
    }

    public void spawnPlayer(Player player, GameTeam team) {
        Location spawnLocation = spawnPoints.get(team);
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
        }
    }

    public void spawnArmorStand(GameTeam team) {
        Location location = spawnPoints.get(team);
        if (location != null) {
            World world = location.getWorld();
            ArmorStand armorStand = (ArmorStand) world.spawnEntity(location.clone().add(0, 1, 0), EntityType.ARMOR_STAND);
            armorStand.setCustomName(team.getTeamName() + " Armor Stand");
            armorStand.setCustomNameVisible(true);

            // アーマースタンドを透明化
            armorStand.setMetadata("invisible", new FixedMetadataValue(plugin, true));

            armorStandMap.put(team, armorStand);
        }
    }

    // アーマースタンドの表示を一時的に撤去
    public void removeArmorStands() {
        for (ArmorStand armorStand : armorStandMap.values()) {
            armorStand.remove();
        }
    }

    // アーマースタンドの表示をリセット
    public void resetArmorStands() {
        for (ArmorStand armorStand : armorStandMap.values()) {
            armorStand.setVisible(true);
            armorStand.setMetadata("invisible", new FixedMetadataValue(plugin, false));
        }
    }

    // 新しいコード: 初期化された ArmorStandMap を返すメソッド
    public Map<GameTeam, ArmorStand> getArmorStandMap() {
        return armorStandMap;
    }

    public void clearSpawnPoints() {
        for (Location location : spawnPoints.values()) {
            // LocationオブジェクトからWorldに存在するEntityを取得して削除
            for (Entity entity : location.getWorld().getEntities()) {
                if (entity.getLocation().distanceSquared(location) < 0.1) {
                    entity.remove();
                }
            }
        }
        spawnPoints.clear();
    }
}
