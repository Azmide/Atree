package org.AzSofware.aTree.manager;

import org.AzSofware.aTree.ATree;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Periodically reminds players who have Aztree enabled.
 */
public class ReminderManager {

    private final ATree plugin;
    private BukkitTask task;

    public ReminderManager(ATree plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop(); // Cancel existing task if any

        if (!plugin.getConfigManager().isReminderEnabled()) return;

        int intervalSeconds = plugin.getConfigManager().getReminderInterval();
        long intervalTicks = intervalSeconds * 20L;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!plugin.isEnabled(player.getUniqueId())) continue;

                    Component message = plugin.getConfigManager()
                            .format(plugin.getConfigManager().getReminderMessage());

                    String type = plugin.getConfigManager().getReminderType();
                    if ("actionbar".equalsIgnoreCase(type)) {
                        player.sendActionBar(message);
                    } else {
                        player.sendMessage(message);
                    }
                }
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Restart reminder task (called after reload).
     */
    public void restart() {
        stop();
        start();
    }
}