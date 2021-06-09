package org.metacity.metacity.player;

import com.enjin.sdk.TrustedPlatformClient;
import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.http.HttpResponse;
import com.enjin.sdk.models.identity.CreateIdentity;
import com.enjin.sdk.models.identity.GetIdentities;
import com.enjin.sdk.models.identity.Identity;
import com.enjin.sdk.models.user.CreateUser;
import com.enjin.sdk.models.user.GetUsers;
import com.enjin.sdk.models.user.User;
import org.bukkit.scheduler.BukkitRunnable;
import org.metacity.metacity.Chain;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.exceptions.GraphQLException;
import org.metacity.metacity.exceptions.NetworkException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerInitializationTask extends BukkitRunnable {

    private static final Long TASK_DELAY = 20L;
    private static final Long TASK_PERIOD = 40L;

    private static final Map<UUID, PlayerInitializationTask> PLAYER_TASKS = new ConcurrentHashMap<>();

    private final MetaPlayer player;

    private boolean inProgress = false;

    protected PlayerInitializationTask(MetaPlayer player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (inProgress || isCancelled()) return;

        inProgress = true;

        if (player == null) {
            cancel();
            return;
        }

        try {
            Chain chain = MetaCity.getInstance().chain();
            if (!player.isUserLoaded()) chain.updateUser(player, u -> {
                player.setUser(u);

                if (player.isUserLoaded() && !player.isIdentityLoaded()) {
                    chain.updateIdentity(player, i -> {
                        if (i != null) {
                            player.setIdentity(i);
                            cancel();
                        }
                    });
                } else if (player.isLoaded() && !isCancelled()) cancel();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.player().ifPresent(p -> {
            player.board().update();
            player.onJoin();
        });

        inProgress = false;
    }

    public static void create(MetaPlayer p) {
        cleanUp(p.uuid());

        PlayerInitializationTask task = new PlayerInitializationTask(p);
        // Note: TASK_PERIOD is measured in server ticks 20 ticks / second.
        task.runTaskTimer(MetaCity.getInstance(), TASK_DELAY, TASK_PERIOD);
    }

    public static void cleanUp(UUID playerUuid) {
        PlayerInitializationTask task = PLAYER_TASKS.remove(playerUuid);
        if (task != null && !task.isCancelled()) task.cancel();
    }

}
