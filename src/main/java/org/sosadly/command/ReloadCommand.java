package org.sosadly.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.sosadly.CustomStats;

public class ReloadCommand implements CommandExecutor {
    private final CustomStats plugin;
    public ReloadCommand(CustomStats plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.reload();
        sender.sendMessage(plugin.getLocaleManager().getMessage("command.reload.success"));
        return true;
    }
}