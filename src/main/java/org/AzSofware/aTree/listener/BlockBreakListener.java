package org.AzSofware.aTree.listener;

import org.AzSofware.aTree.ATree;
import org.AzSofware.aTree.config.ConfigManager;
import org.AzSofware.aTree.util.TreeUtil;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final ATree plugin;

    public BlockBreakListener(ATree plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ConfigManager cfg = plugin.getConfigManager();

        // Ignore the events we fire ourselves for every extra log
        if (plugin.getTreeManager().isBreaking(player.getUniqueId())) return;

        if (!cfg.isAztreeEnabled()) return;
        if (!TreeUtil.isLog(block.getType())) return;
        if (!plugin.isEnabled(player.getUniqueId())) return;
        if (!player.hasPermission("aztree.use")) return;

        if (cfg.isWorldBlacklisted(block.getWorld().getName())) {
            player.sendMessage(cfg.format(cfg.getMsgBlacklistedWorld()));
            return;
        }

        if (cfg.isRequireSneak() && !player.isSneaking()) return;
        if (cfg.isRequireAxe()
                && !Tag.ITEMS_AXES.isTagged(player.getInventory().getItemInMainHand().getType())) return;

        // A tree is still being felled for this player
        if (plugin.getTreeManager().hasActiveTask(player.getUniqueId())) return;

        // Cooldown: the log still breaks normally, only the tree stays up
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), cfg.getCooldown())) {
            double remaining = plugin.getCooldownManager()
                    .getRemainingSeconds(player.getUniqueId(), cfg.getCooldown());
            player.sendMessage(cfg.formatCooldown(cfg.getMsgCooldown(), remaining));
            return;
        }

        if (cfg.isSmartDetection()) {
            boolean hasLeaves = TreeUtil.hasNearbyLeaves(block, cfg.getSmartRadius(), cfg.isNaturalLeavesOnly());
            if (!hasLeaves || !TreeUtil.hasVerticalStructure(block)) {
                player.sendMessage(cfg.format(cfg.getMsgNotTree()));
                return;
            }
        }

        plugin.getTreeManager().startTreeBreak(player, block);
    }
}
