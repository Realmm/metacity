package org.metacity.scoreboard;

import org.bukkit.scoreboard.Team;

class ScoreboardTeam {

    private final Team team;
    private final Slot slot;

    ScoreboardTeam(Team team, Slot slot) {
        this.team = team;
        this.slot = slot;
    }

    Slot slot() {
        return slot;
    }

    Team team() {
        return team;
    }

}
