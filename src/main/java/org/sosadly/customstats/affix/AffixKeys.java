package org.sosadly.customstats.affix;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.sosadly.customstats.CustomStats;

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
    public static final NamespacedKey BLEED_DAMAGE = new NamespacedKey(PLUGIN, "bleed_damage");
    public static final NamespacedKey ARCANE_DAMAGE = new NamespacedKey(PLUGIN, "arcane_damage");
    public static final NamespacedKey NATURE_DAMAGE = new NamespacedKey(PLUGIN, "nature_damage");


    // Active weapon affixes
    public static final NamespacedKey EARTH_SHOCK_CHANCE = new NamespacedKey(PLUGIN, "earth_shock_chance");
    public static final NamespacedKey ARCANE_NOVA_CHANCE = new NamespacedKey(PLUGIN, "arcane_nova_chance");
    public static final NamespacedKey ARCANE_NOVA_DAMAGE = new NamespacedKey(PLUGIN, "arcane_nova_damage");
    public static final NamespacedKey SHADOW_STEP_CHANCE = new NamespacedKey(PLUGIN, "shadow_step_chance");
    public static final NamespacedKey SHADOW_STEP_BONUS = new NamespacedKey(PLUGIN, "shadow_step_bonus");
    public static final NamespacedKey EXPLOSIVE_STRIKE_CHANCE = new NamespacedKey(PLUGIN, "explosive_strike_chance");
    public static final NamespacedKey FROST_ORB_CHANCE = new NamespacedKey(PLUGIN, "frost_orb_chance");
    public static final NamespacedKey FROST_ORB_DAMAGE = new NamespacedKey(PLUGIN, "frost_orb_damage");




    // Resists
    public static final NamespacedKey LIGHTNING_RESIST = new NamespacedKey(PLUGIN, "lightning_resist");
    public static final NamespacedKey FIRE_RESIST = new NamespacedKey(PLUGIN, "fire_resist");
    public static final NamespacedKey WATER_RESIST = new NamespacedKey(PLUGIN, "water_resist");
    public static final NamespacedKey POISON_RESIST = new NamespacedKey(PLUGIN, "poison_resist");
    public static final NamespacedKey BLEED_RESIST = new NamespacedKey(PLUGIN, "bleed_resist");
    public static final NamespacedKey ARCANE_RESIST = new NamespacedKey(PLUGIN, "arcane_resist");
    public static final NamespacedKey NATURE_RESIST = new NamespacedKey(PLUGIN, "nature_resist");

    // Active armor affixes
    public static final NamespacedKey RENEWAL_BLESSING_CHANCE = new NamespacedKey(PLUGIN, "renewal_blessing_chance");
    public static final NamespacedKey ICE_COCOON_CHANCE = new NamespacedKey(PLUGIN, "ice_cocoon_chance");
    public static final NamespacedKey ICE_COCOON_HEAL = new NamespacedKey(PLUGIN, "ice_cocoon_heal");
    public static final NamespacedKey DIVINE_SHIELD_CHANCE = new NamespacedKey(PLUGIN, "divine_shield_chance");
    public static final NamespacedKey THORNS_AURA_CHANCE = new NamespacedKey(PLUGIN, "thorns_aura_chance");
    

    private AffixKeys() {
    }
}