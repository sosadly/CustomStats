package org.sosadly.affix;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.sosadly.CustomStats;

public final class AffixKeys {

    private static final Plugin PLUGIN = CustomStats.getPlugin(CustomStats.class);
    
    // Damage
    public static final NamespacedKey LIGHTNING_DAMAGE = new NamespacedKey(PLUGIN, "lightning_damage");
    public static final NamespacedKey FIRE_DAMAGE = new NamespacedKey(PLUGIN, "fire_damage");
    public static final NamespacedKey WATER_DAMAGE = new NamespacedKey(PLUGIN, "water_damage");
    public static final NamespacedKey LIFE_STEAL = new NamespacedKey(PLUGIN, "life_steal");
    public static final NamespacedKey POISON_DAMAGE = new NamespacedKey(PLUGIN, "poison_damage");
    public static final NamespacedKey CRIT_CHANCE = new NamespacedKey(PLUGIN, "crit_chance");
    public static final NamespacedKey TRUE_DAMAGE = new NamespacedKey(PLUGIN, "true_damage");




    // Resists
    public static final NamespacedKey LIGHTNING_RESIST = new NamespacedKey(PLUGIN, "lightning_resist");
    public static final NamespacedKey FIRE_RESIST = new NamespacedKey(PLUGIN, "fire_resist");
    public static final NamespacedKey WATER_RESIST = new NamespacedKey(PLUGIN, "water_resist");
    public static final NamespacedKey POISON_RESIST = new NamespacedKey(PLUGIN, "poison_resist");
    

    private AffixKeys() {
    }
}