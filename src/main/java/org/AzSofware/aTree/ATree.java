package org.AzSofware.aTree;

import org.AzSofware.aTree.command.ReloadCommand;
import org.AzSofware.aTree.command.ToggleCommand;
import org.AzSofware.aTree.config.ConfigManager;
import org.AzSofware.aTree.listener.BlockBreakListener;
import org.AzSofware.aTree.manager.CooldownManager;
import org.AzSofware.aTree.manager.ReminderManager;
import org.AzSofware.aTree.manager.ReplantManager;
import org.AzSofware.aTree.manager.TreeManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ATree extends JavaPlugin {

    private static ATree instance;

    private ConfigManager configManager;
    private TreeManager treeManager;
    private CooldownManager cooldownManager;
    private ReplantManager replantManager;
    private ReminderManager reminderManager;

    // Players who have toggled Aztree ON
    private final Set<UUID> enabledPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;

        // Init config
        configManager = new ConfigManager(this);
        configManager.load();

        // Init managers
        cooldownManager = new CooldownManager();
        replantManager = new ReplantManager(this);
        treeManager = new TreeManager(this);
        reminderManager = new ReminderManager(this);

        // Register listener
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);

        // Register commands
        ToggleCommand toggleCmd = new ToggleCommand(this);
        getCommand("wc").setExecutor(toggleCmd);
        getCommand("wc").setTabCompleter(toggleCmd);

        ReloadCommand reloadCmd = new ReloadCommand(this);
        getCommand("aztree").setExecutor(reloadCmd);
        getCommand("aztree").setTabCompleter(reloadCmd);

        // Start reminder task
        reminderManager.start();

        getLogger().info("Aztree v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (reminderManager != null) {
            reminderManager.stop();
        }
        if (treeManager != null) {
            treeManager.cancelAll();
        }
        getLogger().info("Aztree disabled!");
    }

    public static ATree getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TreeManager getTreeManager() {
        return treeManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ReplantManager getReplantManager() {
        return replantManager;
    }

    public ReminderManager getReminderManager() {
        return reminderManager;
    }

    public Set<UUID> getEnabledPlayers() {
        return enabledPlayers;
    }

    public boolean isEnabled(UUID uuid) {
        return enabledPlayers.contains(uuid);
    }

    public void toggle(UUID uuid) {
        if (enabledPlayers.contains(uuid)) {
            enabledPlayers.remove(uuid);
        } else {
            enabledPlayers.add(uuid);
        }
    }
}