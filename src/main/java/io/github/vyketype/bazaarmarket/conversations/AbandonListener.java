package io.github.vyketype.bazaarmarket.conversations;

import io.github.vyketype.bazaarmarket.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AbandonListener implements ConversationAbandonedListener {
      @Override
      public void conversationAbandoned(@NotNull ConversationAbandonedEvent event) {
            if (event.gracefulExit())
                  return;
            
            UUID uuid = (UUID) event.getContext().getSessionData("uuid");
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.isOnline()) {
                  Player player = offlinePlayer.getPlayer();
                  Messaging.prefixedChat(player, "§cOrder abandoned!");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
            }
      }
}
