package org.tomo25.snowballfight;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowballFightManager {

    private int time;

    private final TeamScoreManager teamScoreManager;

    private final SnowballFight plugin;

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

    private Location getTeamArmorStandLocation(GameTeam team) {
        String armorStandName = (team == GameTeam.RED) ? "RedStart" : "BlueStart";
        // アーマースタンドの名前から位置を取得するロジックを実装
        // 例: return new Location(Bukkit.getWorld("world"), x, y, z);
        return null;
    }

    public void teleportPlayersToStartLocations() {
        Location redTeamLocation = getTeamArmorStandLocation(GameTeam.RED);
        Location blueTeamLocation = getTeamArmorStandLocation(GameTeam.BLUE);

        if (redTeamLocation != null && blueTeamLocation != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
                Location targetLocation = (playerTeam == GameTeam.RED) ? redTeamLocation : blueTeamLocation;
                player.teleport(targetLocation);
            });
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