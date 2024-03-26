package org.tomo25.snowballfight;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowballFightManager implements Listener {

    private boolean gameStarted;
    private int time;
    private final TeamScoreManager teamScoreManager;
    private final SpawnPointManager spawnPointManager;
    private final SnowballDistributionManager snowballDistributionManager;
    private final SnowballDamageHandler snowballDamageHandler;
    private final GameScoreboardManager scoreboardManager;
    private final SnowballFight plugin;

    // プレイヤーにタイトルを送信するメソッド
    private void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }

    // プレイヤーにサウンドを再生するメソッド
    public void playSound(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public SnowballFightManager(SnowballFight snowballFight) {
        this.plugin = snowballFight;
        this.time = 0;
        this.teamScoreManager = new TeamScoreManager();
        this.spawnPointManager = new SpawnPointManager(snowballFight);
        this.snowballDistributionManager = new SnowballDistributionManager(snowballFight, this);
        this.snowballDamageHandler = new SnowballDamageHandler(this);
        this.scoreboardManager = new GameScoreboardManager(snowballFight, this.teamScoreManager);
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

    // スタートコマンド入力時のカウントダウン
    private void startPreCountdown() {
        new BukkitRunnable() {
            int preCountdown = 10;

            @Override
            public void run() {
                if (preCountdown > 0) {
                    // ゲームスタートまでの残り時間を各プレイヤーに送信
                    String message = ChatColor.GREEN + "ゲームスタートまであと " + preCountdown + " 秒";
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
                    if (preCountdown <= 3) {
                        // 3秒前からはタイトルにカウントを表示し、音を鳴らす
                        Bukkit.getOnlinePlayers().forEach(player -> sendTitle(player, ChatColor.RED + "_" + preCountdown + "_", "", 0, 20, 10));
                        playSound(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);
                    }
                    preCountdown--;
                } else {
                    // ゲームが開始されたときに "スタート" というタイトルを表示
                    Bukkit.getOnlinePlayers().forEach(player -> sendTitle(player, ChatColor.GREEN + "ゲームスタート", "", 0, 30, 20));
                    // メインの処理
                    this.cancel();
                    startGameTimer(); // メインのゲームタイマーをスタート
                    spawnPlayersToStartLocations(); // チームに入っているプレイヤーをそれぞれのスタート地点に移動
                    setPlayerGameModeToSpectator(); // チームに入っていないプレイヤーをスペクテイターにする
                    teleportSpectatorsToRandomSpawn(); // 観戦者のランダムテレポート
                    snowballDistributionManager.startSnowballDistribution(); // 雪玉配布メゾット
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
                    Bukkit.getOnlinePlayers().forEach(player -> scoreboardManager.updatePlayerTeamDisplay(player, time));
                } else {
                    endGame(); // ゲーム終了の処理
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 1秒ごとに実行
    }
    // ゲームの終了処理を行うメソッド
    private void endGame() {
        announceWinningTeam(); // 勝ったチームを宣言
        removePlayerEquipment(); // プレイヤーの装備を削除
        resetPlayerGameMode(); // 観戦者のプレイヤーのゲームモードをリセット
        resetGame();
        spawnPointManager.showArmorStands(); // ゲーム終了後にアーマースタンドを再表示
        gameStarted = false; // ゲームが終了したことを示す
    }

    // ゲームが開始されているかどうかを返すメソッド
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * ここからはゲーム終了時の処理
     * 1. 両チームのスコアを比べて勝敗を決める処理
     * 2. ゲームのリセット処理
     * 3. 雪玉を削除する処理
     * 4. プレイヤーの装備を削除する処理
     */

    // 両チームのスコアを比べて勝敗を決めるメソッド
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

    // ゲームをリセットするメソッド
    private void resetGame() {
        teamScoreManager.resetScores();
        spawnPointManager.hideArmorStands(); // ゲームがリセットされる時にアーマースタンドを非表示にする
    }

    // プレイヤーの装備を削除するメソッド
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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        snowballDamageHandler.onSnowballHit(event);
    }

    //ゲーム中のプレイヤーをそれぞれのチームのスポーン地点にテレポートさせるメソッド
    private void spawnPlayersToStartLocations() {
        // 赤チームと青チームのスポーン地点を取得
        Location redTeamLocation = spawnPointManager.getSpawnPoint(GameTeam.RED);
        Location blueTeamLocation = spawnPointManager.getSpawnPoint(GameTeam.BLUE);

        if (redTeamLocation != null && blueTeamLocation != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
                Location targetLocation = (playerTeam == GameTeam.RED) ? redTeamLocation : blueTeamLocation;

                // プレイヤーにチームごとの革チェストプレートを装備
                equipTeamArmor(player, playerTeam);

                // 赤チームまたは青チームのスポーン地点にプレイヤーをテレポート
                player.teleport(targetLocation);

                if (playerTeam != null) {
                    // プレイヤーが観戦者でない場合に装備とテレポートを行う
                    if (!isPlayerSpectator(player)) {
                        player.teleport(targetLocation);
                        equipTeamArmor(player, playerTeam);
                    }
                }
            });
        }
    }

    // プレイヤーが死亡した際の処理
    @EventHandler
    public void playerDied(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GameTeam playerTeam = teamScoreManager.getPlayerTeam(player);
        if (playerTeam != null) {
            // プレイヤーを観戦者に移動し、一定時間後にリスポーンさせる
            addPlayerToSpectator(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    respawnPlayer(player, playerTeam);
                }
            }.runTaskLater(plugin, 200L); // 10秒後にリスポーン
        }
    }

    // プレイヤーをリスポーンさせるメソッド
    private void respawnPlayer(Player player, GameTeam team) {
        // チームのスポーン地点を取得
        Location respawnLocation = spawnPointManager.getSpawnPoint(team);

        if (respawnLocation != null) {
            // プレイヤーが観戦者でない場合にスポーン地点にテレポートし、チームのアーマーを装備
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
                if (meta != null) {
                    meta.setColor(Color.RED);
                    chestplate.setItemMeta(meta);
                }
            } else {
                chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                meta = (LeatherArmorMeta) chestplate.getItemMeta();
                if (meta != null) {
                    meta.setColor(Color.BLUE);
                    chestplate.setItemMeta(meta);
                }
            }

            equipment.setChestplate(chestplate);
        }
    }


    // SpawnPointManager のインスタンスを提供するメソッド
    public SpawnPointManager getSpawnPointManager() {
        return this.spawnPointManager;
    }

    /**
     * この後観戦者の処理が入る
     * 1. 観戦者の判別処理
     * 2. ゲームスタート時に観戦者のゲームモードをスペクテイターにする処理
     * 3. ゲームスタート時に観戦者を RED もしくは BLUE のスポーン地点にランダム移動
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

    // ゲーム中の新規参加者を観戦者にする
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // ゲームが開始されている場合にプレイヤーが参加したら観戦者にする
        if (isGameStarted()) {
            addPlayerToSpectator(player);
        }
    }

    public boolean isPlayerInTeam(Player player, GameTeam team) {
        return teamScoreManager.getPlayerTeam(player) == team;
    }

    public void addPlayerToTeam(Player player, GameTeam team) {
        teamScoreManager.addPlayerToTeam(player, team);
    }

    public void addPlayerToSpectator(Player player) {
        // プレイヤーを観戦者に設定する
        player.setGameMode(GameMode.SPECTATOR);
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
}
