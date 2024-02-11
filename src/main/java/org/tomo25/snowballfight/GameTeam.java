package org.tomo25.snowballfight;

public enum GameTeam {
    RED("RED"),
    BLUE("BLUE");

    private final String teamName;

    GameTeam(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamName() {
        return teamName;
    }
}
