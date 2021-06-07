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
import org.metacity.scoreboard.BlankLine;
import org.metacity.scoreboard.LineExecution;
import org.metacity.util.CC;
import org.metacity.util.Util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A wrapper for {@link Bukkit}'s {@link org.bukkit.scoreboard.Scoreboard} class
 * Allows for easier manipulation of the sidebar {@link org.bukkit.scoreboard.Scoreboard}
 */
public class RealmScoreboard {

    private final int maxLines = 15;
    boolean toUpdate = false;

    private final org.bukkit.scoreboard.Scoreboard scoreboard;
    private Objective objective;

    private LineExecution title;

    final Set<Line> executions = new HashSet<>();
    final Set<Line> oldExecutions = new HashSet<>();
    final Set<Line> setExecutions = new HashSet<>();
    private final Set<ScoreboardTeam> teams = new HashSet<>();

    /**
     * Create a scoreboard with a certain title
     * @param title The title to set
     */
    public RealmScoreboard(String title) {
        this(title, new ArrayList<>());
    }

    /**
     * Create a scoreboard with a certain {@link LineExecution} title
     * @param p The player to execute the {@link LineExecution}
     * @param title The {@link LineExecution} title to set
     */
    public RealmScoreboard(Player p, LineExecution title) {
        this(title.execute(p));
        this.title = title;
    }

    /**
     * Create a scoreboard with a certain title and {@link LineExecution}'s
     * @param title The title to set
     * @param lines The {@link LineExecution}'s to set
     */
    public RealmScoreboard(String title, LineExecution... lines) {
        this(title, Arrays.asList(lines));
    }

    /**
     * Create a scoreboard with a certain title and a list of {@link LineExecution}'s
     * @param title The title to set
     * @param lines The {@link LineExecution}'s to set
     */
    public RealmScoreboard(String title, List<LineExecution> lines) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("obj", "dummy");
        objective.setDisplayName(title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 0; i < 16; i++) {
            Team t = scoreboard.registerNewTeam(String.valueOf(i));
            String entry = String.valueOf(ChatColor.values()[i]);
            teams.add(new ScoreboardTeam(i, t, entry));
        }

