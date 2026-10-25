package org.AzSofware.aTree.manager;

import org.AzSofware.aTree.ATree;
import org.AzSofware.aTree.config.ConfigManager;
import org.AzSofware.aTree.util.TreeUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Core manager for TreeCapitator logic.
 *
 * <p>Uses BFS to collect all connected logs, then breaks them smoothly over several ticks.
 * Every extra log fires a real {@link BlockBreakEvent} so claim and protection plugins can veto it.</p>
 */
public class TreeManager {

    private final ATree plugin;

    // Active tasks per player (to cancel on logout/disable)
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    // Players whose own BlockBreakEvent is being fired right now (so our listener skips it)
    private final Set<UUID> breaking = new HashSet<>();

    public TreeManager(ATree plugin) {
        this.plugin = plugin;
    }

    /**
     * Start the smooth tree break for a player at a given block.
     * The origin block itself is broken by the vanilla event that triggered this.
     */
    public void startTreeBreak(Player player, Block origin) {
        UUID uuid = player.getUniqueId();
        if (activeTasks.containsKey(uuid)) return;

        ConfigManager cfg = plugin.getConfigManager();
        Material originType = origin.getType();

        List<Block> treeBlocks = collectTree(origin, cfg);
        if (treeBlocks.size() <= 1) return;

        plugin.getCooldownManager().applyCooldown(uuid);

        List<Block> bases = findBases(treeBlocks);
        List<Block> queue = new ArrayList<>(treeBlocks);
        queue.remove(origin);

        int blocksPerTick = cfg.getBlocksPerTick();

        BukkitTask task = new BukkitRunnable() {
            private int index = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    finish(uuid);
                    return;
                }

                for (int i = 0; i < blocksPerTick && index < queue.size(); i++) {
                    Block block = queue.get(index++);

                    // Safety check: only logs of the same tree that are still standing
                    if (!TreeUtil.isLog(block.getType())) continue;

                    if (!canKeepBreaking(player, cfg)) {
                        index = queue.size();
                        break;
                    }

                    breakLog(player, block, cfg);
                }

                if (index >= queue.size()) {
                    long replantDelay = 5L;
                    plugin.getReplantManager().scheduleReplant(bases, originType, replantDelay);
                    finish(uuid);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);

        activeTasks.put(uuid, task);
    }

    /** The tool must still be an axe (when required) and the player must not have lost it. */
    private boolean canKeepBreaking(Player player, ConfigManager cfg) {
        if (!cfg.isRequireAxe()) return true;
        return Tag.ITEMS_AXES.isTagged(player.getInventory().getItemInMainHand().getType());
    }

    private void breakLog(Player player, Block block, ConfigManager cfg) {
        BlockBreakEvent event = new BlockBreakEvent(block, player);
        breaking.add(player.getUniqueId());
        try {
            plugin.getServer().getPluginManager().callEvent(event);
        } finally {
            breaking.remove(player.getUniqueId());
        }
        if (event.isCancelled()) return;

        playEffects(block, cfg);

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (player.getGameMode() == GameMode.CREATIVE || !event.isDropItems()) {
            block.setType(Material.AIR);
        } else {
            block.breakNaturally(tool);
        }

        if (cfg.isToolDamage() && player.getGameMode() != GameMode.CREATIVE
                && tool.getType().getMaxDurability() > 0) {
            player.damageItemStack(EquipmentSlot.HAND, 1);
        }
    }

    /**
     * BFS tree collection starting at the origin block.
     *
     * @return connected logs, origin first (closest to the origin first)
     */
    private List<Block> collectTree(Block origin, ConfigManager cfg) {
        int maxBlocks = cfg.getMaxBlocks();
        int maxDistance = cfg.getMaxDistance();
        boolean sameWood = cfg.isSameWoodOnly();
        String family = TreeUtil.family(origin.getType());
        World world = origin.getWorld();

        List<Block> result = new ArrayList<>();
        Queue<Block> queue = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();

        queue.add(origin);
        visited.add(origin);

        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        while (!queue.isEmpty() && result.size() < maxBlocks) {
            Block current = queue.poll();
            result.add(current);

            // All 26 neighbours so 2x2 trunks, diagonal branches and big jungle trees are found
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        int x = current.getX() + dx;
                        int y = current.getY() + dy;
                        int z = current.getZ() + dz;

                        if (Math.abs(x - originX) > maxDistance || Math.abs(z - originZ) > maxDistance) continue;
                        if (Math.abs(y - originY) > maxDistance * 2) continue;
                        if (y < world.getMinHeight() || y >= world.getMaxHeight()) continue;

                        // Never load new chunks
                        if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;

                        Block neighbor = world.getBlockAt(x, y, z);
                        if (visited.contains(neighbor)) continue;
                        if (!TreeUtil.isLog(neighbor.getType())) continue;
                        if (sameWood && !TreeUtil.family(neighbor.getType()).equals(family)) continue;

                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return result;
    }

    /** Lowest logs of the tree, the spots where a sapling can be planted again. */
    private List<Block> findBases(List<Block> treeBlocks) {
        int minY = Integer.MAX_VALUE;
        for (Block b : treeBlocks) minY = Math.min(minY, b.getY());

        List<Block> bases = new ArrayList<>();
        for (Block b : treeBlocks) {
            if (b.getY() == minY) bases.add(b);
        }
        return bases;
    }

    private void playEffects(Block block, ConfigManager cfg) {
        if (cfg.isEffectSound()) {
            block.getWorld().playSound(
                    block.getLocation(),
                    Sound.BLOCK_WOOD_BREAK,
                    1.0f,
                    0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f
            );
        }

        if (cfg.isEffectParticle()) {
            block.getWorld().spawnParticle(
                    Particle.BLOCK,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    12,
                    0.3, 0.3, 0.3,
                    0.05,
                    block.getBlockData()
            );
        }
    }

    private void finish(UUID uuid) {
        BukkitTask task = activeTasks.remove(uuid);
        if (task != null) task.cancel();
    }

    /** Cancel an active task for a player. */
    public void cancelTask(UUID uuid) {
        finish(uuid);
    }

    /** Cancel all active tasks (on plugin disable). */
    public void cancelAll() {
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();
    }

    public boolean hasActiveTask(UUID uuid) {
        return activeTasks.containsKey(uuid);
    }

    /** True while one of our own break events is being fired for this player. */
    public boolean isBreaking(UUID uuid) {
        return breaking.contains(uuid);
    }
}
