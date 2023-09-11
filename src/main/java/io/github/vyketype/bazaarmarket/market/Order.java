package io.github.vyketype.bazaarmarket.market;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.util.ItemUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;

/**
 * Represents an order in the market.
 * All orders are limit orders.
 * Զըխխը՛մ։
 *
 * @author vyketype
 * @since 0.1
 */
@Getter
@AllArgsConstructor
public class Order {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      private static final String DEAD_PATH = INSTANCE.getDataFolder().getAbsolutePath() + "/dead_orders/";
      
      public enum Type {
            BUY, SELL
      }
      
      private final Material material;
      private final String orderId;
      
      @Setter
      private long timestamp;
      
      private final Type type;
      private final UUID playerUUID;
      
      @Setter
      private BigDecimal price;
      
      @Setter
      private int amount;
      
      private BigDecimal refundableCoins;
      
      /**
       * Represents how many items are left to be traded.
       */
      private int volume;
      
      /**
       * Represents a list of all trades, claimed or unclaimed.
       */
      private final List<Trade> trades;
      
      public Order(Type type, Material material, long timestamp, UUID playerUUID, BigDecimal price, int amount) {
            orderId = RandomStringUtils.randomAlphanumeric(9);
            this.material = material;
            this.type = type;
            this.timestamp = timestamp;
            this.playerUUID = playerUUID;
            this.price = price;
            this.amount = amount;
            refundableCoins = BigDecimal.ZERO;
            volume = amount;
            trades = new LinkedList<>();
      }
      
      public void clearRefunds() {
            this.refundableCoins = BigDecimal.ZERO;
      }
      
      public void addRefunds(BigDecimal refundableCoins) {
            this.refundableCoins = this.refundableCoins.add(refundableCoins);
      }
      
      /**
       * Removes the order from the OrderBook.
       */
      public void kill() {
            OrderBook book = OrderBook.get(material);
            switch (type) {
                  case BUY -> book.getBuyOrders().remove(this);
                  case SELL -> book.getSellOrders().remove(this);
            }
            book.save();
            backupDeadOrder(this);
      }
      
      public List<Trade> getTenRecentTrades() {
            if (trades.isEmpty())
                  return new ArrayList<>();
            List<Trade> list = trades.subList(Math.max(trades.size() - 10, 0), trades.size());
            Collections.reverse(list);
            return list;
      }
      
      /**
       * Checks if there are any trades or refunds that have not been claimed.
       */
      public boolean isCancellable() {
            return !anyTradesLeftToClaim() && refundableCoins.doubleValue() <= 0;
      }
      
      public boolean anyTradesLeftToClaim() {
            for (Trade trade : trades) {
                  if (!trade.isClaimed())
                        return true;
            }
            return false;
      }
      
      /**
       * Checks if the order is filled.
       */
      public boolean isFilled() {
            return volume == 0;
      }
      
      /**
       * Get the items there are to claim on the buyer's end.
       */
      public int getItemsToClaim() {
            int items = 0;
            for (Trade trade : trades) {
                  if (trade.isClaimed())
                        continue;
                  items += trade.getAmount();
            }
            return items;
      }
      
      /**
       * Get the amount of coins there are to claim on the seller's end.
       */
      public BigDecimal getCoinsToClaim() {
            BigDecimal coins = BigDecimal.valueOf(0.0);
            for (Trade trade : trades) {
                  if (trade.isClaimed())
                        continue;
                  coins = coins.add(trade.getPrice().multiply(BigDecimal.valueOf(trade.getAmount())));
            }
            return coins;
      }
      
      public void decreaseVolume(int amount) {
            volume -= amount;
      }
      
      /**
       * Saves the trade to the OrderBook instance.
       */
      public void save() {
            OrderBook book = OrderBook.get(material);
      
            switch (type) {
                  case BUY -> saveBuyOrder(book);
                  case SELL -> saveSellOrder(book);
            }
            
            // Log message
            INSTANCE.getLogger().info("Saved order " + orderId + " to the CACHE.");
      }
      
      private void saveBuyOrder(OrderBook book) {
            // Update the order in the orders list
            for (Order order : book.getBuyOrders()) {
                  if (!Objects.equals(order.getOrderId(), orderId))
                        continue;
                  book.getBuyOrders().set(book.getBuyOrders().indexOf(order), this);
                  book.save();
                  return;
            }
      
            // If the order does not exist, add it to the list
            book.getBuyOrders().add(this);
            book.save();
      }
      
