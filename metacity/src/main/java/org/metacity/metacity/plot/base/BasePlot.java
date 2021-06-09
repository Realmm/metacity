package org.metacity.metacity.plot.base;

import org.bukkit.block.Block;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.plot.Plot;

import java.util.Collection;
import java.util.Optional;

public abstract class BasePlot implements Plot {

    public Collection<Block> getBlocks() {

    };

    public Optional<MetaPlayer> getOwner() {

    };

    public void register(MetaPlayer p) {
        if (getOwner().isPresent()) throw new IllegalStateException("Cannot register plot as it has already been registered to " + getOwner().get().getEthereumAddress());
    };

}
