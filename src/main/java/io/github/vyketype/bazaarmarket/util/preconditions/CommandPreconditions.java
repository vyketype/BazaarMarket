package io.github.vyketype.bazaarmarket.util.preconditions;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.market.OrderBook;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.Messaging;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class CommandPreconditions {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      public static boolean isMarketAlreadyOpen(Player player) {
            if (INSTANCE.getConfig().getBoolean("market_open")) {
                  Messaging.prefixedChat(player, "§cThe market is already open.");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return true;
            }
            return false;
      }
      
      public static boolean isMarketAlreadyClosed(Player player) {
            if (!INSTANCE.getConfig().getBoolean("market_open")) {
                  Messaging.prefixedChat(player, "§cThe market is already closed.");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return true;
            }
            return false;
      }
      
      public static boolean isMarketOpen(Player player) {
            if (!INSTANCE.getConfig().getBoolean("market_open")) {
                  Messaging.prefixedChat(player, "§cThe market is closed. Please wait a few business days!");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return false;
            }
            return true;
      }
      
      public static boolean isPlayerMarketRestricted(Player player) {
            Profile profile = Profile.get(player.getUniqueId());
            if (profile.isRestricted()) {
                  Messaging.prefixedChat(player, "§cYou are restricted from accessing the market!");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return true;
            }
            return false;
      }
      
      public static boolean isPlayerHoldingAir(Player player, Material material) {
            if (material == Material.AIR) {
                  Messaging.prefixedChat(player, "§cYou are not holding an item in your hand!");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return true;
            }
            return false;
      }
      
      public static boolean hasPlayerEverJoined(Player player, UUID uuid) {
            if (!Profile.get(uuid).hasJoinedBefore()) {
                  Messaging.prefixedChat(player, "§cThis player has never joined the server!");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return false;
            }
            return true;
      }
      
      public static boolean doesMarketItemExist(Player player, Material material) {
            if (!OrderBook.getMaterials().contains(material)) {
                  Messaging.prefixedChat(player, "§cThe held item is not on the market!");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return false;
            }
            return true;
      }
      
      public static boolean isNumberValidDouble(Player player, String amount) {
            try {
                  Double.parseDouble(amount);
            } catch (NullPointerException | NumberFormatException e) {
                  Messaging.prefixedChat(player, "§cThis is not a valid number!");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return false;
            }
            return true;
      }
      
      public static boolean isNumberPositive(Player player, double number) {
            if (number <= 0) {
                  Messaging.prefixedChat(player, "§cThis is not a positive number!");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return false;
            }
            return true;
      }
      
      public static boolean economy(Player player, String strArgs) {
            String[] args = StringUtils.split(strArgs, " ", -1);
            String targetName = args[0];
            String strAmount = args[1];
            
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            
            if (!hasPlayerEverJoined(player, offlineTarget.getUniqueId()))
                  return false;
            
            if (!isNumberValidDouble(player, strAmount))
                  return false;
            
            return CommandPreconditions.isNumberPositive(player, Double.parseDouble(strAmount));
      }
      
      public static boolean isPlayerAlreadyTradeBlocked(Player player, String name) {
            Profile profile = Profile.get(player.getUniqueId());
            UUID uuid = Bukkit.getOfflinePlayer(name).getUniqueId();
            if (profile.getBlockedPlayers().contains(uuid)) {
                  Messaging.prefixedChat(player, "§cThis player is already in your trade-block list.");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return true;
            }
            return false;
      }
      
      public static boolean isPlayerAlreadyNotTradeBlocked(Player player, String name) {
            Profile profile = Profile.get(player.getUniqueId());
            UUID uuid = Bukkit.getOfflinePlayer(name).getUniqueId();
            if (!profile.getBlockedPlayers().contains(uuid)) {
                  Messaging.prefixedChat(player, "§cThis player is not in your trade-block list.");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  return true;
            }
            return false;
      }
}
