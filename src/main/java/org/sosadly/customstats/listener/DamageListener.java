package org.sosadly.customstats.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.sosadly.customstats.affix.Affix;
import org.sosadly.customstats.affix.AffixKeys;

import java.util.concurrent.ThreadLocalRandom;

public class DamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        ItemStack itemInHand = damager.getInventory().getItemInMainHand();
        if (itemInHand.getType().isAir() || !itemInHand.hasItemMeta()) {
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (pdc.has(AffixKeys.CRIT_CHANCE, PersistentDataType.DOUBLE)) {
            double critChance = pdc.get(AffixKeys.CRIT_CHANCE, PersistentDataType.DOUBLE);
            if (ThreadLocalRandom.current().nextDouble(0, 100) < critChance) {
                event.setDamage(event.getDamage() * 1.5);
            }
        }

        double totalBonusDamage = 0;
        for (Affix affix : Affix.values()) {
            if (affix == Affix.CRIT_CHANCE) continue;

            if (pdc.has(affix.getKey(), PersistentDataType.DOUBLE)) {
                double affixValue = pdc.get(affix.getKey(), PersistentDataType.DOUBLE);
                totalBonusDamage += applyAffixEffect(event, affix, affixValue, damager, (LivingEntity) event.getEntity());
            }
        }

        if (totalBonusDamage > 0) {
            event.setDamage(event.getDamage() + totalBonusDamage);
        }
    }

    private double applyAffixEffect(EntityDamageByEntityEvent event, Affix affix, double value, Player damager, LivingEntity target) {
        switch (affix) {
            case LIGHTNING_DAMAGE:
                double lightningResist = getTotalResistance(target, AffixKeys.LIGHTNING_RESIST);
                double finalLightningDamage = Math.max(0, value - lightningResist);
                target.getWorld().strikeLightningEffect(target.getLocation());
                return finalLightningDamage;

            case FIRE_DAMAGE:
                double fireResist = getTotalResistance(target, AffixKeys.FIRE_RESIST);
                double finalFireDamage = Math.max(0, value - fireResist);
                target.setFireTicks(40);
                return finalFireDamage;

            case WATER_DAMAGE:
                double waterResist = getTotalResistance(target, AffixKeys.WATER_RESIST);
                return Math.max(0, value - waterResist);

            case POISON_DAMAGE:
                double poisonResist = getTotalResistance(target, AffixKeys.POISON_RESIST);
                double finalPoisonDamage = Math.max(0, value - poisonResist);
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                return finalPoisonDamage;

            case TRUE_DAMAGE:
                target.setHealth(Math.max(0, target.getHealth() - value));
                return 0;

            case LIFE_STEAL:
                double healAmount = value;
                double maxHealth = damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                damager.setHealth(Math.min(maxHealth, damager.getHealth() + healAmount));
                return 0;

            default:
                return 0;
        }
    }

    /**
     * Розраховує загальний опір для заданого типу захисту на броні цілі.
     */
    private double getTotalResistance(LivingEntity target, NamespacedKey resistKey) {
        double totalResist = 0;
        if (target.getEquipment() == null) {
            return 0;
        }
        for (ItemStack armorPiece : target.getEquipment().getArmorContents()) {
            if (armorPiece != null && armorPiece.hasItemMeta()) {
                PersistentDataContainer pdc = armorPiece.getItemMeta().getPersistentDataContainer();
                if (pdc.has(resistKey, PersistentDataType.DOUBLE)) {
                    totalResist += pdc.get(resistKey, PersistentDataType.DOUBLE);
                }
            }
        }
        return totalResist;
    }
}