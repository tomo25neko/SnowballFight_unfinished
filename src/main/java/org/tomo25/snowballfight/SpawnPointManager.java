package org.tomo25.snowballfight;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SpawnPointManager {

    private final Map<GameTeam, Location> spawnPoints;

    public SpawnPointManager() {
        this.spawnPoints = new HashMap<>();
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

    // spawnArmorStandメソッドの修正
    public void spawnArmorStand(GameTeam team) {
        Location location = spawnPoints.get(team);
        if (location != null) {
            World world = location.getWorld();
            ArmorStand armorStand = (ArmorStand) world.spawnEntity(location.clone().add(0, 1, 0), EntityType.ARMOR_STAND);
            armorStand.setCustomName(team.getTeamName() + " Armor Stand");
            armorStand.setCustomNameVisible(true);
            armorStand.setVisible(false);
        }
    }

    // clearSpawnPointsメソッドの修正
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
