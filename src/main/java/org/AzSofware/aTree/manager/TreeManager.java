package org.AzSofware.aTree.manager;

import org.AzSofware.aTree.ATree;
import org.AzSofware.aTree.util.TreeUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Core manager for TreeCapitator logic.
 *
 * <p>Uses BFS (Breadth-First Search) to collect all connected logs,
 * then breaks them smoothly per-tick using a Queue.</p>
 */
public class TreeManager {

    private final ATree plugin;

    // Active tasks per player (to cancel on logout/disable)
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public TreeManager(ATree plugin) {
        this.plugin = plugin;
    }

    /**
     * Start the smooth tree break for a player at a given block.
     *
     * @param player   The player breaking the tree
     * @param origin   The first log block broken
     */
    public void startTreeBreak(Player player, Block origin) {
        // Cancel any existing task for this player
        cancelTask(player.getUniqueId());

        // Collect all tree blocks via BFS
        List<Block> treeBlocks = collectTree(origin);

        if (treeBlocks.isEmpty()) return;

        // Record the origin location and material for replanting
        Location originLoc = origin.getLocation().clone();
        Material originType = origin.getType();

        // Apply cooldown
        plugin.getCooldownManager().applyCooldown(player.getUniqueId());

        Queue<Block> breakQueue = new LinkedList<>(treeBlocks);
        int blocksPerTick = plugin.getConfigManager().getBlocksPerTick();

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (breakQueue.isEmpty()) {
                    // All blocks broken — schedule replant
                    int totalBlocks = treeBlocks.size();
                    // Add extra delay so drops settle: 1 tick per block batch
                    long replantDelay = (long) Math.ceil((double) totalBlocks / blocksPerTick) + 5;
                    plugin.getReplantManager().scheduleReplant(originLoc, originType, replantDelay);
                    activeTasks.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                // Break up to blocksPerTick blocks this tick
                for (int i = 0; i < blocksPerTick; i++) {
                    Block block = breakQueue.poll();
                    if (block == null) break;

                    // Safety check: make sure it's still a log (player might be near)
                    if (!TreeUtil.isLog(block.getType())) continue;

                    // Effects
                    playEffects(block);

                    // Break block (drop items naturally)
                    block.breakNaturally(player.getInventory().getItemInMainHand());
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);

        activeTasks.put(player.getUniqueId(), task);
    }

    /**
     * BFS tree collection.
     * Starts from the origin block and finds all connected logs.
     *
     * @param origin Starting block
     * @return List of blocks to break (ordered BFS, bottom-up)
     */
    private List<Block> collectTree(Block origin) {
        int maxBlocks = plugin.getConfigManager().getMaxBlocks();
        int maxDistance = plugin.getConfigManager().getMaxDistance();

        List<Block> result = new ArrayList<>();
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        queue.add(origin);
        visited.add(origin);

        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        while (!queue.isEmpty() && result.size() < maxBlocks) {
            Block current = queue.poll();
            result.add(current);

            // Check all 6 faces + diagonal variants for logs
            for (Block neighbor : getNeighbors(current)) {
                if (visited.contains(neighbor)) continue;
                if (!TreeUtil.isLog(neighbor.getType())) continue;

                // Distance check from origin
                int dx = Math.abs(neighbor.getX() - originX);
                int dy = Math.abs(neighbor.getY() - originY);
                int dz = Math.abs(neighbor.getZ() - originZ);
                if (dx > maxDistance || dy > maxDistance * 2 || dz > maxDistance) continue;

                // Don't load new chunks
                if (!neighbor.getChunk().isLoaded()) continue;

                visited.add(neighbor);
                queue.add(neighbor);
            }
        }

        return result;
    }

    /**
     * Get all 6 face-adjacent blocks plus 8 diagonal variants for log finding.
     * We check diagonals because some large trees (dark oak, jungle) have non-straight trunks.
     */
    private List<Block> getNeighbors(Block block) {
        List<Block> neighbors = new ArrayList<>(18);
        int[] offsets = {-1, 0, 1};

        for (int dx : offsets) {
            for (int dy : offsets) {
                for (int dz : offsets) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    // Limit horizontal diagonals - prefer vertical traversal
                    if (Math.abs(dx) == 1 && Math.abs(dz) == 1 && dy == 0) continue;
                    neighbors.add(block.getRelative(dx, dy, dz));
                }
            }
        }
        return neighbors;
    }

    /**
     * Play break effects at a block location.
     */
    private void playEffects(Block block) {
        if (plugin.getConfigManager().isEffectSound()) {
            block.getWorld().playSound(
                    block.getLocation(),
                    Sound.BLOCK_WOOD_BREAK,
                    1.0f,
                    0.8f + (float) (Math.random() * 0.4f) // Slight pitch variation
            );
        }

        if (plugin.getConfigManager().isEffectParticle()) {
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

    /**
     * Cancel an active task for a player.
     */
    public void cancelTask(UUID uuid) {
        BukkitTask task = activeTasks.remove(uuid);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    /**
     * Cancel all active tasks (on plugin disable).
     */
    public void cancelAll() {
        activeTasks.values().forEach(t -> {
            if (!t.isCancelled()) t.cancel();
        });
        activeTasks.clear();
    }

    /**
     * Check whether a player has an active tree break task running.
     */
    public boolean hasActiveTask(UUID uuid) {
        return activeTasks.containsKey(uuid);
    }
}
