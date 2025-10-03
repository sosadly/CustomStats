package org.sosadly.customstats;

import org.bukkit.plugin.java.JavaPlugin;
import org.sosadly.customstats.command.CustomStatsCommand;
import org.sosadly.customstats.listener.ApplyAffixItemListener;
import org.sosadly.customstats.listener.CraftListener;
import org.sosadly.customstats.listener.DamageListener;
import org.sosadly.customstats.manager.LocaleManager;

import java.util.Objects;

public final class CustomStats extends JavaPlugin {

    private LocaleManager localeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Ініціалізуємо та завантажуємо мовні файли
        this.localeManager = new LocaleManager(this);
        localeManager.loadMessages();

        // Реєстрація слухачів подій
        getServer().getPluginManager().registerEvents(new CraftListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new ApplyAffixItemListener(this), this);

        // Реєстрація головної команди
        Objects.requireNonNull(getCommand("customstats")).setExecutor(new CustomStatsCommand(this));

        getLogger().info("CustomStats has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomStats has been disabled.");
    }

    public void reload() {
        reloadConfig();
        localeManager.loadMessages();
        getLogger().info("Configuration and language files reloaded.");
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }
}