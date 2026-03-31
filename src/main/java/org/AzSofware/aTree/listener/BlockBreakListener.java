package org.AzSofware.aTree.listener;

import org.AzSofware.aTree.ATree;
import org.AzSofware.aTree.config.ConfigManager;
import org.AzSofware.aTree.util.TreeUtil;
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
        Block  block  = event.getBlock();

        // 1. Global switch
        if (!plugin.getConfigManager().isAztreeEnabled()) return;

        // 2. Must be a log block
        if (!TreeUtil.isLog(block.getType())) return;

        // 3. Player must have toggled Aztree ON
        if (!plugin.isEnabled(player.getUniqueId())) return;

        ConfigManager cfg = plugin.getConfigManager();

        // 4. World blacklist check
        String worldName = block.getWorld().getName();
        if (cfg.isWorldBlacklisted(worldName)) {
            player.sendMessage(cfg.format(cfg.getMsgBlacklistedWorld()));
            return;
        }

        // 5. Sneak requirement
        if (cfg.isRequireSneak() && !player.isSneaking()) return;

        // 6. Cooldown check
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), cfg.getCooldown())) {
            double remaining = plugin.getCooldownManager()
                    .getRemainingSeconds(player.getUniqueId(), cfg.getCooldown());
            player.sendMessage(cfg.formatCooldown(cfg.getMsgCooldown(), remaining));
            event.setCancelled(true);
            return;
        }

        // 7. Smart detection
        if (cfg.isSmartDetection()) {
            boolean hasLeaves   = TreeUtil.hasNearbyLeaves(block, 3);
            boolean hasVertical = TreeUtil.hasVerticalStructure(block);
            if (!hasLeaves || !hasVertical) {
                player.sendMessage(cfg.format(cfg.getMsgNotTree()));
                return;
            }
        }

        // 8. Start smooth tree break
        plugin.getTreeManager().startTreeBreak(player, block);
    }
}