        lines.forEach(this::addLine);
    }

    /**
     *
     * @return The maximum amount of lines this scoreboard can set
     */
    public int getMaxLines() {
        return maxLines;
    }

    /**
     * Sets the title of the scoreboard
     * @param title The title to set
     */
    public void setTitle(String title) {
        setTitle(p -> title);
    }

    /**
     * Sets the title of the scoreboard
     * @param title The {@link LineExecution} to set
     */
    public void setTitle(LineExecution title) {
        this.title = title;
    }

    /**
     * Add a line to the scoreboard
     * Maximum amount of lines a scoreboard can have is 15
     * @param s The content of the line to add
     */
    public void addLine(String s) {
        addLine(p -> s);
    }

    /**
     * Add a {@link LineExecution} line to the scoreboard
     * Maximum amount of lines a scoreboard can have is 15
     * @param execution The {@link LineExecution} to add
     */
    public void addLine(LineExecution execution) {
        addLine(new Line(execution));
    }

    public void addLine(Line line) {
        if (getOrderedLines().size() >= maxLines) throw new IndexOutOfBoundsException("You cannot add more than 15 lines.");
        int next = executions.stream().map(Line::getIndex).reduce((i, ii) -> i > ii ? i : ii).orElse(-1) + 1;
        line.setIndex(next);
        executions.add(line);
    }

    /**
     * Adds a blank line to the scoreboard
     */
    public void addBlankLine() {
        StringBuilder sb = new StringBuilder();
        final LineExecution[] execution = {p -> ""};
        while(getOrderedLines().stream().anyMatch(l -> l.getContent().equals(execution[0]))) {
            sb.append(" ");
            execution[0] = p -> sb.toString();
        }
        BlankLine bl = new BlankLine();
        bl.setBlankContent(execution[0].execute(null));
        addLine(bl);
    }

    /**
     * Sets a specific line to be blank
     * @param index The index of the line to modify
     */
    public void setBlankLine(int index) {
        StringBuilder sb = new StringBuilder();
        final LineExecution[] execution = {p -> ""};
        if (setExecutions.stream().anyMatch(s -> {
            String st = null;

            try {
                st = s.getContent().execute(null);
            } catch (NullPointerException ignored) {}

            if (st == null) return false;
            boolean isBlankLine = StringUtils.isBlank(st);
            return s.getIndex() == index && isBlankLine;
        })) return;

        while(getOrderedLines().stream().anyMatch(l -> l.getContent().equals(execution[0]))) {
            sb.append(" ");
            execution[0] = p -> sb.toString();
        }
        BlankLine bl = new BlankLine();
        bl.setIndex(index);
        bl.setBlankContent(execution[0].execute(null));
        setLine(index, bl);
    }

    /**
     * Removes specific lines from a scoreboard
     * @param index The lines index to remove
     */
    public void removeLines(int... index) {
        Arrays.stream(index).forEach(this::removeLine);
    }

    /**
     * Removes a specific line on a scoreboard
     * @param index The lines index to remove
     */
    public void removeLine(int index) {
        if (setExecutions.removeIf(e -> e.getIndex() == index)) {
            toUpdate = true;
        }
    }

    /**
     * Set a specific line on a scoreboard
     * Appends to bottom of scoreboard if the index is higher than the amount of lines
     * If, after using this method, you add more lines, this line will retain its index
     * Maximum amount of lines a scoreboard can have is 15
     * @param index The line number that you want to set
     * @param execution The {@link LineExecution} you want to set the line to
     * @param append Whether it should append to the bottom of scoreboard, if it can
     */
    public void setLine(int index, LineExecution execution, boolean append) {
        Line line = new Line(execution);
        line.setAppend(append);
        setLine(index, line);
    }

    public void setLine(int index, Line line) {
        if (index > maxLines || index <= 0) throw new IndexOutOfBoundsException("Line index out of bounds (1-15), line attempted to be set at '" + index + "'");
        if (getOrderedLines().size() >= maxLines) throw new IndexOutOfBoundsException("You cannot add more than 15 lines.");
        line.setIndex(index);
        setExecutions.removeIf(e -> e.getIndex() == index);
        setExecutions.add(line);
    }

    /**
     * Set a specific line on a scoreboard
     * Appends to bottom of scoreboard if the index is higher than the amount of lines
     * If, after using this method, you add more lines, this line will retain its index
     * Maximum amount of lines a scoreboard can have is 15
     * @param index The line number that you want to set
     * @param s The content of the line that you want to set
     * @param append Whether it should append to the bottom of scoreboard, if it can
     */
    public void setLine(int index, String s, boolean append) {
        setLine(index, p -> s, append);
    }

    /**
     * Set a specific line on a scoreboard
     * Appends to bottom of scoreboard if the index is higher than the amount of lines
     * If, after using this method, you add more lines, this line will retain its index
     * Maximum amount of lines a scoreboard can have is 15
     * @param index The line number that you want to set
     * @param s The content of the line that you want to set
     */
    public void setLine(int index, String s) {
        setLine(index, p -> s, true);
    }

    /**
     * Set a specific line on a scoreboard
     * Appends to bottom of scoreboard if the index is higher than the amount of lines
     * If, after using this method, you add more lines, this line will retain its index
     * Maximum amount of lines a scoreboard can have is 15
     * @param index The line number that you want to set
     * @param execution The {@link LineExecution} you want to set the line to
     */
    public void setLine(int index, LineExecution execution) {
        setLine(index, execution, true);
    }

    /**
     * Removes the scoreboard from the {@link Player}
     * @param p The {@link Player} to remove the scoreboard from
     */
    public void remove(Player p) {
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    /**
     * Updates a players scoreboard with the updated values
     * This also applies the scoreboard to the player if they don't have one
     * This should be called after modifying the scoreboard in any way
     * @param p The {@link Player} to update
     */
    public void update(Player p) {
        if (executions.size() == 0 && setExecutions.size() == 0) {
            if (p.getScoreboard() != null) remove(p);
            toUpdate = false;
            return;
        }

        //Updates players title if it can
        if (title != null) objective.setDisplayName(Util.color(title.execute(p)));

        //Get the final sorted set and loop through it
//            Logger.debug("Line: " + line.getContent(p));
//            if ((!oldExecutions.isEmpty() && oldExecutions.stream().anyMatch(l -> l.equals(line, p))) ||
//            !toUpdate ||
//                    !line.getUpdate()) return;

            //Find the team for this index (cached on instantiation)
//            Logger.debug("Updating line: " + line.getIndex() + " " + line.getContent(p));


        String name = objective.getName().equals("obj") ? "obj2" : "obj";
        String criteria = "dummy";
        Objective o = scoreboard.registerNewObjective(name, criteria);
        if (title == null) {
            o.setDisplayName(objective.getDisplayName());
        } else o.setDisplayName(Util.color(title.execute(p)));
        o.setDisplaySlot(objective.getDisplaySlot());

        boolean[] updated = {true};

        getOrderedLines().forEach(line -> {
//            Logger.debug("Line: " + line.getIndex() + " Content: " + line.getContent(p));
//            if ((!oldExecutions.isEmpty() && oldExecutions.stream().anyMatch(l -> l.equals(line, p))) ||
//            !toUpdate ||
//                    !line.getUpdate()) return;
//            Logger.debug("Changed: " + line.getIndex() + " Content: " + line.getContent(p));
//            line.setUpdate(false);
            ScoreboardTeam sbTeam = getScoreboardTeam(line);
            Team team = sbTeam.getTeam();
            String entry = sbTeam.getEntry();
            team.addEntry(entry);

            String toAdd = line.getContent(p);

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

//            Logger.debug("team prefix: " + team.getPrefix() + " suffix: " + team.getSuffix());

            //Updates the objective with the appropriate score and entry

            ScoreboardTeam sTeam = getScoreboardTeam(line);
            o.getScore(sTeam.getEntry()).setScore(line.getIndex());
            updated[0] = true;

            oldExecutions.removeIf(l -> l.getIndex() == line.getIndex());
            oldExecutions.add(line);
        });

//        Logger.debug("updated? " + updated[0]);

        if (updated[0]) {
            Objective obj = objective;
            objective = o;
            obj.unregister();
        } else o.unregister();

        //Sets the players scoreboard to this scoreboard
        if (updated[0])p.setScoreboard(scoreboard);
        toUpdate = false;
    }

    private ScoreboardTeam getScoreboardTeam(Line line) {
        return teams.stream().filter(s -> s.getIndex() == line.getIndex()).findFirst().orElseThrow(() -> new IllegalArgumentException("No team with index " + line.getIndex()));
    }

    /**
     * Creates an exact copy of this {@link RealmScoreboard}
     * @return An exact copy of this {@link RealmScoreboard}
     */
    public RealmScoreboard clone() {
        RealmScoreboard scoreboard = new RealmScoreboard(objective.getDisplayName(), new ArrayList<>(executions.stream().map(Line::getContent).collect(Collectors.toList())));
        scoreboard.setExecutions.addAll(setExecutions);
        return scoreboard;
    }

    Set<Line> getOrderedLines() {
        //Get highest key value in executions map
        int highest = executions.stream().map(Line::getIndex).reduce((i, ii) -> i > ii ? i : ii).orElse(0);

        final Set<Line> reversed = new HashSet<>();
        Set<Line> toSet = new HashSet<>();
        Set<Line> toAppend = new HashSet<>(); //for easy sorting

        //Loops through all 'set' lines, if it can and should append to the bottom
        // of the scoreboard, it does, if it shouldn't it simply sets the line
        setExecutions.forEach(e -> {
            boolean canAppend = e.getIndex() >= highest + 1;
            if (canAppend) {
                if (e.getAppend()) toAppend.add(e);
            } else {
                Line clone = e.clone();
                clone.setIndex(e.getIndex() - 1);
                if (clone.isVisible()) toSet.add(clone);
            }
        });

        Set<Line> pushed = new HashSet<>();

        //Loops through all 'added' lines, if it should be replaced by a 'set' line
        // then it replaces it, otherwise it inserts the 'added' line
//        executions.forEach(line -> pushed.add(line.getIndex(), toSet.getOrDefault(line.getIndex(), line.getContent())));

        for (Line execLine : executions) {
            for (Line setLine : toSet) {
                boolean p = false;
                if (execLine.getIndex() == setLine.getIndex()) {
                    if (setLine.isVisible()) pushed.add(setLine);
                    p = true;
                }
                if (!p) {
                    if (execLine.isVisible()) pushed.add(execLine);
                }
            }
        }


//        executions.forEach(line -> pushed.add(linetoSet.getOrDefault(line.getIndex(), line.getContent())));

        //Reverses the pushed map insertion as scoreboards go from highest(top) -> lowest(bottom), not lowest(top) -> highest(bottom)
        if (pushed.size() > 0) {
            for (int i = highest; i >= 0; i--) {
                final int finalI = i;
                pushed.stream().filter(l -> l.getIndex() == finalI).findFirst().ifPresent(l -> {
                    Line clone = l.clone();
                    clone.setIndex(highest - finalI);
                    if (clone.isVisible()) reversed.add(clone);
                });
            }
        }

        Set<Line> finalToSet = new HashSet<>();

        //If there are any 'set' lines to append to the bottom of the scoreboard, this
        // increases all lines already to go onto the scoreboard by how many it should
        // append by, to make room for the appending lines
        reversed.forEach(line -> {
            Line clone = line.clone();
            clone.setIndex(line.getIndex() + toAppend.size());
            if (clone.isVisible()) finalToSet.add(clone);
        });

        //Loops through the 'set' lines that should append to bottom of scoreboard
        for (int x = 0; x < toAppend.size(); x++) {
            //Puts the map in descending order, ordered by values, and gets the 'x'th value
//            Optional<LineExecution> o = toAppend.descendingSet().stream().skip(x).findFirst();
            Optional<Line> o = toAppend.stream().sorted(Comparator.comparingInt(Line::getIndex).reversed()).skip(x).findFirst();

            final int[] next = {0};

            //Finds the next available slot in the final 'finalToSet' map
            while (finalToSet.stream().anyMatch(l -> l.getIndex() == next[0])) {
                next[0]++;
            }

            //Inserts the appended line to the first available slot
            o.ifPresent(l -> {
                finalToSet.removeIf(li -> li.getIndex() == next[0]);
                Line clone = l.clone();
                clone.setIndex(next[0]);
                if (clone.isVisible()) finalToSet.add(clone);
            });
        }

        return finalToSet;
    }

    protected Scoreboard getBukkitScoreboard() {
        return scoreboard;
    }

}
