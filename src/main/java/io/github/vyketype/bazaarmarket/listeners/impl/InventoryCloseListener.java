package io.github.vyketype.bazaarmarket.listeners.impl;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {
      @EventHandler
      public void onInventoryClose(InventoryCloseEvent event) {
            HumanEntity entity = event.getPlayer();
            if (!(entity instanceof Player player))
                  return;
            
            InventoryClickListener.VIEWING_MARKET.remove(player.getUniqueId());
      }
}
