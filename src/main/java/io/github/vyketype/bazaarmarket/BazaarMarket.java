package io.github.vyketype.bazaarmarket;

import io.github.vyketype.bazaarmarket.commands.CommandManager;
import io.github.vyketype.bazaarmarket.configuration.Config;
import io.github.vyketype.bazaarmarket.listeners.ListenerManager;
import io.github.vyketype.bazaarmarket.market.OrderBook;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

// TODO
// - see recently placed orders
// - update version
// - paginated categories (in case of 45+ items)
// - allow more than 9 categories

public class BazaarMarket extends JavaPlugin {
      public static final String MARKET = "§x§1§f§c§a§7§3§lM" +
              "§x§0§0§b§e§8§d§lA" +
              "§x§0§0§b§0§a§0§lR" +
              "§x§0§0§a§1§a§9§lK" +
              "§x§0§0§9§1§a§9§lE" +
              "§x§1§b§8§1§9§f§lT" +
              "§r";
      public static final String PREFIX = MARKET + "§8 | §7";
      public static final String VERSION = "v1.0-pre1";
      
      @Getter
      public static BazaarMarket INSTANCE;
      
      @Getter
      private Config config;
      
      @Getter
      private Config log;
      
      @Getter
      private Config stats;
      
      @Override
      public void onEnable() {
            INSTANCE = this;
            
            config = new Config(new File(getDataFolder().getAbsolutePath() + "/config.yml"), "config.yml");
            log = new Config(new File(getDataFolder().getAbsolutePath() + "/log.yml"), "log.yml");
            stats = new Config(new File(getDataFolder().getAbsolutePath() + "/stats.yml"), "stats.yml");
      
            new CommandManager();
            new ListenerManager();
            
            OrderBook.initializeBooks();
            Scheduler.scheduleBackup();
      
            getLogger().info("Success! Enabled BazaarMarket " + VERSION + " by vyketype");
            getLogger().info("https://github.com/vyketype/BazaarMarket");
      }
      
      @Override
      public void onDisable() {
            Profile.backupAll();
            OrderBook.backupAll();
            
            getLogger().info("Disabled BazaarMarket " + VERSION + " by vyketype");
            getLogger().info("https://github.com/vyketype/BazaarMarket");
            INSTANCE = null;
      }
}