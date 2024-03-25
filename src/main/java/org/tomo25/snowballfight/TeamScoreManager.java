package org.tomo25.snowballfight;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * チームのスコアを管理するクラスです。
 */
public class TeamScoreManager {

    // チームごとのスコアを保持するマップ
    private final Map<GameTeam, Integer> teamScores;

    // プレイヤーごとの所属チームを保持するマップ
    private final Map<Player, GameTeam> playerTeams;

    /**
     * 新しい TeamScoreManager インスタンスを作成します。
     * 初期化時にスコアをリセットします。
     */
    public TeamScoreManager() {
        teamScores = new HashMap<>();
        playerTeams = new HashMap<>();
        resetScores();
    }

    /**
     * 指定されたチームのスコアを取得します。
     * @param team スコアを取得したいチーム
     * @return 指定されたチームのスコア
     */
    public int getTeamScore(GameTeam team) {
        return teamScores.getOrDefault(team, 0);
    }

    /**
     * 指定されたチームのスコアを増やします。
     * @param team スコアを増やしたいチーム
     */
    public void increaseTeamScore(GameTeam team) {
        teamScores.put(team, teamScores.getOrDefault(team, 0) + 1);
    }

    /**
     * 全てのチームのスコアをリセットします。
     */
    public void resetScores() {
        teamScores.clear();
    }

    /**
     * 指定されたプレイヤーの所属チームを取得します。
     * @param player 所属チームを取得したいプレイヤー
     * @return 指定されたプレイヤーの所属チーム、存在しない場合は null
     */
    public GameTeam getPlayerTeam(Player player) {
        return playerTeams.getOrDefault(player, null);
    }

    /**
     * 指定されたプレイヤーを指定されたチームに追加します。
     * @param target チームに追加したいプレイヤー
     * @param team 追加するチーム
     */
    public void addPlayerToTeam(Player target, GameTeam team) {
        playerTeams.put(target, team);
    }

    /**
     * 赤チームのプレイヤー数を取得します。
     * @return 赤チームのプレイヤー数
     */
    public int getRedTeamSize() {
        return getTeamSize(GameTeam.RED);
    }

    /**
     * 青チームのプレイヤー数を取得します。
     * @return 青チームのプレイヤー数
     */
    public int getBlueTeamSize() {
        return getTeamSize(GameTeam.BLUE);
    }

    /**
     * 指定されたチームのプレイヤー数を取得します。
     * @param team プレイヤー数を取得したいチーム
     * @return 指定されたチームのプレイヤー数
     */
    private int getTeamSize(GameTeam team) {
        return (int) playerTeams.values().stream().filter(t -> t == team).count();
    }
}
