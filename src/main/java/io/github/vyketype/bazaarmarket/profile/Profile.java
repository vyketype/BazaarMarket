package io.github.vyketype.bazaarmarket.profile;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.market.Order;
import io.github.vyketype.bazaarmarket.market.OrderBook;
import io.github.vyketype.bazaarmarket.market.Trade;
import io.github.vyketype.bazaarmarket.util.InventoryUtil;
import io.github.vyketype.bazaarmarket.util.ItemUtil;
import io.github.vyketype.bazaarmarket.util.Messaging;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

/**
 * Profile to save player data.
 *
 * @author vyketype
 * @since 0.1
 */
@Getter
@Setter
public class Profile {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      private static final String PROFILES_PATH = INSTANCE.getDataFolder().getAbsolutePath() + "/profiles/";
      private static final Map<UUID, Profile> CACHE = new HashMap<>();
      
      private final UUID uuid;
      
      private BigDecimal balance;
      
      private NotificationSetting notificationSetting;
      
      // Trade-blocked players
      @Setter(AccessLevel.PRIVATE)
      private List<UUID> blockedPlayers;
      
      // Market-restricted
      private boolean restricted;
      
      // Has joined before
      @Getter(AccessLevel.NONE)
      private boolean hasJoinedBefore;
      
      // Statistics
      private int buyOrdersMade;
      private int sellOrdersMade;
      private int tradesMade;
      private BigDecimal moneySpent;
      private BigDecimal moneyGained;
      private int itemsSold;
      private int itemsBought;
      
      public Profile(UUID uuid) {
            this.uuid = uuid;
            balance = BigDecimal.valueOf(0.0);
            notificationSetting = NotificationSetting.TRADES;
            blockedPlayers = new ArrayList<>();
            restricted = false;
            buyOrdersMade = 0;
            sellOrdersMade = 0;
            tradesMade = 0;
            moneySpent = BigDecimal.valueOf(0.0);
            moneyGained = BigDecimal.valueOf(0.0);
            itemsSold = 0;
            itemsBought = 0;
      }
      
      public boolean hasJoinedBefore() {
            return hasJoinedBefore;
      }
      
      public void addBalance(BigDecimal amount) {
            balance = balance.add(amount);
      }
      
      public void removeBalance(BigDecimal amount) {
            balance = balance.subtract(amount);
      }
      
      public void tradeBlockPlayer(UUID uuid) {
            blockedPlayers.add(uuid);
      }
      
      public void unTradeBlockPlayer(UUID uuid) {
            blockedPlayers.remove(uuid);
      }
      
      public void addBuy() {
            buyOrdersMade += 1;
      }
      
      public void addSell() {
            sellOrdersMade += 1;
      }
      
      public void addTrade() {
            tradesMade += 1;
      }
      
      public void addMoneySpent(BigDecimal money) {
            moneySpent = moneySpent.add(money);
      }
      
      public void addMoneyGained(BigDecimal money) {
            moneyGained = moneyGained.add(money);
      }
      
      public void addItemsSold(int items) {
            itemsSold += items;
      }
      
      public void addItemsBought(int items) {
            itemsBought += items;
      }
      
