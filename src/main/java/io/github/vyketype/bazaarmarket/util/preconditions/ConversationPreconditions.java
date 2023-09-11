package io.github.vyketype.bazaarmarket.util.preconditions;

import io.github.vyketype.bazaarmarket.listeners.impl.InventoryClickListener;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.InventoryUtil;
import io.github.vyketype.bazaarmarket.util.Messaging;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class ConversationPreconditions {
      public static boolean playerHasEnoughMoney(Player player, double amount) {
            if (Profile.get(player.getUniqueId()).getBalance().doubleValue() < amount) {
                  Messaging.prefixedChat(player, "§cYou do not have that much money! Order cancelled.");
                  // player.sendTitle("§c§lNot enough money!", "§7You're poor.");
                  playSoundAndCancelOrder(player);
                  return false;
            }
            return true;
      }
      
      public static boolean playerHasEnoughItems(Player player, Material material, int amount) {
            if (amount > InventoryUtil.getItemAmount(player.getUniqueId(), material)) {
                  Messaging.prefixedChat(player, "§cYou don't have this many items! Order cancelled.");
                  // player.sendTitle("§c§lNot enough items!", "§7You're poor.");
                  playSoundAndCancelOrder(player);
                  return false;
            }
            return true;
      }
      
      public static boolean isPositiveInteger(Player player, Number input) {
            if (input.doubleValue() % 1 != 0 || input.intValue() <= 0) {
                  Messaging.prefixedChat(player, "§cThis is not a positive integer! Order cancelled.");
                  // player.sendTitle("§c§lInvalid number!", "§7Must be a positive integer.");
                  playSoundAndCancelOrder(player);
                  return false;
            }
            return true;
      }
      
      public static boolean isPositiveDouble(Player player, Number input) {
            if (input.doubleValue() <= 0) {
                  Messaging.prefixedChat(player, "§cThis is not a positive integer! Order cancelled.");
                  // player.sendTitle("§c§lInvalid number!", "§7Must be a positive number.");
                  playSoundAndCancelOrder(player);
                  return false;
            }
            return true;
      }
      
      private static void playSoundAndCancelOrder(Player player) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
            InventoryClickListener.VIEWING_MARKET.remove(player.getUniqueId());
      }
}
