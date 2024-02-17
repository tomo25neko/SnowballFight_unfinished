package org.tomo25.snowballfight;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
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

    public void spawnArmorStand(JavaPlugin plugin, GameTeam team) {
        Location location = spawnPoints.get(team);
        if (location != null) {
            World world = location.getWorld();
            ArmorStand armorStand = (ArmorStand) world.spawnEntity(location.clone().add(0, 1, 0), EntityType.ARMOR_STAND);
            armorStand.setCustomName(team.getTeamName());
            armorStand.setCustomNameVisible(true);

            // アーマースタンドを透明化
            armorStand.setMetadata("invisible", new FixedMetadataValue(plugin, true));

            armorStandMap.put(team, armorStand);

            // アーマースタンドに染色された革装備を装備させる
            equipColoredLeatherArmor(armorStand, team.getColor());
        }
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

    private void equipColoredLeatherArmor(ArmorStand armorStand, Color color) {
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
