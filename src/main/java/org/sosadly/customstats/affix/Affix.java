package org.sosadly.customstats.affix;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.sosadly.customstats.util.LoreUtil;

import java.util.function.Function;

public enum Affix {

    LIGHTNING_DAMAGE(
            AffixKeys.LIGHTNING_DAMAGE,
            "lightning-damage",
            LoreUtil::createLightningDamageLore
    ),
    FIRE_DAMAGE(
            AffixKeys.FIRE_DAMAGE,
            "fire-damage",
            LoreUtil::createFireDamageLore
    ),
    WATER_DAMAGE(
            AffixKeys.WATER_DAMAGE,
            "water-damage",
            LoreUtil::createWaterDamageLore
    ),
    LIFE_STEAL(
            AffixKeys.LIFE_STEAL,
            "lifesteal",
            LoreUtil::createLifeStealLore
    ),
    POISON_DAMAGE(
            AffixKeys.POISON_DAMAGE,
            "poison-damage",
            LoreUtil::createPoisonDamageLore
    ),
    CRIT_CHANCE(
            AffixKeys.CRIT_CHANCE,
            "crit-chance",
            LoreUtil::createCritChanceLore
    ),
    TRUE_DAMAGE(
            AffixKeys.TRUE_DAMAGE,
            "true-damage",
            LoreUtil::createTrueDamageLore
    ),
    BLEED_DAMAGE(
            AffixKeys.BLEED_DAMAGE,
            "bleed-damage",
            LoreUtil::createBleedDamageLore
    ),
    ARCANE_DAMAGE(
            AffixKeys.ARCANE_DAMAGE,
            "arcane-damage",
            LoreUtil::createArcaneDamageLore
    ),
    NATURE_DAMAGE(
            AffixKeys.NATURE_DAMAGE,
            "nature-damage",
            LoreUtil::createNatureDamageLore
    ),



    // RESIStS
    LIGHTNING_RESIST(
            AffixKeys.LIGHTNING_RESIST,
            "lightning-resist",
            LoreUtil::createLightningResistLore
    ),
    FIRE_RESIST(
            AffixKeys.FIRE_RESIST,
            "fire-resist",
            LoreUtil::createFireResistLore
    ),
    WATER_RESIST(
            AffixKeys.WATER_RESIST,
            "water-resist",
            LoreUtil::createWaterResistLore
    ),
    POISON_RESIST(
            AffixKeys.POISON_RESIST,
            "poison-resist",
            LoreUtil::createPoisonResistLore
    ),
    BLEED_RESIST(
            AffixKeys.BLEED_RESIST,
            "bleed-resist",
            LoreUtil::createBleedResistLore
    ),
    ARCANE_RESIST(
            AffixKeys.ARCANE_RESIST,
            "arcane-resist",
            LoreUtil::createArcaneResistLore
    ),
    NATURE_RESIST(
            AffixKeys.NATURE_RESIST,
            "nature-resist",
            LoreUtil::createNatureResistLore
    );

    private final NamespacedKey key;
    private final String configPath;
    private final Function<Double, Component> loreCreator;

    Affix(NamespacedKey key, String configPath, Function<Double, Component> loreCreator) {
        this.key = key;
        this.configPath = configPath;
        this.loreCreator = loreCreator;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public String getConfigPath() {
        return "affixes." + configPath;
    }

    public Component createLore(double value) {
        return loreCreator.apply(value);
    }
}