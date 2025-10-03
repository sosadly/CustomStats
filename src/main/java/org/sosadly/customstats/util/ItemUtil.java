package org.sosadly.customstats.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.sosadly.customstats.CustomStats;
import org.sosadly.customstats.manager.LocaleManager;

import java.util.stream.Collectors;

public final class ItemUtil {

    private static final NamespacedKey AFFIX_SCROLL_KEY = new NamespacedKey(CustomStats.getPlugin(CustomStats.class), "affix_scroll");
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private ItemUtil() {}

    /**
     * Створює предмет "Скрол Аффіксу" на основі конфігурації та локалізації.
     */
    public static ItemStack createAffixScroll(int amount) {
        CustomStats plugin = CustomStats.getPlugin(CustomStats.class);
        LocaleManager localeManager = plugin.getLocaleManager();

        Material material = Material.matchMaterial(plugin.getConfig().getString("affix-scroll.item-id", "minecraft:paper"));
        if (material == null) {
            material = Material.PAPER;
        }

        ItemStack scroll = new ItemStack(material, amount);
        ItemMeta meta = scroll.getItemMeta();

        // Беремо назву та лор з мовного файлу
        meta.displayName(miniMessage.deserialize(localeManager.getRawString("item.scroll.name", "§6Scroll of Affix")));
        meta.lore(localeManager.getRawStringList("item.scroll.lore").stream()
                .map(miniMessage::deserialize)
                .collect(Collectors.toList()));

        // Додаємо мітку в PDC для надійної ідентифікації
        meta.getPersistentDataContainer().set(AFFIX_SCROLL_KEY, PersistentDataType.BOOLEAN, true);

        scroll.setItemMeta(meta);
        return scroll;
    }

    /**
     * Перевіряє, чи є предмет "Скролом Аффіксу".
     */
    public static boolean isAffixScroll(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(AFFIX_SCROLL_KEY, PersistentDataType.BOOLEAN);
    }
}