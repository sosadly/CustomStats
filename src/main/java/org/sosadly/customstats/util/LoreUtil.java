package org.sosadly.customstats.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.sosadly.customstats.CustomStats;
import org.sosadly.customstats.manager.LocaleManager;

import java.text.DecimalFormat;

public final class LoreUtil {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private LoreUtil() {}

    private static Component createLore(String localeKey, double value) {
        LocaleManager localeManager = CustomStats.getPlugin(CustomStats.class).getLocaleManager();
        String format = localeManager.getRawString("lore." + localeKey, "<red>Missing lore: " + localeKey);
        return miniMessage.deserialize(format, Placeholder.unparsed("value", DECIMAL_FORMAT.format(value)));
    }

    public static Component createActiveLore(String localeKey, double chance, Double extraValue) {
        LocaleManager localeManager = CustomStats.getPlugin(CustomStats.class).getLocaleManager();
        String format = localeManager.getRawString("lore." + localeKey, "<red>Missing lore: " + localeKey);

        TagResolver.Builder builder = TagResolver.builder()
                .resolver(Placeholder.unparsed("chance", DECIMAL_FORMAT.format(chance)));

        if (extraValue != null) {
            builder.resolver(Placeholder.unparsed("value", DECIMAL_FORMAT.format(extraValue)));
        }

        return miniMessage.deserialize(format, builder.build());
    }

    // Всі методи тепер викликають універсальний `createLore`
    public static Component createLightningDamageLore(double damage) { return createLore("lightning_damage", damage); }
    public static Component createFireDamageLore(double damage) { return createLore("fire_damage", damage); }
    public static Component createWaterDamageLore(double damage) { return createLore("water_damage", damage); }
    public static Component createLifeStealLore(double value) { return createLore("life_steal", value); }
    public static Component createPoisonDamageLore(double value) { return createLore("poison_damage", value); }
    public static Component createCritChanceLore(double value) { return createLore("crit_chance", value); }
    public static Component createTrueDamageLore(double value) { return createLore("true_damage", value); }
    public static Component createBleedDamageLore(double value) { return createLore("bleed_damage", value); }
    public static Component createArcaneDamageLore(double value) { return createLore("arcane_damage", value); }
    public static Component createNatureDamageLore(double value) { return createLore("nature_damage", value); }
    public static Component createLightningResistLore(double value) { return createLore("lightning_resist", value); }
    public static Component createFireResistLore(double value) { return createLore("fire_resist", value); }
    public static Component createWaterResistLore(double value) { return createLore("water_resist", value); }
    public static Component createPoisonResistLore(double value) { return createLore("poison_resist", value); }
    public static Component createBleedResistLore(double value) { return createLore("bleed_resist", value); }
    public static Component createArcaneResistLore(double value) { return createLore("arcane_resist", value); }
    public static Component createNatureResistLore(double value) { return createLore("nature_resist", value); }
}