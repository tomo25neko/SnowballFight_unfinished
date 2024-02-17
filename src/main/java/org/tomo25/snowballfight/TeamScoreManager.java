package org.tomo25.snowballfight;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TeamScoreManager {

    private final Map<GameTeam, Integer> teamScores;
    private final Map<Player, GameTeam> playerTeams;

    public TeamScoreManager() {
        teamScores = new HashMap<>();
        playerTeams = new HashMap<>();
        resetScores();
    }

    public int getTeamScore(GameTeam team) {
        return teamScores.getOrDefault(team, 0);
    }

    public void increaseTeamScore(GameTeam team) {
        teamScores.put(team, teamScores.getOrDefault(team, 0) + 1);
    }

    public void resetScores() {
        teamScores.clear();
    }

    public void increaseOpponentTeamScore(GameTeam throwerTeam) {
        if (throwerTeam == GameTeam.RED) {
            increaseTeamScore(GameTeam.BLUE);
        } else {
            increaseTeamScore(GameTeam.RED);
        }
    }

    public GameTeam getPlayerTeam(Player player) {
        return playerTeams.getOrDefault(player, null);
    }

    public void addPlayerToTeam(Player target, GameTeam team) {
        playerTeams.put(target, team);
    }

    public void addPlayerToRedTeam(Player target) {
        addPlayerToTeam(target, GameTeam.RED);
    }

    public void addPlayerToBlueTeam(Player target) {
        addPlayerToTeam(target, GameTeam.BLUE);
    }



    public void increaseRedTeamKills() {
        increaseTeamKills(GameTeam.RED);
    }

    public void increaseBlueTeamKills() {
        increaseTeamKills(GameTeam.BLUE);
    }

    private void increaseTeamKills(GameTeam team) {
        increaseTeamScore(team);
    }

    public boolean isPlayerInTeam(Player player, GameTeam team) {
        return playerTeams.getOrDefault(player, null) == team;
    }

    public int getRedTeamSize() {
        return getTeamSize(GameTeam.RED);
    }

    public int getBlueTeamSize() {
        return getTeamSize(GameTeam.BLUE);
    }

    private int getTeamSize(GameTeam team) {
        return (int) playerTeams.values().stream().filter(t -> t == team).count();
    }

}
