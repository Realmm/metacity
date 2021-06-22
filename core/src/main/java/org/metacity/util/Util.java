package org.metacity.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.sounds.SoundEffect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class Util {

    public static String color(String s) {
        return CC.translate('&', s);
    }

    /**
     * This method is confusing but works... change later.
     * @param string The string to translate
     * @param length The length of the string
     * @return The length of the translated string
     */
    @Deprecated
    private static int translateLength(String string, int length) {
        int nonColorCharCount = 0;
        boolean previousWasColorChar = false;
        for (int i = 0; i < string.length(); i++)
            if (previousWasColorChar)
                previousWasColorChar = false;
            else if (string.charAt(i) == ChatColor.COLOR_CHAR)
                previousWasColorChar = true;
            else {
                nonColorCharCount++;
                if (nonColorCharCount == length)
                    return i + 1;
            }
        return string.length();
    }

    /**
     * Send an action bar to the player, which is just small text above the players hotbar
     * @param p The player to send to
     * @param message The message to send
     */
    public static void actionbar(Player p, String message) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    /**
     * Player a {@link Sound} for a player
     * @param p The player to play the sound for
     * @param sound The sound to play
     */
    public static void sound(Player p, Sound sound) {
        sound(p.getEyeLocation(), sound);
    }

    /**
     * Play a {@link Sound} at a location
     * @param l The location to play the sound
     * @param sound The sound to play
     */
    public static void sound(Location l, Sound sound) {
        sound(l, sound, 1, 1);
    }

    /**
     * Player a {@link Sound} for a player
     * @param l The location to play the sound
     * @param sound The sound to play
     * @param volume The volume to play the sound at, for normal, use 1
     * @param pitch The pitch to play the sound at, for normal, use 1
     */
    public static void sound(Location l, Sound sound, float volume, float pitch) {
        Objects.requireNonNull(l.getWorld()).playSound(l, sound, volume, pitch);
    }

    /**
     * Plays like a cloud *whoosh* type of effect.
     *
     * @param loc       Location of where to play the effect
     * @param playSound Optionally play a sound (Ender dragon flap)
     */
    public static void cloudEffect(Location loc, boolean playSound) {
        if (playSound) sound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP);

        for (int i = 0; i < 50; i++) {
            Vector v = Util.getRandomVector();
            ParticleBuilder.of(Particle.CLOUD).location(loc).offset(v.getX(), v.getY(), v.getZ()).count(1).extra(0.3).spawn();
        }
    }

    /**
     * @param entity The entity to check.
     * @return True if the player has the velocity of a player standing on the
     * ground, and there is a block other than air below them.
     */
    public static boolean isOnGround(Entity entity) {
        return (entity.getVelocity().getY() == getGravity() || entity.getVelocity().getY() == -0.0) && entity.getLocation().add(0, -1, 0).getBlock().getType().isSolid();
    }

    /**
     * Decrement the amount of the item in the players hand by 1
     * @param player The players hand the decrement
     */
    public static void decrementHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;

        int newAmount = item.getAmount() - 1;
        if (newAmount <= 0) {
            player.getInventory().setItemInMainHand(null);
            return;
        }

        item.setAmount(newAmount);
        player.getInventory().setItemInMainHand(item);
    }

    /**
     * Get the centre location of the location provided
     * @param loc The location to centre
     * @return The centred location
     */
    public static Location getCenterLocation(Location loc) {
        return loc.getBlock().getLocation().clone().add(0.5, 0.5, 0.5);
    }

    public static Vector toVector(Location loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Get a vector starting at the given location and ending at the given location
     * @param start The location to begin the vector
     * @param end The location to end the vector
     * @return A directional vector in the direction pointing from start to end
     */
    public static Vector getDirectionalVector(Location start, Location end) {
        Vector v1 = toVector(start);
        Vector v2 = toVector(end);
        double distSq = v1.distanceSquared(v2);

        double dist = Math.sqrt(distSq);
        Vector d = v2.subtract(v1);
        Vector norm = d.divide(new Vector(d.getX() / dist, d.getY() / dist, d.getZ() / dist));

        return norm;
    }

    /**
     * Get a random vector, pointing in a random direction
     * @return A randomized vector
     */
    public static Vector getRandomVector() {
        Vector direction = new Vector();
        direction.setX(0.0D + Math.random() - Math.random());
        direction.setY(Math.random());
        direction.setZ(0.0D + Math.random() - Math.random());

        return direction;
    }

    /**
     * @return The force of gravity that acts on all entities every tick. If an entity
     * is on the ground, this is their Y velocity.
     */
    public static double getGravity() {
        return -0.0784000015258789;
    }

    /**
     * @return 50/50 chance
     */
    public static boolean coinFlip() {
        return Util.chance(50);
    }

    /**
     * @param chance The chance out of 100.
     * @return True if the odds were in your favor.
     */
    public static boolean chance(int chance) {
        return chance((1d * chance) / 100);
    }

    /**
     * @param chance The chance out of 1.
     * @return True if the odds were in your favor.
     */
    public static boolean chance(double chance) {
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    /**
     * @param location The location to search around.
     * @param distance The maximum distance from the location.
     * @param entity   The class of the entity to include. <em><u>This needs to
     *                 be the Craft version of the entity class, like
     *                 CraftLivingEntity.class</em></u>, not LivingEntity.class.
     * @return An ArrayList populated with all given entities of the specified
     * type that are within the given distance of the location.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> ArrayList<T> getEntitiesAround(Location location, double distance, Class<T> entity) {
        ArrayList<T> entities = new ArrayList<>();
        for (Entity e : location.getWorld().getEntities())
            if (e.getLocation().distance(location) <= distance && entity.isAssignableFrom(e.getClass()))
                entities.add((T) e);
        return entities;
    }

    /**
     * Player a {@link Sound} for a player
     * @param p The player to play the sound for
     * @param sound The sound to play
     * @param volume The volume to play the sound at, for normal, use 1
     * @param pitch The pitch to play the sound at, for normal, use 1
     */
    public static void sound(Player p, Sound sound, float volume, float pitch) {
        sound(p.getEyeLocation(), sound, volume, pitch);
    }

    /**
     * Wraps the given String, but avoids cutting color characters off.
     *
     * @param string     The String to wrap.
     * @param lineLength The length of each line.
     * @return A list of wrapped text.
     */
    public static List<String> wrapWithColor(String string, int lineLength) {
        int length = translateLength(string, lineLength);
        List<String> lines;
        if (length == string.length()) {
            lines = new LinkedList<>();
            lines.add(string);
        } else {
            int lastSpace = string.lastIndexOf(' ', length);
            length = lastSpace == -1 ? length : lastSpace + 1;
            String line = string.substring(0, length).trim();
            lines = wrapWithColor(ChatColor.getLastColors(line) + string.substring(length).trim(), lineLength);
            lines.add(0, line);
        }
        return lines;
    }

}
