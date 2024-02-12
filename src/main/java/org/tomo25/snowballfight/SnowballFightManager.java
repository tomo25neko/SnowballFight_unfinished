package org.tomo25.snowballfight;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowballFightManager {

    private int time;
    private final TeamScoreManager teamScoreManager;
    private final SnowballFight plugin;
    private final int snowballGiveInterval = 15; // 雪玉を配布する間隔（秒）
    private final int maxSnowballs = 16; // プレイヤーが持てる雪玉の最大数


    public SnowballFightManager(SnowballFight snowballFight) {
        this.plugin = snowballFight;
        time = 0; // ゲーム開始前
        teamScoreManager = new TeamScoreManager();
    }

    public int getTime() {
        return time;
    }

    public void setTime(int newTime) {
        this.time = newTime;
    }

    public void decreaseTime() {
        time--;
    }

    public void startGame() {
        Location redTeamLocation = getTeamArmorStandLocation(GameTeam.RED);
        Location blueTeamLocation = getTeamArmorStandLocation(GameTeam.BLUE);

        if (redTeamLocation == null || blueTeamLocation == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "エラー: スタート地点「RedStartもしくはBlueStartと名づけられたアーマースタンド」が設定されていません。");
            return;
        }

        startPreCountdown();
        startSnowballDistribution(); // スタート時に雪玉を配布

    }

    private void startSnowballDistribution() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !isPlayerSpectator(player))
                        .forEach(player -> {
                            int currentSnowballs = getPlayerSnowballCount(player);
                            if (currentSnowballs < maxSnowballs) {
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

    private boolean isPlayerSpectator(Player player) {
        GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
        return playerTeam == null; // プレイヤーのチームが null の場合は観戦者と判断
    }
    private void startPreCountdown() {
        new BukkitRunnable() {
            int preCountdown = 10;

            @Override
            public void run() {
                if (preCountdown > 0) {
                    if (preCountdown <= 3) {
                        playSound(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);
                    }
                    preCountdown--;
                } else {
                    this.cancel();
                    startGameTimer();
                }
            }
        }.runTaskTimer((Plugin) this, 0L, 20L);
    }

    private void startGameTimer() {
        playSound(Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (time > 0) {
                    decreaseTime();
                    // 時間が経過するごとにプレイヤーの情報を更新
                    Bukkit.getOnlinePlayers().forEach(SnowballFightManager.this::updatePlayerTeamDisplay);
                } else {
                    endGame();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void endGame() {
        announceWinningTeam();
        resetGame();
    }

    // ゲームが終了した際にプレイヤーの装備を取り外すメソッド
    public void removePlayerArmor(Player player) {
        EntityEquipment equipment = player.getEquipment();
        if (equipment != null) {
            equipment.setChestplate(null);
        }
    }

    private void announceWinningTeam() {
        // Implement winning team announcement logic
    }

    private void resetGame() {
        teamScoreManager.resetScores();
    }

    private void playSound(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && isGameRunning()) {
            event.setCancelled(true);
        }
    }

    private boolean isGameRunning() {
        return time > 0;
    }

    public void increaseOpponentTeamScore(GameTeam throwerTeam) {
        teamScoreManager.increaseOpponentTeamScore(throwerTeam);
    }

    public void playerDied(Player player) {
        GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
        if (playerTeam != null) {
            addPlayerToSpectator(player);

            new BukkitRunnable() {
                @Override
                public void run() {
                    respawnPlayer(player, playerTeam);
                }
            }.runTaskLater((Plugin) this, 100L);
        }
    }

    public void setStartPoint(Player player, GameTeam team) {
        Location standLocation = player.getLocation(); // プレイヤーの位置を取得
        saveArmorStandLocation(team, standLocation); // アーマースタンドの位置を保存
    }

    private void saveArmorStandLocation(GameTeam team, Location location) {
        // アーマースタンドの位置を保存するロジックを実装
        // 例: ファイル、データベース、またはメモリ内のデータ構造に位置情報を保存する
        // 保存する方法は使用するプラグインやニーズにより異なります
    }

    private Location getTeamArmorStandLocation(GameTeam team) {
        String armorStandName = (team == GameTeam.RED) ? "RedStart" : "BlueStart";

        // アーマースタンドの名前から位置を取得するロジックを実装
        // 例: return new Location(Bukkit.getWorld("world"), x, y, z);

        World world = Bukkit.getWorld("your_world_name");  // ワールド名を実際の名前に変更する
        double x = 0;  // アーマースタンドの X 座標
        double y = 0;  // アーマースタンドの Y 座標
        double z = 0;  // アーマースタンドの Z 座標

        // アーマースタンドの名前に応じて座標を設定
        if (armorStandName.equalsIgnoreCase("RedStart")) {
            x = 100;  // 赤チームのアーマースタンドの X 座標
            y = 65;   // 赤チームのアーマースタンドの Y 座標
            z = 200;  // 赤チームのアーマースタンドの Z 座標
        } else if (armorStandName.equalsIgnoreCase("BlueStart")) {
            x = -100;  // 青チームのアーマースタンドの X 座標
            y = 65;    // 青チームのアーマースタンドの Y 座標
            z = -200;  // 青チームのアーマースタンドの Z 座標
        }

        return new Location(world, x, y, z);
    }

    public void teleportPlayersToStartLocations() {
        Location redTeamLocation = getTeamArmorStandLocation(GameTeam.RED);
        Location blueTeamLocation = getTeamArmorStandLocation(GameTeam.BLUE);

        if (redTeamLocation != null && blueTeamLocation != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
                Location targetLocation = (playerTeam == GameTeam.RED) ? redTeamLocation : blueTeamLocation;
                // 赤チームのプレイヤーには赤い革チェストプレートを、青チームのプレイヤーには青い革チェストプレートを装備
                equipTeamArmor(player, playerTeam);
                player.teleport(targetLocation);
            });
        }
    }

    // プレイヤーにチームごとの革チェストプレートを装備するメソッド
    private void equipTeamArmor(Player player, GameTeam team) {
        EntityEquipment equipment = player.getEquipment();
        if (equipment != null) {
            ItemStack chestplate;
            LeatherArmorMeta meta;

            if (team == GameTeam.RED) {
                chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                meta = (LeatherArmorMeta) chestplate.getItemMeta();
                meta.setColor(Color.RED);
            } else {
                chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                meta = (LeatherArmorMeta) chestplate.getItemMeta();
                meta.setColor(Color.BLUE);
            }

            chestplate.setItemMeta(meta);
            equipment.setChestplate(chestplate);
        }
    }

    public void addPlayerToRedTeam(Player player, GameTeam team) {
        teamScoreManager.addPlayerToRedTeam(player);
    }

    public void addPlayerToBlueTeam(Player player, GameTeam team) {
        teamScoreManager.addPlayerToBlueTeam(player);
    }

    private void respawnPlayer(Player player, GameTeam team) {
        // Implement player respawn logic based on the team's spawn location
        Location respawnLocation = getTeamArmorStandLocation(team);
        if (respawnLocation != null) {
            player.teleport(respawnLocation);
        }
    }

    public void addPlayerToSpectator(Player player) {
        // Implement adding a player to the spectator team
    }

    public void increaseRedTeamKills() {
        teamScoreManager.increaseRedTeamKills();
    }

    public void increaseBlueTeamKills() {
        teamScoreManager.increaseBlueTeamKills();
    }

    public String getPlayerTeamDisplay(Player player) {
        GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);

        if (playerTeam == null) {
            return ChatColor.RED + "チームに入っていません";
        }

        switch (playerTeam) {
            case RED:
                return ChatColor.RED + "赤チーム";
            case BLUE:
                return ChatColor.BLUE + "青チーム";
            default:
                return ChatColor.GRAY + "観戦者";
        }
    }

    private void updatePlayerTeamDisplay(Player player) {
        GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
        int redTeamKills = teamScoreManager.getTeamScore(GameTeam.RED);
        int blueTeamKills = teamScoreManager.getTeamScore(GameTeam.BLUE);

        player.sendTitle("", getPlayerTeamDisplay(player), 10, 70, 20);
        player.sendMessage(ChatColor.GREEN + "あなたの所属: " + getPlayerTeamDisplay(player));
        player.sendMessage(ChatColor.GREEN + "サーバーのプレイヤー数: " + Bukkit.getOnlinePlayers().size());
        player.sendMessage(ChatColor.GREEN + "赤チームの人数: " + teamScoreManager.getRedTeamSize());
        player.sendMessage(ChatColor.GREEN + "青チームの人数: " + teamScoreManager.getBlueTeamSize());
        player.sendMessage(ChatColor.RED + "赤チームのキル数: " + redTeamKills);
        player.sendMessage(ChatColor.BLUE + "青チームのキル数: " + blueTeamKills);
    }


    public TeamScoreManager getTeamScoreManager() {
        return teamScoreManager;
    }
}