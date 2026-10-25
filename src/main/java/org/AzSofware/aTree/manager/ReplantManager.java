package org.AzSofware.aTree.manager;

import org.AzSofware.aTree.ATree;
import org.AzSofware.aTree.util.TreeUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Handles auto-replanting of saplings after a tree is felled. */
public class ReplantManager {

    private final ATree plugin;

    public ReplantManager(ATree plugin) {
        this.plugin = plugin;
    }

    /**
     * Replant the given trunk bases once the felling is done.
     *
     * @param bases      positions where a log used to stand on the ground
     * @param logType    log type of the tree, decides the sapling
     * @param delayTicks ticks to wait so the last logs are gone first
     */
    public void scheduleReplant(List<Block> bases, Material logType, long delayTicks) {
        if (!plugin.getConfigManager().isReplantEnabled() || bases.isEmpty()) return;

        Material sapling = TreeUtil.getSapling(logType);
        if (sapling == null) return;

        int chance = plugin.getConfigManager().getReplantChance();
        if (chance < 100 && ThreadLocalRandom.current().nextInt(100) >= chance) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            BlockData data = sapling.createBlockData();
            for (Block block : bases) {
                if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) continue;
                if (!block.getType().isAir()) continue;
                if (!data.isSupported(block)) continue;
                block.setBlockData(data);
            }
        }, delayTicks);
    }
}
