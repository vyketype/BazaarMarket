package io.github.vyketype.bazaarmarket.scheduler;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.market.OrderBook;
import io.github.vyketype.bazaarmarket.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

public class Scheduler {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      /**
       * Backs up all Profiles and OrderBooks to the files every 5 minutes.
       *
       * @since 0.1
       */
      public static void scheduleBackup() {
            int backupTimer = INSTANCE.getConfig().getInt("backup_timer");
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTaskTimer(
                    INSTANCE,
                    () -> { Profile.backupAll(); OrderBook.backupAll(); },
                    backupTimer * 60 * 20L,
                    backupTimer * 60 * 20L
            );
      }
}
