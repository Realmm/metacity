package org.metacity.metacity.plot;

import org.bukkit.block.Block;
import org.metacity.metacity.player.MetaPlayer;

import java.util.Collection;
import java.util.Optional;

public interface Plot {

    Collection<Block> getBlocks();

    Optional<MetaPlayer> getOwner();

    void register(MetaPlayer p);

}
