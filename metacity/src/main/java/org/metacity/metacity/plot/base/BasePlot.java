package org.metacity.metacity.plot.base;

import org.bukkit.block.Block;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.plot.Plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public abstract class BasePlot implements Plot {

    public Collection<Block> getBlocks() {
        return new ArrayList<>();
    };

    public Optional<MetaPlayer> getOwner() {
        return Optional.empty();
    };

    public void register(MetaPlayer p) {
        if (getOwner().isPresent()) throw new IllegalStateException("Cannot register plot as it has already been registered to " + getOwner().get().getEthereumAddress());
    };

}
