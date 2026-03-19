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

/**
 * Listens to BlockBreakEvent and triggers TreeCapitator logic.
 */
public class BlockBreakListener implements Listener {

    private final ATree plugin;

    public BlockBreakListener(ATree plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // 1. Global plugin toggle
        if (!plugin.getConfigManager().isAztreeEnabled()) return;

        // 2. Block must be a log/wood/stem
        if (!TreeUtil.isLog(block.getType())) return;

        // 3. Player must have Aztree toggled on
        if (!plugin.isEnabled(player.getUniqueId())) return;

        ConfigManager cfg = plugin.getConfigManager();

        // 4. Sneak requirement
        if (cfg.isRequireSneak() && !player.isSneaking()) {
            // Only 1 block breaks normally — do nothing extra
            return;
        }

        // 5. Cooldown check
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), cfg.getCooldown())) {
            double remaining = plugin.getCooldownManager()
                    .getRemainingSeconds(player.getUniqueId(), cfg.getCooldown());
            player.sendMessage(cfg.formatCooldown(cfg.getMsgCooldown(), remaining));
            event.setCancelled(true);
            return;
        }

        // 6. Smart detection — is this a real tree?
        if (cfg.isSmartDetection()) {
            boolean hasLeaves = TreeUtil.hasNearbyLeaves(block, 3);
            boolean hasVertical = TreeUtil.hasVerticalStructure(block);

            if (!hasLeaves || !hasVertical) {
                player.sendMessage(cfg.format(cfg.getMsgNotTree()));
                return;
            }
        }

        // 7. Don't drop the broken block naturally here — TreeManager will handle it
        // We let the original block break happen (drop 1 log for the initial hit),
        // then the remaining tree blocks are broken by the task.

        // 8. Start smooth tree break
        plugin.getTreeManager().startTreeBreak(player, block);
    }
}
