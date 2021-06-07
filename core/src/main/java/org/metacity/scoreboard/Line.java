package org.metacity.scoreboard;

import org.bukkit.entity.Player;

public class Line {

    private int index;
    private LineExecution execution;
    private boolean append;
    private boolean visible = true;
    private boolean update;

    public Line(LineExecution execution, boolean append) {
        this.execution = execution;
        this.append = append;
    }

    public Line(String s, boolean append) {
        this(p -> s, append);
    }

    public Line(String s) {
        this(p -> s, true);
    }

    public Line(LineExecution execution) {
        this(execution, true);
    }

    void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public LineExecution getContent() {
        return execution;
    }

    public String getContent(Player p) {
        return execution.execute(p);
    }

    public void setContent(LineExecution execution) {
        this.execution = execution;
        setUpdate(true);
    }

    public void setContent(String s) {
        this.execution = p -> s;
        setUpdate(true);
    }

    public void setAppend(boolean append) {
        this.append = append;
        setUpdate(true);
    }

    public boolean getAppend() {
        return append;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        setUpdate(true);
    }

    public boolean isVisible() {
        return visible;
    }

    public Line clone() {
        Line line = new Line(execution);
        line.setIndex(index);
        line.setAppend(append);
        line.setVisible(visible);
        line.setUpdate(update);
        return line;
    }

    boolean getUpdate() {
        return update;
    }

    void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean equals(Line line, Player p) {
        return line.getContent(p).equals(getContent(p)) && line.getAppend() == append && line.isVisible() == visible;
    }

}
