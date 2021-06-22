package org.metacity.util;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkBuilder {

    private boolean instant = false;
    private int power = 2;
    private boolean trail = false;
    private boolean flicker = false;
    private FireworkEffect.Type type = FireworkEffect.Type.BALL;
    private Color[] colors = new Color[0];

    public FireworkBuilder() {

    }

    public static FireworkBuilder builder() {
        return new FireworkBuilder();
    }

    public void go(Location loc) {
        go(loc, 1);
    }

    public void go(Location loc, int amount) {
        for (int i = 0; i < amount; i++) {
            Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            FireworkMeta meta = fw.getFireworkMeta();

            meta.setPower(power);
            meta.addEffect(FireworkEffect.builder().trail(trail).flicker(flicker).with(type).withColor(colors).build());

            fw.setFireworkMeta(meta);

            if (instant)
                fw.detonate();
        }
    }

    public FireworkBuilder instant(boolean instant) {
        this.instant = instant;
        return this;
    }

    public FireworkBuilder power(int power) {
        this.power = power;
        return this;
    }

    public FireworkBuilder trail(boolean trail) {
        this.trail = trail;
        return this;
    }

    public FireworkBuilder flicker(boolean flicker) {
        this.flicker = flicker;
        return this;
    }

    public FireworkBuilder type(FireworkEffect.Type type) {
        this.type = type;
        return this;
    }

    public FireworkBuilder colors(Color... colors) {
        this.colors = colors;
        return this;
    }
}
