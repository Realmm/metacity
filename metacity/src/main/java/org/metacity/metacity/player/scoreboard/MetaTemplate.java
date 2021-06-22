package org.metacity.metacity.player.scoreboard;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.scoreboard.MetaScoreboard;
import org.metacity.util.CC;

import java.util.Optional;

public class MetaTemplate extends MetaScoreboard {

    public MetaTemplate() {
        super(CC.BLUE_200.bold() + "MetaCity");
    }

    private Optional<MetaPlayer> meta(Player p) {
        return MetaCity.getInstance().getPlayerManager().getPlayer(p);
    }

    private boolean linked(Player p) {
        return meta(p).map(MetaPlayer::isLinked).orElse(false);
    }

    @Nullable
    @Override
    public String getLineOne(Player p) {
        return "";
    }

    @Nullable
    @Override
    public String getLineTwo(Player p) {
        if (!linked(p)) return CC.RED + "Not linked";
        MetaPlayer meta = meta(p).orElse(null);
        if (meta == null) return null;
        return CC.GOLD + "Level: " + CC.GRAY + meta.getLevel();
    }

    @Nullable
    @Override
    public String getLineThree(Player p) {
        if (!linked(p)) return CC.RED + "Link with /meta <qr | link>";
        MetaPlayer meta = meta(p).orElse(null);
        if (meta == null) return null;
        return CC.GOLD + "MetaCoin: " + CC.GRAY + 0.0;
    }

    @Nullable
    @Override
    public String getLineFour(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineFive(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineSix(Player p) {
        return null;
    }

    @Nullable
    @Override
    public String getLineSeven(Player p) {
        return null;
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
        return "";
    }

    @Nullable
    @Override
    public String getLineSixteen(Player p) {
        return CC.YELLOW + "metacity.land";
    }
}
