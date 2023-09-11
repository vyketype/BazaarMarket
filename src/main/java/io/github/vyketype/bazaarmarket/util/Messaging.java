package io.github.vyketype.bazaarmarket.util;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import org.bukkit.entity.Player;

public class Messaging {
      public static void prefixedChat(Player player, String message) {
            player.sendMessage(BazaarMarket.PREFIX + message);
      }
}
