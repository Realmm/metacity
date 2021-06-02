package org.metacity.metacity;

import org.bukkit.OfflinePlayer;
import org.metacity.core.CorePlugin;
import org.metacity.metacity.commands.Commands;
import org.metacity.metacity.enjin.Enjin;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.player.PlayerManager;

public class MetaCity extends CorePlugin {

    private static MetaCity plugin;
    private PlayerManager playerManager;
    private Enjin enjin;

    @Override
    public void onEnable() {
        plugin = this;
        playerManager = new PlayerManager();
        enjin = new Enjin();

        Commands.init();
    }

    public static MetaCity getInstance() {
        return plugin;
    }

    public Enjin getEnjin() {
        return enjin;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

}
