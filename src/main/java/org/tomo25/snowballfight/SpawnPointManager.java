package org.tomo25.snowballfight;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class SpawnPointManager {

    private final Map<GameTeam, Location> spawnPoints;
    private final Map<GameTeam, ArmorStand> armorStandMap;

    public SpawnPointManager(JavaPlugin plugin) {
        this.spawnPoints = new HashMap<>();
        this.armorStandMap = new HashMap<>();
    }

    public void setSpawnPoint(GameTeam team, Location location) {
        spawnPoints.put(team, location);
    }

    public Location getSpawnPoint(GameTeam team) {
        return spawnPoints.get(team);
    }

    // ゲーム中にアーマースタンドを非表示にする
    public void hideArmorStands() {
        for (ArmorStand armorStand : armorStandMap.values()) {
            armorStand.setVisible(false);
        }
    }

    // ゲームが終了したらアーマースタンドを再表示する
    public void showArmorStands() {
        for (ArmorStand armorStand : armorStandMap.values()) {
            armorStand.setVisible(true);
        }
    }

    // チームに対応するアーマースタンドを取得するメソッド
    public ArmorStand getArmorStand(GameTeam team) {
        Location location = getSpawnPoint(team);
        if (location != null) {
            // アーマースタンドはスポーン地点の上に設置されていると仮定しています
            for (ArmorStand armorStand : location.getWorld().getEntitiesByClass(ArmorStand.class)) {
                if (armorStand.getLocation().getBlockY() == location.getBlockY()) {
                    return armorStand;
                }
            }
        }
        return null;
    }

    public void removeArmorStand(GameTeam team) {
        ArmorStand armorStand = getArmorStand(team);
        if (armorStand != null) {
            armorStand.remove();
        }
    }


    public void equipColoredLeatherArmor(ArmorStand armorStand, Color color) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();

        helmetMeta.setColor(color);
        chestplateMeta.setColor(color);
        leggingsMeta.setColor(color);
        bootsMeta.setColor(color);

        helmet.setItemMeta(helmetMeta);
        chestplate.setItemMeta(chestplateMeta);
        leggings.setItemMeta(leggingsMeta);
        boots.setItemMeta(bootsMeta);

        armorStand.setHelmet(helmet);
        armorStand.setChestplate(chestplate);
        armorStand.setLeggings(leggings);
        armorStand.setBoots(boots);
    }
}
