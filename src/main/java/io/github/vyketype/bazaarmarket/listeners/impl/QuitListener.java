package io.github.vyketype.bazaarmarket.listeners.impl;

import io.github.vyketype.bazaarmarket.profile.Profile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class QuitListener implements Listener {
      @EventHandler
      public void onQuit(PlayerQuitEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();
            
            Profile profile = Profile.get(uuid);
            profile.backup(true);
      
            // Remove from "viewing market" map
            InventoryClickListener.VIEWING_MARKET.remove(uuid);
      }
}
