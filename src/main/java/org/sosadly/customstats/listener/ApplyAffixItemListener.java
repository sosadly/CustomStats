package org.sosadly.customstats.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.sosadly.customstats.affix.Affix;
import org.sosadly.customstats.affix.AffixType;
import org.sosadly.customstats.CustomStats;
import org.sosadly.customstats.manager.LocaleManager;
import org.sosadly.customstats.util.ItemUtil;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ApplyAffixItemListener implements Listener {
    private final CustomStats plugin;
    private final LocaleManager localeManager;
    public ApplyAffixItemListener(CustomStats plugin) {
        this.plugin = plugin;
        this.localeManager = plugin.getLocaleManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player player)) return;
        ItemStack cursorItem = event.getCursor();
        ItemStack targetItem = event.getCurrentItem();

        if (!isAffixApplicationClick(event.getClick()) || !ItemUtil.isAffixScroll(cursorItem) || targetItem == null || targetItem.getType().isAir()) {
            return;
        }
        event.setCancelled(true);
        if (!player.hasPermission("customstats.use_affix_scroll")) {
            player.sendMessage(localeManager.getMessage("error.no_permission"));
            return;
        }
        AffixType allowedType = getAllowedAffixType(targetItem);
        if (allowedType == AffixType.NONE) {
            player.sendMessage(localeManager.getMessage("item.scroll_use.invalid_item"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        FileConfiguration config = plugin.getConfig();
        ItemMeta meta = targetItem.getItemMeta();
        Set<Affix> existingAffixes = getExistingAffixes(meta);
        if (existingAffixes.size() >= config.getInt("affix-settings.max-affixes", 2)) {
            player.sendMessage(localeManager.getMessage("item.scroll_use.max_affixes"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        List<Affix> availableAffixes = getAvailableAffixes(allowedType);
        availableAffixes.removeAll(existingAffixes);
        if (availableAffixes.isEmpty()) {
            player.sendMessage(localeManager.getMessage("item.scroll_use.no_more_affixes"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        Collections.shuffle(availableAffixes);
        Affix affixToAdd = availableAffixes.get(0);
        double minValue = config.getDouble(affixToAdd.getConfigPath() + ".min", 1.0);
        double maxValue = config.getDouble(affixToAdd.getConfigPath() + ".max", 5.0);
        double randomValue = Math.min(maxValue, Math.max(minValue, ThreadLocalRandom.current().nextDouble(minValue, maxValue)));
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(affixToAdd.createLore(randomValue));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(affixToAdd.getKey(), PersistentDataType.DOUBLE, randomValue);
        targetItem.setItemMeta(meta);
        event.setCurrentItem(targetItem);

        ItemStack updatedCursor = cursorItem.clone();
        updatedCursor.subtract(1);
        if (updatedCursor.getAmount() <= 0) {
            event.getView().setCursor(null);
        } else {
            event.getView().setCursor(updatedCursor);
        }

        player.sendMessage(localeManager.getMessage("item.scroll_use.success"));
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.2f);
        player.updateInventory();
    }

    private boolean isAffixApplicationClick(ClickType clickType) {
        return clickType == ClickType.RIGHT
                || clickType == ClickType.LEFT
                || clickType == ClickType.SHIFT_RIGHT
                || clickType == ClickType.SHIFT_LEFT
                || clickType == ClickType.MIDDLE;
    }

    private AffixType getAllowedAffixType(ItemStack item) {
        if (item == null) return AffixType.NONE;
        FileConfiguration config = plugin.getConfig();
        String itemKey = item.getType().getKey().toString();
        
        List<String> blacklist = config.getStringList("custom-affix-items.blacklist");
        if (blacklist.contains(itemKey)) {
            return AffixType.NONE;
        }

        ConfigurationSection overridesSection = config.getConfigurationSection("custom-affix-items.overrides");
        if (overridesSection == null) {
            overridesSection = config.getConfigurationSection("custom-affix-items");
        }
        if (overridesSection != null && overridesSection.contains(itemKey)) {
            String type = overridesSection.getString(itemKey, "").toLowerCase();
            if (type.equals("damage")) return AffixType.DAMAGE;
            if (type.equals("resist")) return AffixType.RESIST;
        }
        if (item.getType().name().endsWith("_SWORD")) return AffixType.DAMAGE;
        String name = item.getType().name();
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) {
            return AffixType.RESIST;
        }
        return AffixType.NONE;
    }
    
    private Set<Affix> getExistingAffixes(ItemMeta meta) {
        Set<Affix> existing = new HashSet<>();
        if (meta == null) return existing;
        for (Affix affix : Affix.values()) {
            if (meta.getPersistentDataContainer().has(affix.getKey(), PersistentDataType.DOUBLE)) existing.add(affix);
        }
        return existing;
    }
    
    private List<Affix> getAvailableAffixes(AffixType type) {
        List<Affix> affixes = new ArrayList<>(List.of(Affix.values()));
        if (type == AffixType.DAMAGE) {
            affixes.removeIf(a -> a.name().endsWith("_RESIST"));
        } else if (type == AffixType.RESIST) {
            affixes.removeIf(a -> !a.name().endsWith("_RESIST"));
        }
        return affixes;
    }
}