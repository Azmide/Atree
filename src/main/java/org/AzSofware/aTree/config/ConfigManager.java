package org.AzSofware.aTree.config;

import org.AzSofware.aTree.ATree;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private final ATree plugin;
    private FileConfiguration config;
    
    // Legacy serializer: & codes and hex
        private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()          // support &#RRGGBB
                    .useUnusualXRepeatedCharacterHexFormat() // support &x&R&R&G&G&B&B
                    .build();

    // Cached values
    private String prefix;
    private boolean aztreeEnabled;
    private boolean requireSneak;
    private int maxBlocks;
    private int maxDistance;
    private int blocksPerTick;
    private double cooldown;
    private boolean requireAxe;
    private boolean naturalLeavesOnly;
    private boolean sameWoodOnly;
    private boolean toolDamage;
    private int smartRadius;
    private boolean smartDetection;

    private Set<String> blacklistedWorlds;

    private boolean replantEnabled;
    private int replantChance;

    private boolean reminderEnabled;
    private int reminderInterval;
    private String reminderType;
    private String reminderMessage;

    private boolean effectSound;
    private boolean effectParticle;

    private String msgEnable;
    private String msgDisable;
    private String msgNeedSneak;
    private String msgCooldown;
    private String msgNotTree;
    private String msgBlacklistedWorld;
    private String msgNoPermission;
    private String msgOnlyPlayers;
    private String msgUsage;
    private String msgReloaded;

    public ConfigManager(ATree plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        cache();
    }

    private void cache() {
        prefix = config.getString("prefix", "&e&lWC &d>> ");

        aztreeEnabled = config.getBoolean("aztree.enabled", true);
        requireSneak  = config.getBoolean("aztree.require-sneak", true);
        requireAxe    = config.getBoolean("aztree.require-axe", true);
        maxBlocks     = Math.max(1, config.getInt("aztree.max-blocks", 100));
        maxDistance   = Math.max(1, config.getInt("aztree.max-distance", 6));
        blocksPerTick = Math.max(1, config.getInt("aztree.blocks-per-tick", 10));
        cooldown      = Math.max(0, config.getDouble("aztree.cooldown", 2));
        smartDetection = config.getBoolean("aztree.smart-detection", true);
        smartRadius   = Math.max(1, config.getInt("aztree.smart-detection-radius", 3));
        naturalLeavesOnly = config.getBoolean("aztree.natural-leaves-only", true);
        sameWoodOnly  = config.getBoolean("aztree.same-wood-only", true);
        toolDamage    = config.getBoolean("aztree.tool-damage", true);

        // Blacklisted worlds — case-insensitive
        List<String> rawList = config.getStringList("blacklisted-worlds");
        blacklistedWorlds = new HashSet<>();
        for (String w : rawList) blacklistedWorlds.add(w.toLowerCase());

        replantEnabled  = config.getBoolean("replant.enabled", true);
        replantChance   = Math.clamp(config.getInt("replant.chance", 100), 0, 100);

        reminderEnabled  = config.getBoolean("reminder.enabled", true);
        reminderInterval = Math.max(1, config.getInt("reminder.interval", 5));
        reminderType     = config.getString("reminder.type", "actionbar");
        reminderMessage  = config.getString("reminder.message", "&aWC ENABLED! &7(Sneak to use)");

        effectSound    = config.getBoolean("effects.sound", true);
        effectParticle = config.getBoolean("effects.particle", true);

        msgEnable           = config.getString("messages.enable",            "%prefix%&aATree enabled!");
        msgDisable          = config.getString("messages.disable",           "%prefix%&cATree disabled!");
        msgNeedSneak        = config.getString("messages.need-sneak",        "%prefix%&eYou must sneak!");
        msgCooldown         = config.getString("messages.cooldown",          "%prefix%&cWait &e%time% seconds&c!");
        msgNotTree          = config.getString("messages.not-tree",          "%prefix%&cThis is not a tree!");
        msgBlacklistedWorld = config.getString("messages.blacklisted-world", "%prefix%&cATree cannot be used in this world!");
        msgNoPermission     = config.getString("messages.no-permission",     "%prefix%&cYou may not do that.");
        msgOnlyPlayers      = config.getString("messages.only-players",      "%prefix%&cOnly players can use this command.");
        msgUsage            = config.getString("messages.usage",             "%prefix%&eUsage: &f/aztree reload");
        msgReloaded         = config.getString("messages.reloaded",          "%prefix%&aConfig reloaded successfully!");
    }

    /**
     * Format string menjadi Adventure Component.
     * Support:
     *   - & color codes  → &a &c &l dll
     *   - &#RRGGBB       → hex via LegacySerializer  contoh: &#FF5733
     *   - #RRGGBB        → hex via konversi otomatis  contoh: #FF5733
     */
    public Component format(String message) {
        if (message == null) return Component.empty();

        // Ganti %prefix% dulu
        String parsed = message.replace("%prefix%", prefix);

        // Konversi format #RRGGBB → &#RRGGBB agar bisa dibaca LegacySerializer
        parsed = convertHashHex(parsed);

        // Parse via LegacySerializer (support & codes + &#RRGGBB hex)
        return LEGACY.deserialize(parsed);
    }

    public Component formatCooldown(String message, double time) {
        String timeStr = String.format("%.1f", time);
        return format(message.replace("%time%", timeStr));
    }

    /**
     * Konversi #RRGGBB → &#RRGGBB supaya LegacySerializer bisa baca.
     * Contoh: "Halo #FF5733dunia" → "Halo &#FF5733dunia"
     */
    private String convertHashHex(String text) {
        // Regex: # diikuti tepat 6 karakter hex (tidak didahului &)
        return text.replaceAll("(?<!&)#([0-9A-Fa-f]{6})", "&#$1");
    }

    /** Returns true jika world tersebut di-blacklist */
    public boolean isWorldBlacklisted(String worldName) {
        return blacklistedWorlds.contains(worldName.toLowerCase());
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public boolean isAztreeEnabled()       { return aztreeEnabled; }
    public boolean isRequireSneak()        { return requireSneak; }
    public int     getMaxBlocks()          { return maxBlocks; }
        public int     getMaxDistance()        { return maxDistance; }
    public int     getBlocksPerTick()      { return blocksPerTick; }
    public double  getCooldown()           { return cooldown; }
    public boolean isRequireAxe()          { return requireAxe; }
    public boolean isNaturalLeavesOnly()   { return naturalLeavesOnly; }
    public boolean isSameWoodOnly()        { return sameWoodOnly; }
    public boolean isToolDamage()          { return toolDamage; }
    public int     getSmartRadius()        { return smartRadius; }
    public boolean isSmartDetection()      { return smartDetection; }

    public Set<String> getBlacklistedWorlds() { return blacklistedWorlds; }

    public boolean isReplantEnabled()      { return replantEnabled; }
    public int     getReplantChance()      { return replantChance; }

    public boolean isReminderEnabled()     { return reminderEnabled; }
    public int     getReminderInterval()   { return reminderInterval; }
    public String  getReminderType()       { return reminderType; }
    public String  getReminderMessage()    { return reminderMessage; }

    public boolean isEffectSound()         { return effectSound; }
    public boolean isEffectParticle()      { return effectParticle; }

    public String getPrefix()              { return prefix; }
    public String getMsgEnable()           { return msgEnable; }
    public String getMsgDisable()          { return msgDisable; }
    public String getMsgNeedSneak()        { return msgNeedSneak; }
    public String getMsgCooldown()         { return msgCooldown; }
    public String getMsgNotTree()          { return msgNotTree; }
    public String getMsgBlacklistedWorld() { return msgBlacklistedWorld; }
    public String getMsgNoPermission()     { return msgNoPermission; }
    public String getMsgOnlyPlayers()      { return msgOnlyPlayers; }
    public String getMsgUsage()            { return msgUsage; }
    public String getMsgReloaded()         { return msgReloaded; }
}
