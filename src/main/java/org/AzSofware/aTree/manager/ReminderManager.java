package org.AzSofware.aTree.manager;

import net.kyori.adventure.text.Component;
import org.AzSofware.aTree.ATree;
import org.AzSofware.aTree.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/** Periodically reminds players who have ATree enabled. */
public class ReminderManager {

    private final ATree plugin;
    private BukkitTask task;

    public ReminderManager(ATree plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();

        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isReminderEnabled()) return;

        long intervalTicks = cfg.getReminderInterval() * 20L;

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            Component message = cfg.format(cfg.getReminderMessage());
            boolean actionBar = "actionbar".equalsIgnoreCase(cfg.getReminderType());

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!plugin.isEnabled(player.getUniqueId())) continue;
                if (actionBar) {
                    player.sendActionBar(message);
                } else {
                    player.sendMessage(message);
                }
            }
        }, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /** Restart the reminder task (called after reload). */
    public void restart() {
        start();
    }
}