      private void saveSellOrder(OrderBook book) {
            // Update the order in the orders list
            for (Order order : book.getSellOrders()) {
                  if (!Objects.equals(order.getOrderId(), orderId))
                        continue;
                  book.getSellOrders().set(book.getSellOrders().indexOf(order), this);
                  book.save();
                  return;
            }
            
            // If the order does not exist, add it to the list
            book.getSellOrders().add(this);
            book.save();
      }
      
      /**
       * Get a beautiful string to display the order in a GUI.
       */
      @Override
      public String toString() {
            // Format time
            long secondsTime = (System.currentTimeMillis() - timestamp) / 1000;
            PrettyTime prettyTime = new PrettyTime();
            String time = prettyTime.format(LocalDateTime.now().minusSeconds(secondsTime));
            
            String playerName = Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getName();
            
            // Format string
            return getChatString() + "§7 from §b" + playerName + " §7" + time;
      }
      
      /**
       * Get a beautiful string to display the order in chat.
       */
      public String getChatString() {
            String prefix = type == Order.Type.BUY ? "§a§lBUY §r§a" : "§6§lSELL §r§6";
            return " §8» " + prefix + amount + "§8x §f" + ItemUtil.getStylizedName(material.name()) + " §7@ " +
                    "§6" + price.doubleValue() + "$ ea§7";
      }
      
      // --------------------------------------------------------------------------------------------
      
      /**
       * Fetch an order using its order ID. Returns null an order was not found.
       *
       * @param orderId Order ID.
       */
      @Nullable public static Order get(String orderId) {
            for (OrderBook book : OrderBook.getAll()) {
                  // Check buy orders
                  for (Order order : book.getBuyOrders()) {
                        if (Objects.equals(order.getOrderId(), orderId))
                              return order;
                  }
                  
                  // Check sell orders
                  for (Order order : book.getSellOrders()) {
                        if (Objects.equals(order.getOrderId(), orderId))
                              return order;
                  }
            }
            
            // Nothing was found
            return null;
      }
      
      /**
       * Fetch a dead order using its order ID. Returns null an order was not found.
       *
       * @param orderId Order ID.
       */
      @Nullable public static Order getDeadOrder(String orderId) {
            String filename = orderId + ".yml";
            File file = new File(DEAD_PATH + filename);
            
            // Nothing was found (but this shouldn't happen)
            if (!file.exists())
                  return null;
            
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            
            Material material = Material.valueOf(yaml.getString("itemName").toUpperCase());
            long timestamp = yaml.getLong("timestamp");
            Order.Type orderType = Order.Type.valueOf(yaml.getString("type"));
            UUID playerUUID = UUID.fromString(Objects.requireNonNull(yaml.getString("playerUUID")));
            BigDecimal price = BigDecimal.valueOf(yaml.getDouble("price"));
            int amount = yaml.getInt("amount");
            BigDecimal refunds = BigDecimal.valueOf(yaml.getDouble("refunds"));
            int volume = yaml.getInt("volume");
            
            List<Trade> trades = new ArrayList<>();
            for (String strTrade : yaml.getStringList("trades")) {
                  trades.add(new Trade(strTrade));
            }
            
            return new Order(material, orderId, timestamp, orderType, playerUUID, price, amount, refunds, volume, trades);
      }
      
      /**
       * Backs up the dead Order instance to the files.
       *
       * @param order The Order to back up.
       */
      private static void backupDeadOrder(Order order) {
            String filename = order.getOrderId() + ".yml";
            File file = new File(DEAD_PATH + filename);
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
      
            yaml.set("itemName", order.getMaterial().name());
            yaml.set("timestamp", order.getTimestamp());
            yaml.set("type", order.getType().name());
            yaml.set("playerUUID", order.getPlayerUUID().toString());
            yaml.set("price", order.getPrice());
            yaml.set("amount", order.getAmount());
            yaml.set("refunds", order.getRefundableCoins());
            yaml.set("volume", order.getVolume());
      
            List<String> trades = new ArrayList<>() {{
                  order.getTrades().forEach(trade -> add(trade.getSerializedString()));
            }};
            yaml.set("trades", trades);
            
            try {
                  yaml.save(file);
            } catch (IOException ex) {
                  ex.printStackTrace();
                  INSTANCE.getLogger().log(Level.SEVERE, "Unable to save dead order " + order.getOrderId() + ".");
            }
      }
}