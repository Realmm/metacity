package org.metacity.metacity;

import org.bukkit.scheduler.BukkitRunnable;

public class AuthenticationTask extends BukkitRunnable {

    private final SpigotBootstrap bootstrap;

    public AuthenticationTask(SpigotBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void run() {
        bootstrap.authenticateTPClient();
    }

}
