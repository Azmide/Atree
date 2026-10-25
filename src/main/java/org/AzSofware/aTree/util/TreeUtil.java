package org.AzSofware.aTree.util;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;

import java.util.EnumSet;
import java.util.Set;

public final class TreeUtil {

    private TreeUtil() {}

    public static final Set<Material> LOG_MATERIALS = EnumSet.of(
            // Overworld logs
            Material.OAK_LOG,       Material.SPRUCE_LOG,    Material.BIRCH_LOG,
            Material.JUNGLE_LOG,    Material.ACACIA_LOG,    Material.DARK_OAK_LOG,
            Material.CHERRY_LOG,    Material.MANGROVE_LOG,  Material.PALE_OAK_LOG,

            // Stripped logs
            Material.STRIPPED_OAK_LOG,      Material.STRIPPED_SPRUCE_LOG,
            Material.STRIPPED_BIRCH_LOG,    Material.STRIPPED_JUNGLE_LOG,
            Material.STRIPPED_ACACIA_LOG,   Material.STRIPPED_DARK_OAK_LOG,
            Material.STRIPPED_CHERRY_LOG,   Material.STRIPPED_MANGROVE_LOG,
            Material.STRIPPED_PALE_OAK_LOG,

            // Wood (bark all sides)
            Material.OAK_WOOD,      Material.SPRUCE_WOOD,   Material.BIRCH_WOOD,
            Material.JUNGLE_WOOD,   Material.ACACIA_WOOD,   Material.DARK_OAK_WOOD,
            Material.CHERRY_WOOD,   Material.MANGROVE_WOOD, Material.PALE_OAK_WOOD,

            // Stripped wood
            Material.STRIPPED_OAK_WOOD,     Material.STRIPPED_SPRUCE_WOOD,
            Material.STRIPPED_BIRCH_WOOD,   Material.STRIPPED_JUNGLE_WOOD,
            Material.STRIPPED_ACACIA_WOOD,  Material.STRIPPED_DARK_OAK_WOOD,
            Material.STRIPPED_CHERRY_WOOD,  Material.STRIPPED_MANGROVE_WOOD,
            Material.STRIPPED_PALE_OAK_WOOD,

            // Nether stems
            Material.CRIMSON_STEM,          Material.WARPED_STEM,
            Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM,
            Material.CRIMSON_HYPHAE,        Material.WARPED_HYPHAE,
            Material.STRIPPED_CRIMSON_HYPHAE, Material.STRIPPED_WARPED_HYPHAE
    );

    public static final Set<Material> LEAF_MATERIALS = EnumSet.of(
            Material.OAK_LEAVES,        Material.SPRUCE_LEAVES,
            Material.BIRCH_LEAVES,      Material.JUNGLE_LEAVES,
            Material.ACACIA_LEAVES,     Material.DARK_OAK_LEAVES,
            Material.CHERRY_LEAVES,     Material.MANGROVE_LEAVES,
            Material.PALE_OAK_LEAVES,   
            Material.AZALEA_LEAVES,     Material.FLOWERING_AZALEA_LEAVES
    );

    public static boolean isLog(Material m)  { return LOG_MATERIALS.contains(m); }
    public static boolean isLeaf(Material m) { return LEAF_MATERIALS.contains(m); }
    public static boolean isLog(Block b)     { return isLog(b.getType()); }
    public static boolean isLeaf(Block b)    { return isLeaf(b.getType()); }

    /**
     * Scan the area around (and above) a log for leaves.
     *
     * @param naturalOnly only count non-persistent leaves, i.e. leaves not placed by a player
     */
    public static boolean hasNearbyLeaves(Block block, int radius, boolean naturalOnly) {
        World world = block.getWorld();
        int bx = block.getX(), by = block.getY(), bz = block.getZ();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (!world.isChunkLoaded((bx + x) >> 4, (bz + z) >> 4)) continue;
                for (int y = -1; y <= radius + 3; y++) {
                    Block b = world.getBlockAt(bx + x, by + y, bz + z);
                    if (!isLeaf(b.getType())) continue;
                    if (naturalOnly && b.getBlockData() instanceof Leaves leaves && leaves.isPersistent()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    /** A log is a valid trunk when another log sits directly above or below it. */
    public static boolean hasVerticalStructure(Block block) {
        return isLog(block.getRelative(0, 1, 0).getType())
                || isLog(block.getRelative(0, -1, 0).getType());
    }

    /** Wood family of a log, e.g. STRIPPED_OAK_WOOD and OAK_LOG are both "OAK". */
    public static String family(Material log) {
        String name = log.name();
        if (name.startsWith("STRIPPED_")) name = name.substring("STRIPPED_".length());
        for (String suffix : new String[]{"_LOG", "_WOOD", "_STEM", "_HYPHAE"}) {
            if (name.endsWith(suffix)) return name.substring(0, name.length() - suffix.length());
        }
        return name;
    }

    /**
     * Map log → sapling for auto-replant.
     */
    public static Material getSapling(Material log) {
        return switch (log) {
            case OAK_LOG, OAK_WOOD,
                 STRIPPED_OAK_LOG, STRIPPED_OAK_WOOD           -> Material.OAK_SAPLING;
            case SPRUCE_LOG, SPRUCE_WOOD,
                 STRIPPED_SPRUCE_LOG, STRIPPED_SPRUCE_WOOD      -> Material.SPRUCE_SAPLING;
            case BIRCH_LOG, BIRCH_WOOD,
                 STRIPPED_BIRCH_LOG, STRIPPED_BIRCH_WOOD        -> Material.BIRCH_SAPLING;
            case JUNGLE_LOG, JUNGLE_WOOD,
                 STRIPPED_JUNGLE_LOG, STRIPPED_JUNGLE_WOOD      -> Material.JUNGLE_SAPLING;
            case ACACIA_LOG, ACACIA_WOOD,
                 STRIPPED_ACACIA_LOG, STRIPPED_ACACIA_WOOD      -> Material.ACACIA_SAPLING;
            case DARK_OAK_LOG, DARK_OAK_WOOD,
                 STRIPPED_DARK_OAK_LOG, STRIPPED_DARK_OAK_WOOD  -> Material.DARK_OAK_SAPLING;
            case CHERRY_LOG, CHERRY_WOOD,
                 STRIPPED_CHERRY_LOG, STRIPPED_CHERRY_WOOD      -> Material.CHERRY_SAPLING;
            case MANGROVE_LOG, MANGROVE_WOOD,
                 STRIPPED_MANGROVE_LOG, STRIPPED_MANGROVE_WOOD  -> Material.MANGROVE_PROPAGULE;
            case PALE_OAK_LOG, PALE_OAK_WOOD,
                 STRIPPED_PALE_OAK_LOG, STRIPPED_PALE_OAK_WOOD  -> Material.PALE_OAK_SAPLING;
            default -> null; // Nether stems have no sapling
        };
    }
}