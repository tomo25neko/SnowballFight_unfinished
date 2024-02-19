package org.tomo25.snowballfight;

import org.bukkit.Color;

public enum GameTeam {
    RED("RED", Color.RED),
    BLUE("BLUE", Color.BLUE);

    private final String teamName;
    private final Color color;

    GameTeam(String teamName, Color color) {
        this.teamName = teamName;
        this.color = color;
    }

    public String getTeamName() {
        return teamName;
    }

    public Color getColor() {
        return color;
    }

    public static GameTeam getTeamByName(String name) {
        for (GameTeam team : values()) {
            if (team.getTeamName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }
}
