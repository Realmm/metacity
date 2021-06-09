package org.metacity.metacity.world;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {

    private final World world;

    public Generator(String world) {
        WorldCreator creator = new WorldCreator("worlds/" + world);
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);
        creator.hardcore(false);
        creator.type(WorldType.FLAT);
        this.world = creator.createWorld();
        this.world.setDifficulty(Difficulty.PEACEFUL);

    }

    public World world() {
        return world;
    }

}
