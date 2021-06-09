package org.metacity.metacity.plot;

import org.metacity.metacity.plot.base.BasePlot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PlotManager {

    private final List<Plot> plots = new ArrayList<>();

    private static PlotManager plotManager;

    private PlotManager() {

    }

    public static PlotManager of() {
        if (plotManager == null) plotManager = new PlotManager();
        return plotManager;
    }

    public Collection<Plot> getPlots() {
        return Collections.unmodifiableCollection(plots);
    }

}
