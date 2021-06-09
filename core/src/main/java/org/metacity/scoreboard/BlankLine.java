package org.metacity.scoreboard;

public class BlankLine extends Line {

    public BlankLine(Slot slot) {
        super("", slot);
    }

    void setBlankContent(String s) {
        super.setContent(s);
    }

    @Override
    public void setContent(String s) {
        throw new UnsupportedOperationException("Unable to set content of blank line");
    }

    @Override
    public void setContent(LineExecution execution) {
        throw new UnsupportedOperationException("Unable to set content of blank line");
    }

}
