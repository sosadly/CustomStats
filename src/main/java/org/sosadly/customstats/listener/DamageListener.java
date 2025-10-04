package org.sosadly.customstats.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.sosadly.customstats.CustomStats;
import org.sosadly.customstats.affix.ActiveAffix;
import org.sosadly.customstats.affix.Affix;
import org.sosadly.customstats.affix.AffixKeys;
import org.sosadly.customstats.affix.AffixType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class DamageListener implements Listener {

    private static final String DIVINE_METADATA = "customstats_divine_shield";

    private final CustomStats plugin;

    public DamageListener(CustomStats plugin) {
        this.plugin = plugin;
    }

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

        double activeBonusDamage = handleWeaponActiveAffixes(event, damager, (LivingEntity) event.getEntity(), pdc);

        double totalExtra = totalBonusDamage + activeBonusDamage;
        if (totalExtra > 0) {
            event.setDamage(event.getDamage() + totalExtra);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityHurt(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        handleDivineShieldProtection(event, player);
        handleArmorActiveAffixes(event, player);
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

            case BLEED_DAMAGE:
                double bleedResist = getTotalResistance(target, AffixKeys.BLEED_RESIST);
                double finalBleedDamage = Math.max(0, value - bleedResist);
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
                return finalBleedDamage;

            case ARCANE_DAMAGE:
                double arcaneResist = getTotalResistance(target, AffixKeys.ARCANE_RESIST);
                double finalArcaneDamage = Math.max(0, value - arcaneResist);
                target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 25, 0.3, 0.6, 0.3, 0.02);
                return finalArcaneDamage;

            case NATURE_DAMAGE:
                double natureResist = getTotalResistance(target, AffixKeys.NATURE_RESIST);
                double finalNatureDamage = Math.max(0, value - natureResist);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
                target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, target.getLocation().add(0, 1, 0), 10, 0.4, 0.4, 0.4, 0.05);
                return finalNatureDamage;

            default:
                return 0;
        }
    }

    private double handleWeaponActiveAffixes(EntityDamageByEntityEvent event, Player damager, LivingEntity target, PersistentDataContainer weaponPdc) {
        List<ActiveAffix> weaponAffixes = ActiveAffix.forType(AffixType.DAMAGE);
        FileConfiguration config = plugin.getConfig();
        for (ActiveAffix active : weaponAffixes) {
            if (!weaponPdc.has(active.getChanceKey(), PersistentDataType.DOUBLE)) {
                continue;
            }

            double chance = weaponPdc.get(active.getChanceKey(), PersistentDataType.DOUBLE);
            if (ThreadLocalRandom.current().nextDouble(0, 100) >= chance) {
                continue;
            }

            String path = active.getConfigPath();
            switch (active) {
                case EARTH_SHOCK -> {
                    double bonusDamage = config.getDouble(path + ".bonus-damage", 4.0);
                    double splashDamage = config.getDouble(path + ".splash-damage", Math.max(1.0, bonusDamage / 2));
                    double radius = config.getDouble(path + ".radius", 3.0);
                    double knockback = config.getDouble(path + ".knockback", 0.6);
                    triggerEarthShock(damager, target, splashDamage, radius, knockback);
                    return bonusDamage;
                }
                case ARCANE_NOVA -> {
                    double novaDamage = getDoubleOrDefault(weaponPdc, AffixKeys.ARCANE_NOVA_DAMAGE, config.getDouble(path + ".damage-min", 5.0));
                    double radius = config.getDouble(path + ".radius", 3.5);
                    double splashMultiplier = config.getDouble(path + ".splash-multiplier", 0.6);
                    triggerArcaneNova(damager, target, novaDamage, radius, splashMultiplier);
                    return novaDamage;
                }
                case SHADOW_STEP -> {
                    double bonusDamage = getDoubleOrDefault(weaponPdc, AffixKeys.SHADOW_STEP_BONUS, config.getDouble(path + ".bonus-damage-min", 4.0));
                    double behindDistance = config.getDouble(path + ".behind-distance", 1.6);
                    int slowDuration = config.getInt(path + ".target-slow-duration", 60);
                    int weaknessDuration = config.getInt(path + ".target-weakness-duration", 40);
                    performShadowStep(damager, target, behindDistance, slowDuration, weaknessDuration);
                    return bonusDamage;
                }
                case EXPLOSIVE_STRIKE -> {
                    double power = config.getDouble(path + ".explosion-power", 2.0);
                    boolean breakBlocks = config.getBoolean(path + ".break-blocks", false);
                    boolean setFire = config.getBoolean(path + ".set-fire", false);
                    target.getWorld().createExplosion(target.getLocation(), (float) power, setFire, breakBlocks, damager);
                }
                case FROST_ORB -> {
                    double extraDamage = getDoubleOrDefault(weaponPdc, AffixKeys.FROST_ORB_DAMAGE, config.getDouble(path + ".damage-min", 3.0));
                    int slowDuration = config.getInt(path + ".slow-duration", 60);
                    int slowAmplifier = config.getInt(path + ".slow-amplifier", 1);
                    int structureDuration = config.getInt(path + ".structure-duration", 60);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowDuration, slowAmplifier, false, true, true));
                    spawnFrostOrbParticles(target.getLocation());
                    spawnFrostOrbStructure(target.getLocation(), structureDuration);
                    return extraDamage;
                }
                default -> {
                    // no-op
                }
            }

            return 0;
        }
        return 0;
    }

    private void handleArmorActiveAffixes(EntityDamageEvent event, Player player) {
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        if (armorContents == null || armorContents.length == 0) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        for (ItemStack armorPiece : armorContents) {
            if (armorPiece == null || !armorPiece.hasItemMeta()) {
                continue;
            }

            PersistentDataContainer pdc = armorPiece.getItemMeta().getPersistentDataContainer();
            for (ActiveAffix active : ActiveAffix.forType(AffixType.RESIST)) {
                if (!pdc.has(active.getChanceKey(), PersistentDataType.DOUBLE)) {
                    continue;
                }

                double chance = pdc.get(active.getChanceKey(), PersistentDataType.DOUBLE);
                if (ThreadLocalRandom.current().nextDouble(0, 100) >= chance) {
                    continue;
                }

                String path = active.getConfigPath();
                switch (active) {
                    case RENEWAL_BLESSING -> {
                        int duration = config.getInt(path + ".duration", 200);
                        int amplifier = config.getInt(path + ".amplifier", 0);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, amplifier, false, true, true));
                    }
                    case ICE_COCOON -> {
                        int duration = config.getInt(path + ".duration", 60);
                        double heal = getDoubleOrDefault(pdc, AffixKeys.ICE_COCOON_HEAL, config.getDouble(path + ".heal-amount", 10.0));
                        String materialName = config.getString(path + ".shell-material", "minecraft:ice");
                        Material shellMaterial = Material.matchMaterial(materialName, true);
                        if (shellMaterial == null) {
                            shellMaterial = Material.ICE;
                        }
                        double radius = config.getDouble(path + ".shell-radius", 1.5);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 4, false, true, true));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 2, false, true, true));
                        double maxHealth = Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).map(attr -> attr.getValue()).orElse(20.0);
                        player.setHealth(Math.min(maxHealth, player.getHealth() + heal));
                        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 30, 0.6, 0.8, 0.6, 0.1);
                        player.setNoDamageTicks(Math.max(player.getNoDamageTicks(), duration));
                        spawnIceCocoon(player, shellMaterial, radius, duration);
                    }
                    case DIVINE_SHIELD -> {
                        double thresholdPercent = config.getDouble(path + ".health-threshold-percent", 10.0);
                        double maxHealth = Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).map(attr -> attr.getValue()).orElse(20.0);
                        if ((player.getHealth() / maxHealth) * 100 > thresholdPercent) {
                            continue;
                        }

                        int duration = config.getInt(path + ".duration", 60);
                        int particleCount = config.getInt(path + ".particle-count", 40);
                        long expireAt = System.currentTimeMillis() + duration * 50L;
                        player.setMetadata(DIVINE_METADATA, new FixedMetadataValue(plugin, expireAt));
                        spawnDivineParticles(player, particleCount);
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            if (!player.hasMetadata(DIVINE_METADATA)) {
                                return;
                            }
                            long stored = player.getMetadata(DIVINE_METADATA).stream()
                                    .filter(value -> value.getOwningPlugin() == plugin)
                                    .map(MetadataValue::asLong)
                                    .findFirst()
                                    .orElse(0L);
                            if (stored <= expireAt) {
                                player.removeMetadata(DIVINE_METADATA, plugin);
                            }
                        }, duration);
                    }
                    case THORNS_AURA -> {
                        double reflectPercent = config.getDouble(path + ".reflect-percent", 0.4);
                        int resistanceDuration = config.getInt(path + ".resistance-duration", 60);
                        int thornsDuration = config.getInt(path + ".thorns-duration", 100);
                        spawnThornsAura(player, reflectPercent, resistanceDuration, thornsDuration, event);
                    }
                    default -> {
                        // no-op
                    }
                }

                return;
            }
        }
    }

    private void handleDivineShieldProtection(EntityDamageEvent event, Player player) {
        if (!player.hasMetadata(DIVINE_METADATA)) {
            return;
        }

        long now = System.currentTimeMillis();
        List<MetadataValue> metadataValues = player.getMetadata(DIVINE_METADATA);
        long expiry = metadataValues.stream()
                .filter(value -> value.getOwningPlugin() == plugin)
                .map(MetadataValue::asLong)
                .findFirst()
                .orElse(0L);

        if (expiry <= now) {
            player.removeMetadata(DIVINE_METADATA, plugin);
            return;
        }

        double finalDamage = event.getFinalDamage();
        if (player.getHealth() - finalDamage <= 1.0) {
            double allowedDamage = Math.max(0, player.getHealth() - 1.0);
            event.setDamage(Math.min(event.getDamage(), allowedDamage));
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

    private double getDoubleOrDefault(PersistentDataContainer container, NamespacedKey key, double fallback) {
        if (container.has(key, PersistentDataType.DOUBLE)) {
            Double value = container.get(key, PersistentDataType.DOUBLE);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    private void triggerEarthShock(Player damager, LivingEntity target, double splashDamage, double radius, double knockback) {
        World world = target.getWorld();
        if (world == null) {
            return;
        }

        Location center = target.getLocation().clone();
    world.spawnParticle(Particle.BLOCK, center, 50, radius / 2.0, 0.4, radius / 2.0, Material.DEEPSLATE.createBlockData());
        world.spawnParticle(Particle.CRIT, center.clone().add(0, 0.4, 0), 20, radius / 3.0, 0.2, radius / 3.0, 0.05);
    world.playSound(center, Sound.BLOCK_STONE_BREAK, 0.8f, 0.8f);

        Vector upward = target.getVelocity().clone();
        upward.setY(Math.max(upward.getY(), 0.35));
        target.setVelocity(upward);

        for (Entity entity : world.getNearbyEntities(center, radius, 1.2, radius)) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (living.equals(target)) {
                continue;
            }
            if (damager != null && living.equals(damager)) {
                continue;
            }

            living.setNoDamageTicks(0);
            if (damager != null) {
                living.damage(splashDamage, damager);
            } else {
                living.damage(splashDamage);
            }

            Vector push = living.getLocation().toVector().subtract(center.toVector());
            push.setY(0);
            if (push.lengthSquared() > 0.0001) {
                push.normalize().multiply(knockback);
            }
            push.setY(Math.max(push.getY(), 0.25));
            living.setVelocity(living.getVelocity().add(push));
        }
    }

    private void triggerArcaneNova(Player damager, LivingEntity target, double damage, double radius, double splashMultiplier) {
        World world = target.getWorld();
        if (world == null) {
            return;
        }

        Location center = target.getLocation().clone();
        world.spawnParticle(Particle.DRAGON_BREATH, center.clone().add(0, 0.5, 0), 40, radius / 2.0, 0.5, radius / 2.0, 0.02);
        world.spawnParticle(Particle.END_ROD, center.clone().add(0, 1.0, 0), 30, radius / 3.0, 0.6, radius / 3.0, 0.02);
        world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.8f, 1.2f);

        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            living.setNoDamageTicks(0);
            double finalDamage = living.equals(target) ? damage : damage * splashMultiplier;
            if (damager != null) {
                living.damage(finalDamage, damager);
            } else {
                living.damage(finalDamage);
            }
        }
    }

    private void performShadowStep(Player damager, LivingEntity target, double behindDistance, int slowDuration, int weaknessDuration) {
        if (damager == null || target == null) {
            return;
        }

        Location targetLocation = target.getLocation();
        Vector direction = targetLocation.getDirection().normalize();
        Location behind = targetLocation.toVector().subtract(direction.multiply(behindDistance)).toLocation(target.getWorld());
        behind.setY(targetLocation.getY());

        damager.teleport(behind);
    damager.getWorld().spawnParticle(Particle.SMOKE, damager.getLocation(), 20, 0.4, 0.4, 0.4, 0.01);
        damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.4f);

        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowDuration, 1, false, true, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weaknessDuration, 0, false, true, true));
    }

    private void spawnThornsAura(Player player, double reflectPercent, int resistanceDuration, int thornsDuration, EntityDamageEvent event) {
    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, resistanceDuration, 1, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, thornsDuration, 1, false, true, true));
    player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 30, 0.6, 0.8, 0.6, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.7f, 1.0f);

        if (event instanceof EntityDamageByEntityEvent damageEvent && damageEvent.getDamager() instanceof LivingEntity attacker) {
            double reflected = damageEvent.getFinalDamage() * Math.max(0.0, reflectPercent);
            if (reflected > 0) {
                attacker.damage(reflected, player);
            }
        }
    }

    private void spawnFrostOrbParticles(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        Location center = location.clone();
        world.spawnParticle(Particle.SNOWFLAKE, center.add(0, 1, 0), 40, 0.7, 0.7, 0.7, 0.05);
        world.spawnParticle(Particle.CLOUD, location.clone(), 20, 0.4, 0.4, 0.4, 0.01);
    }

    private void spawnFrostOrbStructure(Location center, int durationTicks) {
        World world = center.getWorld();
        if (world == null || durationTicks <= 0) {
            return;
        }

        List<BlockState> replacedBlocks = new ArrayList<>();
        Location base = center.clone().add(0, 0.2, 0);
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    double distance = Math.sqrt(x * x + (y - 1) * (y - 1) + z * z);
                    if (distance > 1.5) {
                        continue;
                    }
                    Block block = base.clone().add(x, y, z).getBlock();
                    if (!block.isPassable() && block.getType() != Material.AIR) {
                        continue;
                    }
                    BlockState previousState = block.getState();
                    block.setType(Material.SNOW_BLOCK, false);
                    replacedBlocks.add(previousState);
                }
            }
        }

        if (!replacedBlocks.isEmpty()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> replacedBlocks.forEach(state -> state.update(true, false)), durationTicks);
        }
    }

    private void spawnDivineParticles(Player player, int count) {
        Location base = player.getLocation();
        World world = base.getWorld();
        if (world == null) {
            return;
        }
        world.spawnParticle(Particle.END_ROD, base.clone().add(0, 1.2, 0), count, 0.5, 0.8, 0.5, 0.02);
        world.spawnParticle(Particle.HEART, base.clone().add(0, 1.5, 0), 5, 0.2, 0.4, 0.2, 0.01);
    }

    private void spawnIceCocoon(Player player, Material material, double radius, int durationTicks) {
        World world = player.getWorld();
        if (world == null || durationTicks <= 0) {
            return;
        }

        Location base = player.getLocation();
        List<BlockState> replacedBlocks = new ArrayList<>();
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -2; z <= 2; z++) {
                    double distance = Math.sqrt(x * x + (y - 1.5) * (y - 1.5) + z * z);
                    if (distance > radius) {
                        continue;
                    }
                    Block block = base.clone().add(x, y, z).getBlock();
                    if (!block.isPassable() && block.getType() != Material.AIR) {
                        continue;
                    }
                    BlockState previousState = block.getState();
                    block.setType(material, false);
                    replacedBlocks.add(previousState);
                }
            }
        }

        if (!replacedBlocks.isEmpty()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> replacedBlocks.forEach(state -> state.update(true, false)), durationTicks);
        }
    }
}