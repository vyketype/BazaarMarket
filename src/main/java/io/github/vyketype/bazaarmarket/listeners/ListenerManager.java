package io.github.vyketype.bazaarmarket.listeners;

import com.google.common.collect.ImmutableList;
import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.listeners.impl.InventoryClickListener;
import io.github.vyketype.bazaarmarket.listeners.impl.InventoryCloseListener;
import io.github.vyketype.bazaarmarket.listeners.impl.JoinListener;
import io.github.vyketype.bazaarmarket.listeners.impl.QuitListener;
import org.bukkit.event.Listener;

public class ListenerManager {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      private static final ImmutableList<Listener> LISTENERS = ImmutableList.of(
              new JoinListener(),
              new QuitListener(),
              new InventoryClickListener(),
              new InventoryCloseListener()
      );
      
      public ListenerManager() {
            loadListeners();
      }
      
      public void loadListeners() {
            LISTENERS.forEach(listener -> INSTANCE.getServer().getPluginManager().registerEvents(listener, INSTANCE));
      }
}
