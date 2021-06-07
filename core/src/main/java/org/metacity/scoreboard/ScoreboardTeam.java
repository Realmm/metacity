package org.metacity.scoreboard;

import org.bukkit.scoreboard.Team;

class ScoreboardTeam {

    private final int index;
    private final Team team;
    private final String entry;

    ScoreboardTeam(int index, Team team, String entry) {
        this.index = index;
        this.team = team;
        this.entry = entry;
    }

    int getIndex() {
        return index;
    }

    Team getTeam() {
        return team;
    }

    String getEntry() {
        return entry;
    }

}
