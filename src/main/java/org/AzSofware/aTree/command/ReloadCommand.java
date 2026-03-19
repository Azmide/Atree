package org.AzSofware.aTree.command;

import org.AzSofware.aTree.ATree;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        if (!sender.hasPermission("aztree.reload")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Component.text("Usage: /aztree reload", NamedTextColor.YELLOW));
            return true;
        }

        // Reload config
        plugin.getConfigManager().reload();

        // Restart reminder with new interval
        plugin.getReminderManager().restart();

        sender.sendMessage(plugin.getConfigManager().format(
                plugin.getConfigManager().getPrefix() + "&aConfig reloaded successfully!"
        ));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return Collections.emptyList();
    }
}
