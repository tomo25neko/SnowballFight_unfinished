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

    private boolean gameStarted; // ゲームが開始されているかどうかを示すフラグ
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
        this.snowballDistributionManager = new SnowballDistributionManager(snowballFight, this);
    }

    // スポーン地点の設定
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
        // ゲームが開始される前にプレイヤーを観戦者に追加
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (teamScoreManager.getPlayerTeam(player) == null) {
                addPlayerToSpectator(player);
            }
        }

        startPreCountdown();
        gameStarted = true;  // ゲームが開始されたことを示す
    }

    //スタートコマンド入力時のカウントダウン
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
                    startGameTimer();//メインのゲームタイマーをスタート
                    spawnPlayersToStartLocations();//チームに入っているプレイヤーをそれぞれのスタート地点に移動
                    setPlayerGameModeToSpectator();//チームに入っていないプレイヤーをスペクテイターにする
                    teleportSpectatorsToRandomSpawn();//観戦者のランダムテレポート
                    snowballDistributionManager.startSnowballDistribution();//雪玉配布メゾット
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // SnowballFightインスタンスを使用する
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
                    endGame();//ゲーム終了の処理
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 1秒ごとに実行
    }


    private void endGame() {
        announceWinningTeam();//勝ったチームを宣言
        removePlayerEquipment();//プレイヤーの装備を削除
        resetPlayerGameMode(); // 観戦者のプレイヤーのゲームモードをリセット
        resetGame();
        spawnPointManager.showArmorStands(); // ゲーム終了後にアーマースタンドを再表示
        gameStarted = false;  // ゲームが終了したことを示す
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    /**ここからはゲーム終了時の処理
     * 1両チームのスコアを比べて勝敗を決める処理
     * 2ゲームのリセット処理
     * 3雪玉を削除する処理
     * 4プレイヤーの装備を削除する処理
     */

    //両チームのスコアを比べて勝敗を決める
    private void announceWinningTeam() {
        // 赤チームと青チームのスコアを取得
        int redTeamScore = teamScoreManager.getTeamScore(GameTeam.RED);
        int blueTeamScore = teamScoreManager.getTeamScore(GameTeam.BLUE);

        if (redTeamScore > blueTeamScore) {
            Bukkit.broadcastMessage(ChatColor.RED + "赤チームが勝利しました！");
        } else if (blueTeamScore > redTeamScore) {
            Bukkit.broadcastMessage(ChatColor.BLUE + "青チームが勝利しました！");
        } else {
            Bukkit.broadcastMessage(ChatColor.GRAY + "引き分けです！");
        }
    }

    private void resetGame() {
        teamScoreManager.resetScores();
        spawnPointManager.hideArmorStands(); // ゲームがリセットされる時にアーマースタンドを非表示にする
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


    private void playSound(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    //プレイヤーがダメージを受けた際に無効化する
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && isGameStarted()) {
            event.setCancelled(true);
        }
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

    //プレイヤーを復活させる処理
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

    private void respawnPlayer(Player player, GameTeam team) {
        // チームのスポーン地点を取得
        Location respawnLocation = spawnPointManager.getSpawnPoint(team);

        if (respawnLocation != null) {
            // プレイヤーが観戦者の場合はスポーン地点にテレポートしない
            if (!isPlayerSpectator(player)) {
                // スポーン地点にプレイヤーをテレポート
                player.teleport(respawnLocation);
                // チームのアーマーを装備させる
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
                chestplate.setItemMeta(meta);
            } else {
                chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                meta = (LeatherArmorMeta) chestplate.getItemMeta();
                meta.setColor(Color.BLUE);
                chestplate.setItemMeta(meta);
            }

            equipment.setChestplate(chestplate);
        }
    }

    // SpawnPointManager のインスタンスを提供するメソッド
    public SpawnPointManager getSpawnPointManager() {
        return this.spawnPointManager;
    }

    /**
    この後観戦者の処理が入る
     1 観戦者の判別処理
     2ゲームスタート時に観戦者のゲームモードをスペクテイターにする処理
     3ゲームスタート時に観戦者をREDもしくはBLUEのスポーン地点にランダム移動
     **/
    public boolean isPlayerSpectator(Player player) {
        GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
        return playerTeam == null; // プレイヤーのチームが null の場合は観戦者と判断
    }

    public void setPlayerGameModeToSpectator() {
        Bukkit.getOnlinePlayers().stream()
                .filter(this::isPlayerSpectator)
                .forEach(player -> player.setGameMode(GameMode.SPECTATOR));
    }

    public void resetPlayerGameMode() {
        Bukkit.getOnlinePlayers().forEach(player -> player.setGameMode(GameMode.SURVIVAL));
    }

    public void teleportSpectatorsToRandomSpawn() {
        // 赤チームと青チームのスポーン地点を取得
        Location redSpawn = spawnPointManager.getSpawnPoint(GameTeam.RED);
        Location blueSpawn = spawnPointManager.getSpawnPoint(GameTeam.BLUE);

        // ランダムにスポーン地点を選択
        Location randomSpawn = (Math.random() < 0.5) ? redSpawn : blueSpawn;

        // 観戦者をランダムなスポーン地点に移動
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPlayerSpectator(player)) {
                player.teleport(randomSpawn);
            }
        }
    }

    public boolean isPlayerInTeam(Player player, GameTeam team) {
        return teamScoreManager.getPlayerTeam(player) == team;
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
            return ChatColor.RED + "無所属";
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

    //プレイヤーにスコアを表示するメゾット
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
        Score redTeamPlayerScore = objective.getScore(ChatColor.RED + "所属: " + getPlayerTeamDisplay(player));
        redTeamPlayerScore.setScore(6);  // 6 は表示される位置を示します
        Score serverPlayerCountScore = objective.getScore(ChatColor.GREEN + "総プレイヤー数: " + Bukkit.getOnlinePlayers().size());
        serverPlayerCountScore.setScore(5);
        Score redTeamSizeScore = objective.getScore(ChatColor.GREEN + "赤チーム人数: " + teamScoreManager.getRedTeamSize());
        redTeamSizeScore.setScore(4);
        Score blueTeamSizeScore = objective.getScore(ChatColor.GREEN + "青チーム人数: " + teamScoreManager.getBlueTeamSize());
        blueTeamSizeScore.setScore(3);
        Score redTeamKillScore = objective.getScore(ChatColor.RED + "赤キル数: " + redTeamScore);
        redTeamKillScore.setScore(2);
        Score blueTeamKillScore = objective.getScore(ChatColor.BLUE + "青キル数: " + blueTeamScore);
        blueTeamKillScore.setScore(1);

        // プレイヤーにスコアボードを表示
        player.setScoreboard(scoreboard);
    }
}
