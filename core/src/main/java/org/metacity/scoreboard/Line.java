package org.metacity.scoreboard;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class Line {

    private @Nullable LineExecution execution;
    private boolean visible = true;
    private boolean refresh;
    private Slot slot;

    Line(@Nullable LineExecution execution, Slot slot) {
        this.execution = execution;
        this.slot = slot;
    }

    Line(@Nullable String s, Slot slot) {
        this(s == null ? null : p -> s, slot);
    }

    public Slot slot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
        setToRefresh(true);
    }

    public LineExecution content() {
        return execution;
    }

    public String content(Player p) {
        return execution == null ? "" : execution.execute(p);
    }

    public void setContent(LineExecution execution) {
        this.execution = execution;
        setToRefresh(true);
    }

    public void setContent(String s) {
        this.execution = p -> s;
        setToRefresh(true);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        setToRefresh(true);
    }

    public boolean isVisible() {
        return execution != null && visible;
    }

    public Line clone() {
        Line line = new Line(execution, slot);
        line.setVisible(visible);
        line.setToRefresh(refresh);
        return line;
    }

    //Whether the line should be refreshed on the scoreboard
    boolean shouldRefresh(Player p) {
        return refresh ;
    }

    //Set if this line should up refreshed on the scoreboard
    private void setToRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean equals(Line line, Player p) {
        return line.content(p).equals(content(p)) && line.slot().slot == slot().slot && line.isVisible() == visible;
    }

}
