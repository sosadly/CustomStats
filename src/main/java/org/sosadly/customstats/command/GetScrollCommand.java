package org.sosadly.customstats.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.sosadly.customstats.CustomStats;
import org.sosadly.customstats.manager.LocaleManager;
import org.sosadly.customstats.util.ItemUtil;

public class GetScrollCommand implements CommandExecutor {
    private final LocaleManager localeManager;
    public GetScrollCommand(CustomStats plugin) { this.localeManager = plugin.getLocaleManager(); }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(localeManager.getMessage("error.player_only"));
            return true;
        }
        int amount = 1;
        if (args.length > 1) {
            try { amount = Integer.parseInt(args[1]); } catch (NumberFormatException e) {
                player.sendMessage(localeManager.getMessage("error.invalid_amount"));
                return true;
            }
        }
        ItemStack scroll = ItemUtil.createAffixScroll(amount);
        player.getInventory().addItem(scroll);
        player.sendMessage(localeManager.getMessage("command.getscroll.success", "amount", String.valueOf(amount)));
        return true;
    }
}