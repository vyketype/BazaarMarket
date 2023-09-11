package io.github.vyketype.bazaarmarket.listeners.impl;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.Messaging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

/**
 * Handles what happens when players join the server.
 *
 * @author vyketype
 * @since 0.1
 */
public class JoinListener implements Listener {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      @EventHandler
      public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            
            // Send login message 3 seconds after player joins
            String loginMessage = Objects.requireNonNull(INSTANCE.getConfig().getString("login_message"));
            if (!loginMessage.equalsIgnoreCase("-none")) {
                  INSTANCE.getServer().getScheduler().runTaskLater(
                          INSTANCE,
                          () -> Messaging.prefixedChat(player, loginMessage),
                          3 * 20L
                  );
            }
            
            Profile profile = Profile.get(player.getUniqueId());
            profile.setHasJoinedBefore(true);
            profile.save();
      }
}
