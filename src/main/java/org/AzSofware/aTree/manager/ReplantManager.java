package org.AzSofware.aTree.manager;

import org.AzSofware.aTree.ATree;
import org.AzSofware.aTree.util.TreeUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * Handles auto-replanting of saplings after tree destruction.
 */
public class ReplantManager {

    private final ATree plugin;
    private final Random random = new Random();

    public ReplantManager(ATree plugin) {
        this.plugin = plugin;
    }

    /**
     * Schedule a replant at the given location for a specific log type.
     * Runs 1 tick after the queue finishes to ensure blocks are broken first.
     *
     * @param location  Location of the original bottom log
     * @param logType   Material type of the log (to determine sapling)
     * @param delayTicks Delay in ticks before replanting
     */
    public void scheduleReplant(Location location, Material logType, long delayTicks) {
        if (!plugin.getConfigManager().isReplantEnabled()) return;

        Material sapling = TreeUtil.getSapling(logType);
        if (sapling == null) return;

        int chance = plugin.getConfigManager().getReplantChance();

        new BukkitRunnable() {
            @Override
            public void run() {
                // Roll chance
                if (chance < 100 && random.nextInt(100) >= chance) return;

                Block block = location.getBlock();
                Block below = block.getRelative(BlockFace.DOWN);

                // Only place if:
                // 1. Current spot is air
                // 2. Block below is solid (can support sapling)
                if (block.getType() != Material.AIR) return;
                if (!below.getType().isSolid()) return;

                block.setType(sapling);
            }
        }.runTaskLater(plugin, delayTicks);
    }
}
