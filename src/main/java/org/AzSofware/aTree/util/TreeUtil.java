package org.AzSofware.aTree.util;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.EnumSet;
import java.util.Set;

/**
 * Utility class for tree-related material checks and sapling mapping.
 */
public final class TreeUtil {

    private TreeUtil() {}

    // All log/wood/stem materials valid for TreeCapitator
    public static final Set<Material> LOG_MATERIALS = EnumSet.of(
            // Overworld logs
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.CHERRY_LOG, Material.MANGROVE_LOG,
            // Stripped logs
            Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG,
            Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_JUNGLE_LOG,
            Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
            Material.STRIPPED_CHERRY_LOG, Material.STRIPPED_MANGROVE_LOG,
            // Wood (bark on all sides)
            Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.BIRCH_WOOD,
            Material.JUNGLE_WOOD, Material.ACACIA_WOOD, Material.DARK_OAK_WOOD,
            Material.CHERRY_WOOD, Material.MANGROVE_WOOD,
            // Stripped wood
            Material.STRIPPED_OAK_WOOD, Material.STRIPPED_SPRUCE_WOOD,
            Material.STRIPPED_BIRCH_WOOD, Material.STRIPPED_JUNGLE_WOOD,
            Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_DARK_OAK_WOOD,
            Material.STRIPPED_CHERRY_WOOD, Material.STRIPPED_MANGROVE_WOOD,
            // Nether stems
            Material.CRIMSON_STEM, Material.WARPED_STEM,
            Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM,
            Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE,
            Material.STRIPPED_CRIMSON_HYPHAE, Material.STRIPPED_WARPED_HYPHAE
    );

    // All leaf materials
    public static final Set<Material> LEAF_MATERIALS = EnumSet.of(
            Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES,
            Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
            Material.CHERRY_LEAVES, Material.MANGROVE_LEAVES, Material.AZALEA_LEAVES,
            Material.FLOWERING_AZALEA_LEAVES
    );

    public static boolean isLog(Material material) {
        return LOG_MATERIALS.contains(material);
    }

    public static boolean isLeaf(Material material) {
        return LEAF_MATERIALS.contains(material);
    }

    public static boolean isLog(Block block) {
        return isLog(block.getType());
    }

    public static boolean isLeaf(Block block) {
        return isLeaf(block.getType());
    }

    public static boolean hasNearbyLeaves(Block block, int radius) {
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();

        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= radius + 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block relative = block.getWorld().getBlockAt(bx + x, by + y, bz + z);
                    if (isLeaf(relative.getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasVerticalStructure(Block block) {
        // Check if there's a log directly above or below
        Block above = block.getRelative(0, 1, 0);
        Block below = block.getRelative(0, -1, 0);
        return isLog(above.getType()) || isLog(below.getType());
    }

    public static Material getSapling(Material log) {
        return switch (log) {
            case OAK_LOG, OAK_WOOD,
                 STRIPPED_OAK_LOG, STRIPPED_OAK_WOOD -> Material.OAK_SAPLING;
            case SPRUCE_LOG, SPRUCE_WOOD,
                 STRIPPED_SPRUCE_LOG, STRIPPED_SPRUCE_WOOD -> Material.SPRUCE_SAPLING;
            case BIRCH_LOG, BIRCH_WOOD,
                 STRIPPED_BIRCH_LOG, STRIPPED_BIRCH_WOOD -> Material.BIRCH_SAPLING;
            case JUNGLE_LOG, JUNGLE_WOOD,
                 STRIPPED_JUNGLE_LOG, STRIPPED_JUNGLE_WOOD -> Material.JUNGLE_SAPLING;
            case ACACIA_LOG, ACACIA_WOOD,
                 STRIPPED_ACACIA_LOG, STRIPPED_ACACIA_WOOD -> Material.ACACIA_SAPLING;
            case DARK_OAK_LOG, DARK_OAK_WOOD,
                 STRIPPED_DARK_OAK_LOG, STRIPPED_DARK_OAK_WOOD -> Material.DARK_OAK_SAPLING;
            case CHERRY_LOG, CHERRY_WOOD,
                 STRIPPED_CHERRY_LOG, STRIPPED_CHERRY_WOOD -> Material.CHERRY_SAPLING;
            case MANGROVE_LOG, MANGROVE_WOOD,
                 STRIPPED_MANGROVE_LOG, STRIPPED_MANGROVE_WOOD -> Material.MANGROVE_PROPAGULE;
            default -> null; // Nether stems don't have saplings
        };
    }
}