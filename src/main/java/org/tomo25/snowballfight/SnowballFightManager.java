package org.tomo25.snowballfight;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class SnowballFightManager {

    private int time;
    private final TeamScoreManager teamScoreManager;
    private final SpawnPointManager spawnPointManager;
    private final SnowballDistributionManager snowballDistributionManager;
    private final SnowballFight plugin;

    public SnowballFightManager(SnowballFight snowballFight) {
        this.plugin = snowballFight;
        this.time = 0;
        this.teamScoreManager = new TeamScoreManager();
        this.spawnPointManager = new SpawnPointManager(snowballFight);
        this.snowballDistributionManager = new SnowballDistributionManager(snowballFight);
    }

    public void setSpawnLocation(GameTeam team, Location location) {
        spawnPointManager.setSpawnPoint(team, location);
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
        // スタート地点が設定されているか確認
        if (spawnPointManager.getSpawnPoint(GameTeam.RED) == null || spawnPointManager.getSpawnPoint(GameTeam.BLUE) == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "エラー: 赤チームと青チームのスタート地点が設定されていません！");
            return;
        }

        startPreCountdown();
        snowballDistributionManager.startSnowballDistribution(this); // スタート時に雪玉を配布

    }

    // SpawnPointManager のインスタンスを提供するメソッド
    public SpawnPointManager getSpawnPointManager() {
        return this.spawnPointManager;
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
                    spawnPlayersToStartLocations(); // ゲームのスタート時にプレイヤーをスタート地点にテレポート
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
        }.runTaskTimer(plugin, 0L, 20L); // 1秒ごとに実行
    }


    private void endGame() {
        announceWinningTeam();
        removePlayerEquipment();
        resetGame();
        spawnPointManager.showArmorStands(); // ゲーム終了後にアーマースタンドを再表示
    }

    private void announceWinningTeam() {
        // Implement winning team announcement logic
    }

    private void resetGame() {
        teamScoreManager.resetScores();
        spawnPointManager.hideArmorStands(); // ゲームがリセットされる時にアーマースタンドを非表示にする
    }

    private void playSound(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    private void removePlayerEquipment() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            // プレイヤーの雪玉をクリア
            player.getInventory().remove(Material.SNOWBALL);

            // プレイヤーの装備を取り外す
            removePlayerArmor(player);
        });
    }

    // ゲームが終了した際にプレイヤーの装備を取り外すメソッド
    public void removePlayerArmor(Player player) {
        EntityEquipment equipment = player.getEquipment();
        if (equipment != null) {
            equipment.setChestplate(null);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && isGameRunning()) {
            event.setCancelled(true);
        }
    }
    public boolean isGameRunning() {
        return time > 0;
    }

    private void spawnPlayersToStartLocations() {
        Location redTeamLocation = spawnPointManager.getSpawnPoint(GameTeam.RED);
        Location blueTeamLocation = spawnPointManager.getSpawnPoint(GameTeam.BLUE);

        if (redTeamLocation != null && blueTeamLocation != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
                Location targetLocation = (playerTeam == GameTeam.RED) ? redTeamLocation : blueTeamLocation;
                // 赤チームのプレイヤーには赤い革チェストプレートを、青チームのプレイヤーには青い革チェストプレートを装備
                equipTeamArmor(player, playerTeam);
                player.teleport(targetLocation);

                if (playerTeam != null) {
                    // プレイヤーが観戦者でない場合にテレポート
                    if (!isPlayerSpectator(player)) {
                        player.teleport(targetLocation);
                        equipTeamArmor(player, playerTeam);
                    }
                }
            });
        }
    }

    @EventHandler
    public void playerDied(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
        if (playerTeam != null) {
            addPlayerToSpectator(player); // プレイヤーを観戦者に移動

            new BukkitRunnable() {
                @Override
                public void run() {
                    respawnPlayer(player, playerTeam); // プレイヤーをリスポーンさせる
                }
            }.runTaskLater(plugin, 200L); // 10秒後にリスポーン
        }
    }
    public boolean isPlayerSpectator(Player player) {
        GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
        return playerTeam == null; // プレイヤーのチームが null の場合は観戦者と判断
    }

    private void respawnPlayer(Player player, GameTeam team) {
        // Implement player respawn logic based on the team's spawn location
        Location respawnLocation = spawnPointManager.getSpawnPoint(team);
        if (respawnLocation != null) {
            // プレイヤーが観戦者の場合はスポーン地点にテレポートしない
            if (!isPlayerSpectator(player)) {
                player.teleport(respawnLocation);
                equipTeamArmor(player, team);
            }
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
        }
    }

    public void addPlayerToTeam(Player player, GameTeam team) {
        teamScoreManager.addPlayerToTeam(player, team);
    }

    public void addPlayerToRedTeam(Player player, GameTeam team) {
        teamScoreManager.addPlayerToRedTeam(player);
    }

    public void addPlayerToBlueTeam(Player player,GameTeam team) {
        teamScoreManager.addPlayerToBlueTeam(player);
    }

    public void addPlayerToSpectator(Player player) {
        // Implement adding a player to the spectator team
    }

    public TeamScoreManager getTeamScoreManager() {
        return teamScoreManager;
    }

    public String getPlayerTeamDisplay(Player player) {
        GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);

        if (playerTeam == null) {
            return ChatColor.RED + "チーム入ろう";
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
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("snowballFight");

        if (objective == null) {
            // Objective が存在しない場合は新しい Objective を登録
            objective = scoreboard.registerNewObjective("snowballFight", "dummy", ChatColor.BOLD + "Snowball Fight");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // スコアを設定
        int redTeamScore = teamScoreManager.getTeamScore(GameTeam.RED);
        int blueTeamScore = teamScoreManager.getTeamScore(GameTeam.BLUE);

        // プレイヤーごとのスコアを設定
        Score TimeScore = objective.getScore(ChatColor.AQUA + "残り時間:" + time);
        TimeScore.setScore(7);
        Score redTeamPlayerScore = objective.getScore(ChatColor.RED + "あなたの所属: " + getPlayerTeamDisplay(player));
        redTeamPlayerScore.setScore(6);  // 6 は表示される位置を示します
        Score serverPlayerCountScore = objective.getScore(ChatColor.GREEN + "サーバーのプレイヤー数: " + Bukkit.getOnlinePlayers().size());
        serverPlayerCountScore.setScore(5);
        Score redTeamSizeScore = objective.getScore(ChatColor.GREEN + "赤チームの人数: " + teamScoreManager.getRedTeamSize());
        redTeamSizeScore.setScore(4);
        Score blueTeamSizeScore = objective.getScore(ChatColor.GREEN + "青チームの人数: " + teamScoreManager.getBlueTeamSize());
        blueTeamSizeScore.setScore(3);
        Score redTeamKillScore = objective.getScore(ChatColor.RED + "赤チームのキル数: " + redTeamScore);
        redTeamKillScore.setScore(2);
        Score blueTeamKillScore = objective.getScore(ChatColor.BLUE + "青チームのキル数: " + blueTeamScore);
        blueTeamKillScore.setScore(1);

        // プレイヤーにスコアボードを表示
        player.setScoreboard(scoreboard);
    }
}
