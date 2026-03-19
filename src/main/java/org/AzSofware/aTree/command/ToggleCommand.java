package org.AzSofware.aTree.command;

import org.AzSofware.aTree.ATree;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * /wc command - toggles TreeCapitator on/off for the player.
 */
public class ToggleCommand implements CommandExecutor, TabCompleter {

    private final ATree plugin;

    public ToggleCommand(ATree plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        plugin.toggle(player.getUniqueId());
        boolean enabled = plugin.isEnabled(player.getUniqueId());

        String msg = enabled
                ? plugin.getConfigManager().getMsgEnable()
                : plugin.getConfigManager().getMsgDisable();

        player.sendMessage(plugin.getConfigManager().format(msg));

        // If disabling, cancel any active task
        if (!enabled) {
            plugin.getTreeManager().cancelTask(player.getUniqueId());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
