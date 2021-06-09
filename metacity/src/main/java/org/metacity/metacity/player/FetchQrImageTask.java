package org.metacity.metacity.player;

import lombok.NonNull;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.metacity.metacity.MetaCity;

import javax.imageio.ImageIO;
import java.net.URL;

public class FetchQrImageTask extends BukkitRunnable {

    private static final Long TASK_DELAY  = 1L;
    private static final Long TASK_PERIOD = 2L;

    private final MetaPlayer p;
    private final String url;

    private FetchQrImageTask() {
        throw new IllegalStateException();
    }

    protected FetchQrImageTask(@NonNull MetaPlayer player,
                               @NonNull String url) throws NullPointerException {
        this.p = player;
        this.url = url;
    }

    @Override
    public void run() {
        if (isCancelled()) return;

        if (!p.player().isPresent()) {
            cancel();
        } else {
            try {
                p.setLinkingCodeQr(ImageIO.read(new URL(url)));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cancel();
            }
        }
    }

    public static BukkitTask fetch(MetaPlayer player, String url) {
        if (player == null || url == null)
            return null;

        FetchQrImageTask task = new FetchQrImageTask(player, url);
        // Note: TASK_PERIOD is measured in server ticks 20 ticks / second.
        return task.runTaskTimerAsynchronously(MetaCity.getInstance(), TASK_DELAY, TASK_PERIOD);
    }

}