      /**
       * Sends a message to the player when an order is filled.
       */
      public void sendCompletionNotification(Order order) {
            Player player = Objects.requireNonNull(Bukkit.getPlayer(uuid));
            if (player.isOnline()) {
                  Messaging.prefixedChat(player, "One of your orders was §a§lfilled§r" + order.getChatString());
                  player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 2F);
            }
      }
      
      public void sendTradeNotification(Order order) {
            Player player = Objects.requireNonNull(Bukkit.getPlayer(uuid));
            if (player.isOnline()) {
                  Messaging.prefixedChat(player, "A §a§ltrade §r§7occurred in this order§r" + order.getChatString());
                  player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 2F);
            }
      }
      
      public void claimGoods(Order order) {
            Player player = Bukkit.getPlayer(uuid);
            if (order.getType() == Order.Type.BUY) {
                  if (order.getRefundableCoins().compareTo(BigDecimal.valueOf(0)) > 0) {
                        BigDecimal refunded = order.getRefundableCoins();
                        addBalance(refunded);
                        order.clearRefunds();
                        order.save();
                        Messaging.prefixedChat(player, "§7You were refunded §6" + refunded.doubleValue() + "$§7.");
                  }
                  giveItems(player, order);
            } else {
                  giveCoins(player, order);
            }
      }
      
      private void giveItems(Player player, Order order) {
            int amount = 0;
            
            for (Trade trade : order.getTrades()) {
                  if (trade.isClaimed())
                        continue;
                  
                  amount += trade.getAmount();
                  trade.claim();
                  order.save();
                  
                  if (order.isFilled() && order.isCancellable()) {
                        order.kill();
                  }
            }
            
            if (amount != 0) {
                  InventoryUtil.addItemsToInventory(player, new ItemStack(order.getMaterial(), amount));
                  String itemName = ItemUtil.getStylizedName(order.getMaterial().name());
                  Messaging.prefixedChat(player, "You were given §6" + amount + "§8x §f" + itemName);
            }
      }
      
      private void giveCoins(Player player, Order order) {
            BigDecimal coins = BigDecimal.valueOf(0);
            for (Trade trade : order.getTrades()) {
                  if (trade.isClaimed())
                        continue;
                  
                  coins = coins.add(trade.getPrice().multiply(BigDecimal.valueOf(trade.getAmount())));
                  trade.claim();
                  order.save();
                  
                  if (order.isFilled() && order.isCancellable()) {
                        order.kill();
                  }
            }
            addBalance(coins);
            Messaging.prefixedChat(player, "§7You have received §6" + coins.doubleValue() + "$§7.");
      }
      
      public void refundGoodsOnCancellation(Order order) {
            Player player = Objects.requireNonNull(Bukkit.getPlayer(uuid));
            if (order.getType() == Order.Type.BUY) {
                  BigDecimal amount = order.getPrice().multiply(BigDecimal.valueOf(order.getVolume()));
                  addBalance(amount);
                  Messaging.prefixedChat(player, "§7You were refunded §6" + amount.doubleValue() + "$§7.");
            } else {
                  InventoryUtil.addItemsToInventory(player, new ItemStack(order.getMaterial(), order.getVolume()));
                  String itemName = ItemUtil.getStylizedName(order.getMaterial().name());
                  Messaging.prefixedChat(player, "You were refunded §6" + order.getVolume() + "§8x §f" + itemName);
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NETHERITE_BLOCK_PLACE, 1F, 1F);
      }
      
      public List<Order> getPlayerBuyOrders() {
            List<Order> list = new ArrayList<>();
            for (OrderBook book : OrderBook.getAll()) {
                  if (book.getBuyOrders().isEmpty())
                        continue;
                  for (Order order : book.getBuyOrders()) {
                        if (Bukkit.getOfflinePlayer(order.getPlayerUUID()) != Bukkit.getOfflinePlayer(uuid))
                              continue;
                        list.add(order);
                  }
            }
            return list;
      }
      
      public List<Order> getPlayerSellOrders() {
            List<Order> list = new ArrayList<>();
            for (OrderBook book : OrderBook.getAll()) {
                  if (book.getSellOrders().isEmpty())
                        continue;
                  for (Order order : book.getSellOrders()) {
                        if (Bukkit.getOfflinePlayer(order.getPlayerUUID()) != Bukkit.getOfflinePlayer(uuid))
                              continue;
                        list.add(order);
                  }
            }
            return list;
      }
      
      /**
       * Saves the Profile instance to the cache.
       */
      public void save() {
            CACHE.put(uuid, this);
            /*
            String playerName = Objects.requireNonNull(Bukkit.getOfflinePlayer(uuid)).getName();
            INSTANCE.getLogger().info("The Profile of " + playerName + " has been saved to the CACHE.");
             */
      }
      
      /**
       * Backs up the Profile instance to the files.
       */
      public void backup(boolean consoleMessage) {
            String filename = uuid.toString() + ".yml";
            
            File file = new File(PROFILES_PATH + filename);
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            
            List<String> blockedList = new ArrayList<>();
            for (UUID uuid : blockedPlayers) {
                  blockedList.add(uuid.toString());
            }
            
            yaml.set("balance", balance.doubleValue());
            yaml.set("notificationSetting", notificationSetting.name());
            yaml.set("blockedPlayers", blockedList);
            yaml.set("restricted", restricted);
            yaml.set("hasJoinedBefore", hasJoinedBefore);
            yaml.set("buyOrdersMade", buyOrdersMade);
            yaml.set("sellOrdersMade", sellOrdersMade);
            yaml.set("tradesMade", tradesMade);
            yaml.set("moneySpent", moneySpent.doubleValue());
            yaml.set("moneyGained", moneyGained.doubleValue());
            yaml.set("itemsSold", itemsSold);
            yaml.set("itemsBought", itemsBought);
            
            try {
                  yaml.save(file);
            } catch (IOException ex) {
                  ex.printStackTrace();
                  INSTANCE.getLogger().log(Level.SEVERE, "Unable to save Profile of " + uuid + ".");
            }
            
            if (consoleMessage) {
                  String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                  INSTANCE.getLogger().info("The Profile of " + playerName + " has been backed up to the files.");
            }
      }
      
      // --------------------------------------------------------------------------------------------
      
      /**
       * Backs up all Profile instances to the files.
       */
      public static void backupAll() {
            CACHE.values().forEach(profile -> profile.backup(false));
            INSTANCE.getLogger().info("All Profiles have been backed up.");
      }
      
      private static Profile getFromFiles(UUID uuid) {
            String filename = uuid.toString() + ".yml";
            
            File file = new File(PROFILES_PATH + filename);
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            
            List<UUID> blockedPlayers = new ArrayList<>();
            for (String string : yaml.getStringList("blockedPlayers")) {
                  blockedPlayers.add(UUID.fromString(string));
            }
            
            Profile profile = new Profile(uuid);
            profile.setBalance(BigDecimal.valueOf(yaml.getDouble("balance")));
            profile.setNotificationSetting(NotificationSetting.valueOf(yaml.getString("notificationSetting")));
            profile.setRestricted(yaml.getBoolean("restricted"));
            profile.setHasJoinedBefore(yaml.getBoolean("hasJoinedBefore"));
            profile.setBlockedPlayers(blockedPlayers);
            profile.setBuyOrdersMade(yaml.getInt("buyOrdersMade"));
            profile.setSellOrdersMade(yaml.getInt("sellOrdersMade"));
            profile.setTradesMade(yaml.getInt("tradesMade"));
            profile.setMoneySpent(BigDecimal.valueOf(yaml.getDouble("moneySpent")));
            profile.setMoneyGained(BigDecimal.valueOf(yaml.getDouble("moneyGained")));
            profile.setItemsSold(yaml.getInt("itemsSold"));
            profile.setItemsBought(yaml.getInt("itemsBought"));
            
            profile.save();
            return profile;
      }
      
      public static Profile get(UUID uuid) {
            // Get from CACHE
            if (CACHE.containsKey(uuid)) {
                  return CACHE.get(uuid);
            }
            
            // If the Profile does not exist
            String filename = uuid.toString() + ".yml";
            if (!new File(PROFILES_PATH + filename).exists()) {
                  Profile profile = new Profile(uuid);
                  profile.save();
                  return profile;
            }
            
            // Get from files
            Profile profile = getFromFiles(uuid);
            profile.save();
            return profile;
      }
}
