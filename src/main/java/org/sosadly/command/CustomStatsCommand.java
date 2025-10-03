package org.sosadly.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.sosadly.CustomStats;
import org.sosadly.manager.LocaleManager;

import java.util.HashMap;
import java.util.Map;

public class CustomStatsCommand implements CommandExecutor {
    private final Map<String, CommandExecutor> subCommands = new HashMap<>();
    private final LocaleManager localeManager;

    public CustomStatsCommand(CustomStats plugin) {
        this.localeManager = plugin.getLocaleManager();
        subCommands.put("reload", new ReloadCommand(plugin));
        subCommands.put("getscroll", new GetScrollCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(localeManager.getMessage("command.usage", "label", label));
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        CommandExecutor subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            sender.sendMessage(localeManager.getMessage("error.unknown_command"));
            return true;
        }
        if (!sender.hasPermission("customstats.admin")) {
            sender.sendMessage(localeManager.getMessage("error.no_permission"));
            return true;
        }
        return subCommand.onCommand(sender, command, label, args);
    }
}