package org.AzSofware.aTree.config;

import org.AzSofware.aTree.ATree;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final ATree plugin;
    private FileConfiguration config;

    // Cached values
    private String prefix;
    private boolean aztreeEnabled;
    private boolean requireSneak;
    private int maxBlocks;
    private int radius;
    private int maxDistance;
    private int blocksPerTick;
    private int cooldown;
    private boolean smartDetection;

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

    public ConfigManager(ATree plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        cache();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        cache();
    }

    private void cache() {
        prefix = color(config.getString("prefix", "&8[&aAztree&8] "));

        aztreeEnabled = config.getBoolean("aztree.enabled", true);
        requireSneak = config.getBoolean("aztree.require-sneak", true);
        maxBlocks = config.getInt("aztree.max-blocks", 100);
        radius = config.getInt("aztree.radius", 6);
        maxDistance = config.getInt("aztree.max-distance", 6);
        blocksPerTick = config.getInt("aztree.blocks-per-tick", 10);
        cooldown = config.getInt("aztree.cooldown", 2);
        smartDetection = config.getBoolean("aztree.smart-detection", true);

        replantEnabled = config.getBoolean("replant.enabled", true);
        replantChance = config.getInt("replant.chance", 100);

        reminderEnabled = config.getBoolean("reminder.enabled", true);
        reminderInterval = config.getInt("reminder.interval", 5);
        reminderType = config.getString("reminder.type", "actionbar");
        reminderMessage = config.getString("reminder.message", "&aAztree AKTIF! &7(Jongkok untuk digunakan)");

        effectSound = config.getBoolean("effects.sound", true);
        effectParticle = config.getBoolean("effects.particle", true);

        msgEnable = color(config.getString("messages.enable", "%prefix%&aAztree diaktifkan!"));
        msgDisable = color(config.getString("messages.disable", "%prefix%&cAztree dimatikan!"));
        msgNeedSneak = color(config.getString("messages.need-sneak", "%prefix%&eKamu harus jongkok!"));
        msgCooldown = color(config.getString("messages.cooldown", "%prefix%&cTunggu %time% detik!"));
        msgNotTree = color(config.getString("messages.not-tree", "%prefix%&cIni bukan pohon!"));
    }

    /**
     * Translate legacy & color codes to Adventure components
     */
    public Component format(String message) {
        String parsed = message.replace("%prefix%", prefix);
        // Convert & codes to legacy format then to adventure
        parsed = parsed.replace("&", "§");
        return Component.text(parsed).asComponent();
    }

    public Component formatCooldown(String message, double time) {
        String timeStr = String.format("%.1f", time);
        return format(message.replace("%time%", timeStr));
    }

    private String color(String text) {
        if (text == null) return "";
        return text;
    }

    // Getters

    public boolean isAztreeEnabled() { return aztreeEnabled; }
    public boolean isRequireSneak() { return requireSneak; }
    public int getMaxBlocks() { return maxBlocks; }
    public int getRadius() { return radius; }
    public int getMaxDistance() { return maxDistance; }
    public int getBlocksPerTick() { return blocksPerTick; }
    public int getCooldown() { return cooldown; }
    public boolean isSmartDetection() { return smartDetection; }

    public boolean isReplantEnabled() { return replantEnabled; }
    public int getReplantChance() { return replantChance; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public int getReminderInterval() { return reminderInterval; }
    public String getReminderType() { return reminderType; }
    public String getReminderMessage() { return reminderMessage; }

    public boolean isEffectSound() { return effectSound; }
    public boolean isEffectParticle() { return effectParticle; }

    public String getPrefix() { return prefix; }
    public String getMsgEnable() { return msgEnable; }
    public String getMsgDisable() { return msgDisable; }
    public String getMsgNeedSneak() { return msgNeedSneak; }
    public String getMsgCooldown() { return msgCooldown; }
    public String getMsgNotTree() { return msgNotTree; }
}
