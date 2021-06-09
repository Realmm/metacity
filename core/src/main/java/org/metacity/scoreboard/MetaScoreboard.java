package org.metacity.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.language.bm.Lang;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.metacity.util.Util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class MetaScoreboard implements ScoreboardTemplate {

    private Scoreboard scoreboard;
    private Objective objective;
    private final Set<ScoreboardTeam> teams = new HashSet<>();
    private @Nonnull
    LineExecution title;
    private final List<History> history = new ArrayList<>();

    public MetaScoreboard(@Nonnull String title) {
        this(p -> title);
    }

    public MetaScoreboard(@Nonnull LineExecution title) {
        if (title == null) throw new IllegalStateException("Title cannot be null");
        this.title = title;
        initScoreboard();
        initObjective();
    }

    private void initObjective() {
        objective = scoreboard.registerNewObjective("obj", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private void initScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        for (Slot slot : Slot.values()) {
            Team t = scoreboard.registerNewTeam(String.valueOf(slot.entry()));
            teams.add(new ScoreboardTeam(t, slot));
        }
    }

    public void setTitle(LineExecution title) {
        if (title == null) throw new IllegalStateException("Title cannot be null");
        this.title = title;
    }

    public void setTitle(String title) {
        setTitle(p -> title);
    }

    public void remove(Player p) {
        scoreboard = null;
        if (Bukkit.getScoreboardManager() != null && p != null)
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void update(Player p) {
        if (scoreboard == null || scoreboard.getObjectives().isEmpty()) {
            initScoreboard();
            initObjective();
        }

        System.out.println("name: " + objective.getName());
        String name = objective.getName().equals("obj") ? "obj2" : "obj";
        System.out.println("nameNow: " + name);
        String criteria = "dummy";

//        Objective remove = scoreboard.getObjective(name);
//        if (remove != null) remove.unregister();

        Objective o = scoreboard.registerNewObjective(name, criteria, Util.color(title.execute(p)));
        o.setDisplaySlot(objective.getDisplaySlot());

        Map<Integer, String> historyMap = new HashMap<>();
        List<ChatColor> usedForBlankLine = new ArrayList<>();

        List<Line> ordered = getOrderedLines(p);
        List<DataHolder> holders = new ArrayList<>();

        ordered.forEach(line -> {
            ScoreboardTeam sbTeam = getScoreboardTeam(line);
            Team team = sbTeam.team();
            String entry = sbTeam.slot().entry();
            team.addEntry(entry);
            ScoreboardTeam sTeam = getScoreboardTeam(line);

            String toAdd;
            if (line instanceof BlankLine) {
                List<ChatColor> colors = Arrays.asList(ChatColor.values());
                Collections.shuffle(colors);
                ChatColor color = colors.get(0);
                while (usedForBlankLine.contains(color)) {
                    Collections.shuffle(colors);
                    color = colors.get(0);
                }
                toAdd = color + " ";
                usedForBlankLine.add(color);
            } else toAdd = line.content(p);

            if (getHistory(p).isPresent()) {
                History h = getHistory(p).get();
                if (!h.hasChanged(line.slot().slot, toAdd)) {
                    holders.add(new DataHolder(sTeam, line, toAdd));
                    return;
                }
            }
            //need a way to store a history of all lines for individual players on last successful update
            //check against this history, compare the content of the line at that slot, if different, update that line

            //Splits the line in half, sets the first half as prefix, second half as suffix, of the appropriate team
            if (toAdd.length() <= 16) {
                team.setPrefix(Util.color(toAdd));
                team.setSuffix("");
            } else {
                if (toAdd.length() > 30) {
                    toAdd = StringUtils.substring(toAdd, 0, 30);
                }

                String sub = StringUtils.substring(toAdd, 0, 16);
                char splitCode = sub.charAt(sub.length() - 1);
                boolean codeSplit = splitCode == ChatColor.COLOR_CHAR;
                String prefix = StringUtils.substring(toAdd, 0, codeSplit ? 15 : 16);
                String lastColor = ChatColor.getLastColors(Util.color(prefix)).equals("") ? ChatColor.RESET.toString() : ChatColor.getLastColors(Util.color(prefix));
                String suffix = StringUtils.substring(toAdd, codeSplit ? 15 : 16, codeSplit ? 31 : 30);

                team.setPrefix(Util.color(prefix));
                team.setSuffix(Util.color(codeSplit ? suffix : lastColor + suffix));
            }

            //Updates the objective with the appropriate score and entry

            holders.add(new DataHolder(sTeam, line, toAdd));
        });

        adjustSlots(holders);

        boolean[] updated = {false};
        holders.forEach(h -> {
            historyMap.put(h.adjustedSlot, h.toAdd);
            o.getScore(h.sTeam.slot().entry()).setScore(h.adjustedSlot);
            System.out.println("Set scoreboard " + h.adjustedSlot + " " + h.toAdd);
            updated[0] = true;
        });

        if (updated[0]) {
            Objective obj = objective;
            objective = o;
            System.out.println("changing objectives");
            scoreboard.getObjectives().forEach(ob -> System.out.println("obj: " + ob.getName()));

            try {
                obj.unregister();
            } catch (IllegalStateException e) {}
        } else {
            try {
                o.unregister();
            } catch (IllegalStateException e) {}
        }

        setHistory(p, new History(p, historyMap));


        System.out.println("Set scoreboard2");
        p.setScoreboard(scoreboard);
        System.out.println("score:: " + p.getScoreboard());

    }

    public void reset() {
        scoreboard.getObjectives().forEach(Objective::unregister);
    }

    private static class DataHolder {

        private final ScoreboardTeam sTeam;
        private final Line line;
        private final String toAdd;
        private int adjustedSlot;

        private DataHolder(ScoreboardTeam sTeam, Line line, String toAdd) {
            this.sTeam = sTeam;
            this.line = line;
            this.toAdd = toAdd;
        }

        private Slot slot() {
            return line.slot();
        }

    }

    private void updateScore(Map<Integer, String> historyMap, Objective o, ScoreboardTeam sTeam, Line line, String toAdd) {
        historyMap.put(line.slot().slot, toAdd);
        o.getScore(sTeam.slot().entry()).setScore(line.slot().slot);
    }

    /**
     * Adjust the slot value and return it, returning the appropriate score to set on the scoreboard
     * This is dependant on the amount of lines being set on the scoreboard at the time
     * @param lines The total amount of visible lines on the scoreboard
     * @return The adjusted score for this slot
     */
    private void adjustSlots(List<DataHolder> lines) {
        if (lines.size() > Slot.values().length)
            throw new IllegalStateException("Cannot adjust slot for lines of greater than " + Slot.values().length + ", received " + lines + " lines");
        int i = 0;
        Comparator<DataHolder> comparator = Comparator.comparing(DataHolder::slot);
        for (DataHolder holder : lines.stream().sorted(comparator.reversed()).collect(Collectors.toList())) {
            holder.adjustedSlot = i++;
        }
    }

    private List<Line> getOrderedLines(Player p) {
        List<Line> lines = new ArrayList<>();
        Comparator<Line> lineComparator = Comparator.comparing(Line::slot, Enum::compareTo);
        for (Line line : getLines(p).stream().sorted(lineComparator).collect(Collectors.toList())) {
            if (!line.isVisible()) continue;
            if (line.content(p) == null) continue;
            lines.add(line);
        }
        return lines;
    }

    private ScoreboardTeam getScoreboardTeam(Line line) {
        return teams.stream().filter(s -> s.slot() == line.slot()).findFirst().orElseThrow(() -> new IllegalArgumentException("No team with index " + line.slot().slot));
    }

    private static class History {

        private final Player p;
        private final Map<Integer, String> lineMap;

        private History(Player p, Map<Integer, String> lineMap) {
            this.p = p;
            this.lineMap = lineMap;
        }

        private boolean hasChanged(int slot, String content) {
            return !lineMap.containsKey(slot) || !lineMap.get(slot).equals(content);
        }

    }

    private void setHistory(Player p, History history) {
        this.history.removeIf(h -> h.p.getUniqueId().equals(p.getUniqueId()));
        this.history.add(history);
    }

    private Optional<History> getHistory(Player p) {
        return this.history.stream().filter(h -> h.p.getUniqueId().equals(p.getUniqueId())).findFirst();
    }

    protected Scoreboard getBukkitScoreboard() {
        return scoreboard;
    }

}
