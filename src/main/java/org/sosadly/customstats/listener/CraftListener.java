package org.sosadly.customstats.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.sosadly.customstats.affix.Affix;
import org.sosadly.customstats.affix.ActiveAffix;
import org.sosadly.customstats.affix.AffixType;
import org.sosadly.customstats.CustomStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CraftListener implements Listener {

    private final CustomStats plugin;
    private final Map<String, ItemStack> rollCache = new ConcurrentHashMap<>();
    public CraftListener(CustomStats plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null) return;

        Player player = (Player) event.getView().getPlayer();
        ItemStack result = recipe.getResult();
        
        if (getAllowedAffixType(result) == AffixType.NONE) {
            return;
        }
        
        String cacheKey = buildRecipeKey(player, recipe);
        if (cacheKey == null) return;

        if (event.getInventory().getResult() == null) {
            rollCache.remove(cacheKey);
            return;
        }

        if (rollCache.containsKey(cacheKey)) {
            event.getInventory().setResult(rollCache.get(cacheKey).clone());
            return;
        }

        ItemStack customizedItem = generateAffixes(result);
        rollCache.put(cacheKey, customizedItem);
        event.getInventory().setResult(customizedItem.clone());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null) return;

        Player player = (Player) event.getWhoClicked();
        String cacheKey = buildRecipeKey(player, recipe);
        ItemStack baseItem = recipe.getResult();
        AffixType affixType = getAllowedAffixType(baseItem);

        if (affixType == AffixType.NONE) {
            if (cacheKey != null) {
                rollCache.remove(cacheKey);
            }
            return;
        }

        if (event.isShiftClick()) {
            handleShiftClickCraft(event, player, cacheKey, baseItem);
            return;
        }

        // Якщо кеш містить предмет, це перший крафт. Використовуємо його.
        if (cacheKey != null && rollCache.containsKey(cacheKey)) {
            event.setCurrentItem(rollCache.get(cacheKey).clone());
            // Негайно видаляємо з кешу, щоб наступний предмет був унікальним.
            rollCache.remove(cacheKey);
        } else {
            // Якщо кеш порожній (це 2-й і наступні предмети при Shift-кліку), генеруємо новий.
            event.setCurrentItem(generateAffixes(baseItem));
        }
    }

    private void handleShiftClickCraft(CraftItemEvent event, Player player, String cacheKey, ItemStack baseItem) {
        CraftingInventory craftingInventory = event.getInventory();
        int crafts = calculateMaxCrafts(craftingInventory);
        if (crafts <= 0) {
            if (cacheKey != null) {
                rollCache.remove(cacheKey);
            }
            return;
        }

        event.setCancelled(true);

        int resultAmount = Math.max(1, baseItem.getAmount());
        for (int i = 0; i < crafts; i++) {
            ItemStack generated = generateAffixes(baseItem);
            generated.setAmount(resultAmount);
            giveItem(player, generated);
        }

        consumeIngredients(craftingInventory, crafts);
        craftingInventory.setResult(null);

        if (cacheKey != null) {
            rollCache.remove(cacheKey);
        }

        Bukkit.getScheduler().runTask(plugin, player::updateInventory);
    }

    private void giveItem(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(remaining -> player.getWorld().dropItem(player.getLocation(), remaining));
        }
    }

    private int calculateMaxCrafts(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();
        int maxCrafts = Integer.MAX_VALUE;
        boolean foundIngredient = false;

        for (ItemStack item : matrix) {
            if (item == null || item.getType() == Material.AIR) continue;
            foundIngredient = true;
            maxCrafts = Math.min(maxCrafts, item.getAmount());
        }

        return foundIngredient ? maxCrafts : 0;
    }

    private void consumeIngredients(CraftingInventory inventory, int crafts) {
        ItemStack[] matrix = inventory.getMatrix();

        for (int i = 0; i < matrix.length; i++) {
            ItemStack item = matrix[i];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            int newAmount = item.getAmount() - crafts;
            if (newAmount > 0) {
                item.setAmount(newAmount);
                matrix[i] = item;
            } else {
                matrix[i] = null;
            }
        }

        inventory.setMatrix(matrix);
    }

    private ItemStack generateAffixes(ItemStack originalItem) {
        AffixType allowedType = getAllowedAffixType(originalItem);
        if (allowedType == AffixType.NONE) {
            return originalItem;
        }
        
        FileConfiguration config = plugin.getConfig();
        int minAffixes = config.getInt("affix-settings.min-affixes", 1);
        int maxAffixes = config.getInt("affix-settings.max-affixes", 2);
        int affixesToApply = (minAffixes >= maxAffixes) ? minAffixes : ThreadLocalRandom.current().nextInt(minAffixes, maxAffixes + 1);

        if (affixesToApply <= 0) {
            return originalItem;
        }

        ItemStack customizedItem = originalItem.clone();
        ItemMeta meta = customizedItem.getItemMeta();
        List<Component> lore = new ArrayList<>();
        if (meta == null) {
            return customizedItem;
        }

        if (meta.hasLore()) {
            lore.addAll(meta.lore());
        }

        boolean hasActiveAffix = hasActiveAffix(meta);
        ActiveAffix selectedActive = null;
        if (!hasActiveAffix) {
            List<ActiveAffix> possibleActiveAffixes = new ArrayList<>(ActiveAffix.forType(allowedType));
            if (!possibleActiveAffixes.isEmpty()) {
                double rollChance = config.getDouble("affix-settings.active-affix-roll-chance", 5.0);
                if (rollChance > 0 && ThreadLocalRandom.current().nextDouble(0, 100) < rollChance) {
                    Collections.shuffle(possibleActiveAffixes);
                    selectedActive = possibleActiveAffixes.get(0);
                }
            }
        }

        if (selectedActive != null && meta != null) {
            applyActiveAffix(meta, lore, selectedActive, config);
            affixesToApply = Math.max(0, affixesToApply - 1);
        }

        List<Affix> availableAffixes = getAvailableAffixes(allowedType);
        
        Collections.shuffle(availableAffixes);
        int finalAffixCount = Math.min(affixesToApply, availableAffixes.size());

        for (int i = 0; i < finalAffixCount; i++) {
            Affix affix = availableAffixes.get(i);
            double minValue = config.getDouble(affix.getConfigPath() + ".min", 1.0);
            double maxValue = config.getDouble(affix.getConfigPath() + ".max", 5.0);
            double randomValue = Math.min(maxValue, Math.max(minValue, ThreadLocalRandom.current().nextDouble(minValue, maxValue)));
            
            meta.getPersistentDataContainer().set(affix.getKey(), PersistentDataType.DOUBLE, randomValue);
            lore.add(affix.createLore(randomValue));
        }

        meta.lore(lore);
        customizedItem.setItemMeta(meta);
        return customizedItem;
    }

    private boolean hasActiveAffix(ItemMeta meta) {
        if (meta == null) {
            return false;
        }
        for (ActiveAffix activeAffix : ActiveAffix.values()) {
            if (meta.getPersistentDataContainer().has(activeAffix.getChanceKey(), PersistentDataType.DOUBLE)) {
                return true;
            }
        }
        return false;
    }

    private void applyActiveAffix(ItemMeta meta, List<Component> lore, ActiveAffix activeAffix, FileConfiguration config) {
        String path = activeAffix.getConfigPath();
        double chanceMin = config.getDouble(path + ".chance-min", 3.0);
        double chanceMax = config.getDouble(path + ".chance-max", 10.0);
        double chanceValue = randomInRange(chanceMin, chanceMax);
        meta.getPersistentDataContainer().set(activeAffix.getChanceKey(), PersistentDataType.DOUBLE, chanceValue);

        NamespacedKey extraKey = activeAffix.getExtraValueKey().orElse(null);
        Double extraValue = null;
        switch (activeAffix) {
            case FROST_ORB -> {
                double dmgMin = config.getDouble(path + ".damage-min", 3.0);
                double dmgMax = config.getDouble(path + ".damage-max", 10.0);
                extraValue = randomInRange(dmgMin, dmgMax);
                if (extraKey != null && extraValue != null) {
                    meta.getPersistentDataContainer().set(extraKey, PersistentDataType.DOUBLE, extraValue);
                }
            }
            case ARCANE_NOVA -> {
                double dmgMin = config.getDouble(path + ".damage-min", 5.0);
                double dmgMax = config.getDouble(path + ".damage-max", 12.0);
                extraValue = randomInRange(dmgMin, dmgMax);
                if (extraKey != null && extraValue != null) {
                    meta.getPersistentDataContainer().set(extraKey, PersistentDataType.DOUBLE, extraValue);
                }
            }
            case SHADOW_STEP -> {
                double bonusMin = config.getDouble(path + ".bonus-damage-min", 4.0);
                double bonusMax = config.getDouble(path + ".bonus-damage-max", 8.0);
                extraValue = randomInRange(bonusMin, bonusMax);
                if (extraKey != null && extraValue != null) {
                    meta.getPersistentDataContainer().set(extraKey, PersistentDataType.DOUBLE, extraValue);
                }
            }
            case ICE_COCOON -> {
                double heal = config.getDouble(path + ".heal-amount", 10.0);
                extraValue = heal;
                if (extraKey != null) {
                    meta.getPersistentDataContainer().set(extraKey, PersistentDataType.DOUBLE, extraValue);
                }
            }
            default -> {
                if (extraKey != null) {
                    meta.getPersistentDataContainer().set(extraKey, PersistentDataType.DOUBLE, chanceValue);
                }
            }
        }

        lore.add(activeAffix.createLore(chanceValue, extraValue));
    }

    private double randomInRange(double min, double max) {
        if (max <= min) {
            return min;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
    
    private List<Affix> getAvailableAffixes(AffixType type) {
        List<Affix> affixes = new ArrayList<>(List.of(Affix.values()));
        if (type == AffixType.DAMAGE) {
            affixes.removeIf(a -> a.name().endsWith("_RESIST"));
        } else if (type == AffixType.RESIST) {
            affixes.removeIf(a -> !a.name().endsWith("_RESIST"));
        } else {
            affixes.clear(); // Не повертаємо жодних афіксів, якщо тип NONE
        }
        return affixes;
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
        
        if (isSword(item.getType())) return AffixType.DAMAGE;
        if (isArmor(item.getType())) return AffixType.RESIST;
        
        return AffixType.NONE;
    }

    private boolean isSword(Material material) {
        return material.name().endsWith("_SWORD");
    }

    private boolean isArmor(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }
    
    private String buildRecipeKey(Player player, Recipe recipe) {
        if (!(recipe instanceof ShapedRecipe shapedRecipe)) {
            return null;
        }
        Map<Material, Integer> ingredientCount = new TreeMap<>();
        for (RecipeChoice choice : shapedRecipe.getChoiceMap().values()) {
            if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
                if (!materialChoice.getChoices().isEmpty()) {
                    ingredientCount.merge(materialChoice.getChoices().get(0), 1, Integer::sum);
                }
            }
        }
        String ingredientsString = ingredientCount.entrySet().stream()
                .map(entry -> entry.getKey().name() + ":" + entry.getValue())
                .collect(Collectors.joining(","));
        return player.getUniqueId() + "|" + recipe.getResult().getType().name() + "|" + ingredientsString;
    }
}