package org.metacity.metacity.mmo;

import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.player.QuestionablePlayer;
import org.metacity.util.CC;
import org.metacity.util.ParticleBuilder;
import org.metacity.util.Util;

import java.util.Optional;

public abstract class MMOPlayer {

    private final QuestionablePlayer p;
    private double maxHealth = 20,
            maxMana = 200,
            maxShield = 1000,
            health = 20,
            mana = 200,
            shield = 0;
    private int level = 1;
    private BukkitRunnable runnable;

    public MMOPlayer(OfflinePlayer p) {
        this.p = new QuestionablePlayer(p);
        display();
    }

    private void display() {
        if (this.runnable == null) {
            this.runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    player().ifPresent(p -> {
                        Util.actionbar(p,
                                CC.RED + String.valueOf(health) + "/" + maxHealth + CC.RED + " ♡️ " +
                                        CC.WHITE.bold() + "| " +
                                        CC.BLUE + mana + "/" + maxMana + " ◉ " +
                                        CC.WHITE.bold() + "| " +
                                        CC.GRAY + shield + "/" + maxShield + " &#9671;︎"); //fix all 3 logos, none work properly
                    });
                }
            };
            runnable.runTaskTimer(MetaCity.getInstance(), 0, 1);
        }
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public Optional<Player> player() {
        return p.getPlayer();
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double max) {
        this.maxHealth = max;
    }

    public void setHealth(double health) {
        setHealth(health, false);
    }

    public void kill() {
        p.getPlayer().ifPresent(p -> {
            p.damage(9999);
            ParticleBuilder.of(Particle.REDSTONE)
                    .color(Color.RED)
                    .count(20)
                    .offset(1, 1, 1)
                    .spawn();
            Util.cloudEffect(p.getLocation(), false);
        });
    }

    public void setHealth(double health, boolean damage) {
        boolean death = health <= 0;
        if (death) {
            this.health = 0;
            kill();
            return;
        }
        this.health = Math.max(0, health);
        player().ifPresent(p -> {
            if (this.health <= 20) {
                p.setHealth(this.health);
            } else p.setHealth(20);

            if (damage) {
                double hp = this.health <= 20 ? this.health : 20;
                p.setHealth(hp);
                p.damage(1);
                p.setHealth(hp);
            }
        });

    }

    public double getHealth() {
        return health;
    }

    public double getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(double mana) {
        this.maxMana = mana;
    }

    public void setMana(double mana) {
        this.mana = mana;
    }

    public double getMana() {
        return mana;
    }

    public double getMaxShield() {
        return maxShield;
    }

    public void setMaxShield(double shield) {
        this.maxShield = shield;
    }

    public void setShield(double shield) {
        this.shield = Math.max(0, shield);
    }

    public double getShield() {
        return shield;
    }

    public boolean hasMana() {
        return mana > 0;
    }

    public boolean canPerform(double mana) {
        return mana < getMana();
    }

    public boolean hasShields() {
        return shield > 0;
    }

    public void hit(double damage, boolean strike) {
        player().ifPresent(p -> {
            if (hasShields()) {
                if (damage > shield || shield - damage <= 0) { //If the shield is about to break
                    Util.sound(p, Sound.ITEM_SHIELD_BREAK);
                } else Util.sound(p, Sound.ITEM_SHIELD_BLOCK); //Blocked hit completely

                double after = shield - damage;
                if (after < 0) { //Damage the players health next, as more damage needs to be done
                    double remaining = 0 - after;
                    setHealth(health - remaining, strike);
                }

                setShield(Math.max(0, shield - damage));
            } else {
                setHealth(health - damage, strike);
            }
        });
    }

}
