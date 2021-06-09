package org.metacity.scoreboard;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public interface ScoreboardTemplate {

    @Nullable String getLineOne(Player p);

    @Nullable String getLineTwo(Player p);

    @Nullable String getLineThree(Player p);

    @Nullable String getLineFour(Player p);

    @Nullable String getLineFive(Player p);

    @Nullable String getLineSix(Player p);

    @Nullable String getLineSeven(Player p);

    @Nullable String getLineEight(Player p);

    @Nullable String getLineNine(Player p);

    @Nullable String getLineTen(Player p);

    @Nullable String getLineEleven(Player p);

    @Nullable String getLineTwelve(Player p);

    @Nullable String getLineThirteen(Player p);

    @Nullable String getLineFourteen(Player p);

    @Nullable String getLineFifteen(Player p);

    @Nullable String getLineSixteen(Player p);

    default Collection<? extends Line> getLines(Player p) {
        String one = getLineOne(p);
        String two = getLineTwo(p);
        String three = getLineThree(p);
        String four = getLineFour(p);
        String five = getLineFive(p);
        String six = getLineSix(p);
        String seven = getLineSeven(p);
        String eight = getLineEight(p);
        String nine = getLineNine(p);
        String ten = getLineTen(p);
        String eleven = getLineEleven(p);
        String twelve = getLineTwelve(p);
        String thirteen = getLineThirteen(p);
        String fourteen = getLineFourteen(p);
        String fifteen = getLineFifteen(p);
        String sixteen = getLineSixteen(p);
        return Collections.unmodifiableCollection(Arrays.asList(
                one != null && one.equals("") ? new BlankLine(Slot.ONE) : new Line(this::getLineOne, Slot.ONE),
                two != null && two.equals("") ? new BlankLine(Slot.TWO) : new Line(this::getLineTwo, Slot.TWO),
                three != null && three.equals("") ? new BlankLine(Slot.THREE) : new Line(this::getLineThree, Slot.THREE),
                four != null && four.equals("") ? new BlankLine(Slot.FOUR) : new Line(this::getLineFour, Slot.FOUR),
                five != null && five.equals("") ? new BlankLine(Slot.FIVE) : new Line(this::getLineFive, Slot.FIVE),
                six != null && six.equals("") ? new BlankLine(Slot.SIX) : new Line(this::getLineSix, Slot.SIX),
                seven != null && seven.equals("") ? new BlankLine(Slot.SEVEN) : new Line(this::getLineSeven, Slot.SEVEN),
                eight != null && eight.equals("") ? new BlankLine(Slot.EIGHT) : new Line(this::getLineEight, Slot.EIGHT),
                nine != null && nine.equals("") ? new BlankLine(Slot.NINE) : new Line(this::getLineNine, Slot.NINE),
                ten != null && ten.equals("") ? new BlankLine(Slot.TEN) : new Line(this::getLineTen, Slot.TEN),
                eleven != null && eleven.equals("") ? new BlankLine(Slot.ELEVEN) : new Line(this::getLineEleven, Slot.ELEVEN),
                twelve != null && twelve.equals("") ? new BlankLine(Slot.TWELVE) : new Line(this::getLineTwelve, Slot.TWELVE),
                thirteen != null && thirteen.equals("") ? new BlankLine(Slot.THIRTEEN) : new Line(this::getLineThirteen, Slot.THIRTEEN),
                fourteen != null && fourteen.equals("") ? new BlankLine(Slot.FOURTEEN) : new Line(this::getLineFourteen, Slot.FOURTEEN),
                fifteen != null && fifteen.equals("") ? new BlankLine(Slot.FIFTEEN) : new Line(this::getLineFifteen, Slot.FIFTEEN),
                sixteen != null && sixteen.equals("") ? new BlankLine(Slot.SIXTEEN) : new Line(this::getLineSixteen, Slot.SIXTEEN)
        ));
    }

}
