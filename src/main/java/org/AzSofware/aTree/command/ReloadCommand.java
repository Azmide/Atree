package org.AzSofware.aTree.command;

import org.AzSofware.aTree.ATree;
import org.AzSofware.aTree.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * /aztree reload - reloads the plugin configuration.
 * Permission: aztree.reload
 */
public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final ATree plugin;

    public ReloadCommand(ATree plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("aztree.reload")) {
            sender.sendMessage(cfg.format(cfg.getMsgNoPermission()));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(cfg.format(cfg.getMsgUsage()));
            return true;
        }

        cfg.reload();

        // Restart reminder with new interval
        plugin.getReminderManager().restart();

        sender.sendMessage(cfg.format(cfg.getMsgReloaded()));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("aztree.reload")) {
            return List.of("reload");
        }
        return Collections.emptyList();
    }
}
