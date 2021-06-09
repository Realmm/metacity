package org.metacity.metacity.player.scoreboard;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metacity.scoreboard.MetaScoreboard;
import org.metacity.util.CC;

public class MetaTemplate extends MetaScoreboard {

    public MetaTemplate() {
        super(CC.BLUE_200.bold() + "MetaCity");
    }

    @Nullable
    @Override
    public String getLineOne(Player p) {
        return "";
    }

    @Nullable
    @Override
    public String getLineTwo(Player p) {
        return "test";
    }

    @Nullable
    @Override
    public String getLineThree(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineFour(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineFive(Player p) {
        return "five";
    }

    @Nullable
    @Override
    public String getLineSix(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineSeven(Player p) {
        return "sven: " + p.getUniqueId().toString();
    }

    @Nullable
    @Override
    public String getLineEight(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineNine(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineTen(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineEleven(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineTwelve(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineThirteen(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineFourteen(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineFifteen(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineSixteen(Player p) {
        return null;
    }
}
