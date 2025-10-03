package org.sosadly.customstats.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sosadly.customstats.CustomStats;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LocaleManager {

    private final CustomStats plugin;
    private FileConfiguration messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LocaleManager(CustomStats plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        // ВИПРАВЛЕННЯ: Спочатку зберігаємо всі дефолтні мови, якщо папки lang не існує.
        saveDefaultLanguages();

        String lang = plugin.getConfig().getString("language", "uk");
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");

        // Якщо файл все ще не існує (наприклад, користувач вказав неіснуючу мову),
        // завантажуємо українську за замовчуванням.
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + lang + ".yml' not found. Defaulting to 'uk.yml'.");
            langFile = new File(plugin.getDataFolder(), "lang/uk.yml");
        }
        messages = YamlConfiguration.loadConfiguration(langFile);

        // Завантажуємо значення за замовчуванням з JAR, якщо якісь ключі відсутні
        InputStream defaultStream = plugin.getResource("lang/" + lang + ".yml");
        if (defaultStream == null) {
            defaultStream = plugin.getResource("lang/uk.yml");
        }
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaultConfig);
            messages.options().copyDefaults(true);
            try {
                messages.save(langFile);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to save language defaults: " + ex.getMessage());
            }
        }
    }

    /**
     * При першому запуску створює папку lang і зберігає всі мови з JAR.
     */
    private void saveDefaultLanguages() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            // Зберігаємо всі відомі мови. Якщо додасте нові, допишіть їх сюди.
            plugin.saveResource("lang/uk.yml", false);
            plugin.saveResource("lang/en.yml", false);
        }
    }

    /**
     * Отримує форматований Component з мовного файлу.
     * Плейсхолдери в .yml файлах мають бути у форматі <key>.
     */
    public Component getMessage(String key, String... placeholders) {
        String message = messages.getString(key, "<red>Missing message: " + key);

        if (placeholders.length == 0) {
            return miniMessage.deserialize(message);
        }

        if (placeholders.length % 2 != 0) {
            plugin.getLogger().warning("Invalid placeholder count for message key: " + key);
            return miniMessage.deserialize(message);
        }

        List<TagResolver> resolvers = new ArrayList<>();
        for (int i = 0; i < placeholders.length; i += 2) {
            resolvers.add(Placeholder.unparsed(placeholders[i], placeholders[i + 1]));
        }

        return miniMessage.deserialize(message, TagResolver.resolver(resolvers));
    }

    /**
     * Отримує простий рядок з мовного файлу.
     */
    public String getRawString(String key, String def) {
        return messages.getString(key, def);
    }

    /**
     * Отримує список рядків з мовного файлу.
     */
    public List<String> getRawStringList(String key) {
        return messages.getStringList(key);
    }
}