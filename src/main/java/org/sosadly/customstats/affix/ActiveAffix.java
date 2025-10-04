package org.sosadly.customstats.affix;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.sosadly.customstats.util.LoreUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ActiveAffix {
    EARTH_SHOCK("active-affixes.weapon.earth_shock", AffixType.DAMAGE, AffixKeys.EARTH_SHOCK_CHANCE, null, "active.earth_shock"),
    ARCANE_NOVA("active-affixes.weapon.arcane_nova", AffixType.DAMAGE, AffixKeys.ARCANE_NOVA_CHANCE, AffixKeys.ARCANE_NOVA_DAMAGE, "active.arcane_nova"),
    SHADOW_STEP("active-affixes.weapon.shadow_step", AffixType.DAMAGE, AffixKeys.SHADOW_STEP_CHANCE, AffixKeys.SHADOW_STEP_BONUS, "active.shadow_step"),
    EXPLOSIVE_STRIKE("active-affixes.weapon.explosive_strike", AffixType.DAMAGE, AffixKeys.EXPLOSIVE_STRIKE_CHANCE, null, "active.explosive_strike"),
    FROST_ORB("active-affixes.weapon.frost_orb", AffixType.DAMAGE, AffixKeys.FROST_ORB_CHANCE, AffixKeys.FROST_ORB_DAMAGE, "active.frost_orb"),
    RENEWAL_BLESSING("active-affixes.armor.renewal_blessing", AffixType.RESIST, AffixKeys.RENEWAL_BLESSING_CHANCE, null, "active.renewal_blessing"),
    ICE_COCOON("active-affixes.armor.ice_cocoon", AffixType.RESIST, AffixKeys.ICE_COCOON_CHANCE, AffixKeys.ICE_COCOON_HEAL, "active.ice_cocoon"),
    DIVINE_SHIELD("active-affixes.armor.divine_shield", AffixType.RESIST, AffixKeys.DIVINE_SHIELD_CHANCE, null, "active.divine_shield"),
    THORNS_AURA("active-affixes.armor.thorns_aura", AffixType.RESIST, AffixKeys.THORNS_AURA_CHANCE, null, "active.thorns_aura");

    private final String configPath;
    private final AffixType appliesTo;
    private final NamespacedKey chanceKey;
    private final NamespacedKey extraValueKey;
    private final String loreKey;

    ActiveAffix(String configPath, AffixType appliesTo, NamespacedKey chanceKey, NamespacedKey extraValueKey, String loreKey) {
        this.configPath = configPath;
        this.appliesTo = appliesTo;
        this.chanceKey = chanceKey;
        this.extraValueKey = extraValueKey;
        this.loreKey = loreKey;
    }

    public String getConfigPath() {
        return configPath;
    }

    public AffixType getAppliesTo() {
        return appliesTo;
    }

    public NamespacedKey getChanceKey() {
        return chanceKey;
    }

    public Optional<NamespacedKey> getExtraValueKey() {
        return Optional.ofNullable(extraValueKey);
    }

    public Component createLore(double chance, Double extraValue) {
        return LoreUtil.createActiveLore(loreKey, chance, extraValue);
    }

    public static List<ActiveAffix> forType(AffixType type) {
        return EnumSet.allOf(ActiveAffix.class).stream()
                .filter(affix -> affix.appliesTo == type)
                .collect(Collectors.toList());
    }
}
