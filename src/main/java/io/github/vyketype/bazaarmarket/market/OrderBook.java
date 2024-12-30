package io.github.vyketype.bazaarmarket.market;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.profile.NotificationSetting;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.ItemUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

/**
 * Represents the order book for one item.
 * Ի՞նչ եղանք հիմա, ի՞նչ էինք առաջ։
 *
 * @author vyketype
 * @since 0.1
 */
@Getter
@AllArgsConstructor
public class OrderBook {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      private static final String BOOKS_PATH = INSTANCE.getDataFolder().getAbsolutePath() + "/orderbooks/";
      private static final Map<String, OrderBook> CACHE = new HashMap<>();
      
      private final Material material;
      
      private final List<Order> buyOrders;
      private final List<Order> sellOrders;
      
      public OrderBook(Material material) {
            this.material = material;
            buyOrders = new ArrayList<>();
            sellOrders = new ArrayList<>();
      }
      
      /**
       * Get the name of the material, as provided by Minecraft.
       */
      public String getItemName() {
            return material.name();
      }
      
      /**
       * Retrieve the four most recent buy orders.
       */
      public List<String> getRecentBuyOrders() {
            List<String> list = new ArrayList<>();
            
            // Find 4 most recent buy orders
            int buyIndex = 0;
            for (Order order : buyOrders) {
                  if (buyIndex == 8)
                        break;
                  list.add(order.toString());
                  buyIndex++;
            }
            
            return list;
      }
      
      /**
       * Retrieve the four most recent sell orders.
       */
      public List<String> getRecentSellOrders() {
            List<String> list = new ArrayList<>();
      
            // Find 4 most recent sell orders
            int sellIndex = 0;
            for (Order order : sellOrders) {
                  if (sellIndex == 4)
                        break;
                  list.add(order.toString());
                  sellIndex++;
            }
      
            return list;
      }
      
      /**
       * Gets the price for the cheapest buy order.
       */
      public BigDecimal getLowestBuyPrice() {
            List<Double> prices = new ArrayList<>();
            buyOrders.forEach(order -> prices.add(order.getPrice().doubleValue()));
            
            // Check if there are no orders
            if (prices.isEmpty())
                  return BigDecimal.valueOf(0.0);
            else return BigDecimal.valueOf(Collections.min(prices));
      }
      
      /**
       * Gets the price for the most expensive sell order.
       */
      public BigDecimal getHighestSellPrice() {
            List<Double> prices = new ArrayList<>();
            sellOrders.forEach(order -> prices.add(order.getPrice().doubleValue()));
            
            // Check if there are no orders
            if (prices.isEmpty())
                  return BigDecimal.valueOf(0.0);
            else return BigDecimal.valueOf(Collections.max(prices));
      }
      
      /**
       * Register a new buy Order.
       *
       * @param order The new buy Order.
       */
      public void buy(Order order) {
            // Add statistic to Profile
            Profile profile = Profile.get(order.getPlayerUUID());
            profile.addBuy();
            profile.save();
            
            // Add global statistic
            INSTANCE.getStats().set("buyOrders", INSTANCE.getStats().getInt("buyOrders") + 1);
            INSTANCE.getStats().save();
            
            // Add buy order to the OrderBook
            buyOrders.add(order);
      
            processOrder(order, sellOrders);
      }
      
      /**
       * Register a new sell Order.
       *
       * @param order The new sell Order.
       */
      public void sell(Order order) {
            // Add statistic to Profile
            Profile profile = Profile.get(order.getPlayerUUID());
            profile.addSell();
            profile.save();
            
            // Add global statistic
            INSTANCE.getStats().set("sellOrders", INSTANCE.getStats().getInt("sellOrders") + 1);
            INSTANCE.getStats().save();
      
            // Add sell order to the OrderBook
            sellOrders.add(order);
            
            processOrder(order, buyOrders);
      }
      
      /**
       * Find matching orders and handle them.
       *
       * @param initialOrder The order to process.
       * @param orders The list of matching buy or sell orders.
       */
      private void processOrder(Order initialOrder, List<Order> orders) {
            Order.Type type = initialOrder.getType();
            
            // Check all matching orders (they are already sorted by time)
            for (Order matchingOrder : orders) {
                  boolean selfTesting = INSTANCE.getConfig().getBoolean("self_testing", false);
                  if (!selfTesting && initialOrder.getPlayerUUID() == matchingOrder.getPlayerUUID())
                        continue;
                        
                  // Check for blocked players
                  Profile initialProfile = Profile.get(initialOrder.getPlayerUUID());
                  Profile matchingProfile = Profile.get(matchingOrder.getPlayerUUID());
                  if (initialProfile.getBlockedPlayers().contains(matchingOrder.getPlayerUUID()))
                        continue;
                  if (matchingProfile.getBlockedPlayers().contains(initialOrder.getPlayerUUID()))
                        continue;
                  
                  // Prices
                  double initialPrice = initialOrder.getPrice().doubleValue();
                  double matchingPrice = matchingOrder.getPrice().doubleValue();
                  
                  // Conditions to process a limit order
                  boolean buyCondition = type == Order.Type.BUY && matchingPrice <= initialPrice;
                  boolean sellCondition = type == Order.Type.SELL && matchingPrice >= initialPrice;
                  
                  // Check if the prices match
                  if (buyCondition || sellCondition) {
                        int traded = Math.min(initialOrder.getVolume(), matchingOrder.getVolume());
                        BigDecimal price = matchingOrder.getPrice();
      
                        // If an order was filled, but was not cancellable, then skip
                        if (traded == 0)
                              continue;
                        
                        // Decrease volumes of orders
                        initialOrder.decreaseVolume(traded);
                        matchingOrder.decreaseVolume(traded);
                        
                        handleRefunds(initialOrder, matchingOrder, traded);
      
                        // Find buy and sell orders
                        Order buyOrder = type == Order.Type.BUY ? initialOrder : matchingOrder;
                        Order sellOrder = type == Order.Type.SELL ? initialOrder : matchingOrder;
                        
                        addTrade(buyOrder, sellOrder, traded, price);
                        
                        // Send market notifications if the matching order was filled
                        if (matchingOrder.isFilled() && matchingProfile.getNotificationSetting() != NotificationSetting.OFF) {
                              matchingProfile.sendCompletionNotification(matchingOrder);
                        }
                        
                        // Send market notifications if the initial order was filled and stop this process
                        if (initialOrder.isFilled() && initialProfile.getNotificationSetting() != NotificationSetting.OFF) {
                              initialProfile.sendCompletionNotification(initialOrder);
                              break;
                        }
                  }
            }
            
            // Kill all filled/claimed orders
            // cleanUp();
      }
      
      private void handleRefunds(Order initialOrder, Order matchingOrder, int traded) {
            // If the initial order is not a buy order, then refunds are not needed
            // The price will be the buy order's price in the above case
            if (initialOrder.getType() != Order.Type.BUY)
                  return;
            
            // Refund = buyPrice - sellPrice
            BigDecimal refund = initialOrder.getPrice().subtract(matchingOrder.getPrice());
            BigDecimal totalRefund = refund.multiply(BigDecimal.valueOf(traded));
            
            // If the buy price matches the sell price
            if (totalRefund.compareTo(BigDecimal.valueOf(0)) == 0)
                  return;
            
            initialOrder.addRefunds(totalRefund);
            initialOrder.save();
      }
      
      private void addTrade(Order buyOrder, Order sellOrder, int amount, BigDecimal price) {
            Order[] twoOrders = new Order[] { buyOrder, sellOrder };
            for (Order order : twoOrders) {
                  Trade trade = new Trade(
                          buyOrder.getOrderId(),
                          sellOrder.getOrderId(),
                          System.currentTimeMillis(),
                          price,
                          amount,
                          false,
                          order.getType()
                  );
                  order.getTrades().add(trade);
                  order.save();
            }
            // trade.save();
      
            Profile buyer = Profile.get(buyOrder.getPlayerUUID());
            Profile seller = Profile.get(sellOrder.getPlayerUUID());
            
            updateLogForTrade(buyOrder, sellOrder, amount, price.doubleValue());
            updateTradeStatistics(buyer, seller, price, amount);
      
            if (buyer.getNotificationSetting() == NotificationSetting.TRADES) {
                  buyer.sendTradeNotification(buyOrder);
            }
      
            if (seller.getNotificationSetting() == NotificationSetting.TRADES) {
                  seller.sendTradeNotification(sellOrder);
            }
      }
      
      private void updateLogForTrade(Order buyOrder, Order sellOrder, int traded, double price) {
            String[] args = {
                    Bukkit.getPlayer(buyOrder.getPlayerUUID()).getName(),
                    buyOrder.getOrderId(),
                    String.valueOf(traded),
                    ItemUtil.getStylizedName(buyOrder.getMaterial().name()),
                    String.valueOf(price),
                    sellOrder.getOrderId()
            };
            String action = MarketAction.TRADE.format(args);
            MarketAction.updateLog(action);
      }
      
      private void updateTradeStatistics(Profile buyer, Profile seller, BigDecimal price, int amount) {
            BigDecimal total = price.multiply(BigDecimal.valueOf(amount));
      
            // Global statistics
            INSTANCE.getStats().set("trades", INSTANCE.getStats().getInt("trades") + 1);
            INSTANCE.getStats().set("moneyTraded", INSTANCE.getStats().getDouble("moneyTraded") + total.doubleValue());
            INSTANCE.getStats().set("itemsTraded", INSTANCE.getStats().getInt("itemsTraded") + amount);
            INSTANCE.getStats().save();
            
            // Buyer statistics
            buyer.addTrade();
            buyer.addMoneySpent(total);
            buyer.addItemsBought(amount);
            buyer.save();
            
            // Seller statistics
            seller.addTrade();
            seller.addMoneyGained(total);
            seller.addItemsSold(amount);
            seller.save();
      }
      
      /**
       * Kill all orders that have been filled and collected.
       */
      void cleanUp() {
            for (Order order : buyOrders) {
                  if (order.isFilled() && order.isCancellable())
                        order.kill();
            }
            for (Order order : sellOrders) {
                  if (order.isFilled() && order.isCancellable())
                        order.kill();
            }
      }
      
      public void save() {
            CACHE.put(material.name(), this);
            // INSTANCE.getLogger().info("Saved OrderBook of item " + getItemName() + " to the CACHE.");
      }
      
      public void backup(boolean consoleMessage) {
            String filename = material.name().toUpperCase() + ".yml";
            File file = new File(BOOKS_PATH + filename);
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            
            for (String key : yaml.getKeys(false)) {
                  yaml.set(key, null);
            }
            
            try {
                  yaml.save(file);
            } catch (IOException ex) {
                  ex.printStackTrace();
                  INSTANCE.getLogger().log(Level.SEVERE, "This is an issue.");
            }
            
            for (Order order : buyOrders) {
                  writeInFile(filename, order, "buys");
            }
            for (Order order : sellOrders) {
                  writeInFile(filename, order, "sells");
            }
            
            if (consoleMessage) {
                  INSTANCE.getLogger().info("Backed up OrderBook of item " + getItemName() + " to the files.");
            }
      }
      
      private void writeInFile(String filename, Order order, String type) {
            File file = new File(BOOKS_PATH + filename);
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            
            String initPath = type + "." + order.getOrderId() + ".";
            
            yaml.set(initPath + "timestamp", order.getTimestamp());
            yaml.set(initPath + "type", order.getType().name());
            yaml.set(initPath + "playerUUID", order.getPlayerUUID().toString());
            yaml.set(initPath + "price", order.getPrice());
            yaml.set(initPath + "amount", order.getAmount());
            yaml.set(initPath + "refunds", order.getRefundableCoins());
            yaml.set(initPath + "volume", order.getVolume());
            
            List<String> trades = new ArrayList<>() {{
                  order.getTrades().forEach(trade -> add(trade.getSerializedString()));
            }};
            yaml.set(initPath + "trades", trades);
            
            try {
                  yaml.save(file);
            } catch (IOException ex) {
                  ex.printStackTrace();
                  INSTANCE.getLogger().log(Level.SEVERE, "Unable to save OrderBook of " + getItemName() + ".");
            }
      }
      
      // --------------------------------------------------------------------------------------------

      public static List<Material> getMaterials() {
            List<Material> materials = new ArrayList<>();
            ConfigurationSection categories = INSTANCE.getConfig().getConfigurationSection("categories");
            for (String key : categories.getKeys(false)) {
                  INSTANCE.getConfig().getStringList("categories." + key + ".items").forEach(material -> {
                        materials.add(Material.valueOf(material.toUpperCase()));
                  });
            }
            return materials;
      }
      
      // NOTE: OrderBook files are saved as Material#name() IN UPPER CASE
      
      public static void initializeBooks() {
            List<Material> materials = getMaterials();
            
            for (Material material : materials) {
                  File file = new File(BOOKS_PATH + material.name().toUpperCase() + ".yml");
                  if (file.exists())
                        continue;
                  
                  try {
                        file.createNewFile();
                  } catch (IOException ex) {
                        ex.printStackTrace();
                        INSTANCE.getLogger().log(Level.SEVERE, "Unable to create OrderBook of " + material.name() + ".");
                  }
            }
            
            loadAll();
      }
      
      private static void loadAll() {
            for (Material material : getMaterials()) {
                  String filename = material.name().toUpperCase()+ ".yml";
                  
                  File file = new File(BOOKS_PATH + filename);
                  YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                  
                  List<Order> buyOrders = new ArrayList<>();
                  ConfigurationSection buys = yaml.getConfigurationSection("buys");
                  if (buys != null) {
                        for (String key : buys.getKeys(false)) {
                              buyOrders.add(getFromFile(filename, key, "buys", material));
                        }
                  }
                  
                  List<Order> sellOrders = new ArrayList<>();
                  ConfigurationSection sells = yaml.getConfigurationSection("sells");
                  if (sells != null) {
                        for (String key : sells.getKeys(false)) {
                              sellOrders.add(getFromFile(filename, key, "sells", material));
                        }
                  }
                  
                  OrderBook book = new OrderBook(material, buyOrders, sellOrders);
                  CACHE.put(material.name(), book);
                  
                  try {
                        yaml.save(file);
                  } catch (IOException ex) {
                        ex.printStackTrace();
                        INSTANCE.getLogger().log(Level.SEVERE, "Unable to save OrderBook of " + book.getItemName() + ".");
                  }
            }
            
            INSTANCE.getLogger().info("All OrderBooks (" + CACHE.values().size() + ") have been loaded.");
      }
      
      private static Order getFromFile(String filename, String orderId, String type, Material material) {
            File file = new File(BOOKS_PATH + filename);
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            
            String initPath = type + "." + orderId + ".";
            
            long timestamp = yaml.getLong(initPath + "timestamp");
            Order.Type orderType = Order.Type.valueOf(yaml.getString(initPath + "type"));
            UUID playerUUID = UUID.fromString(Objects.requireNonNull(yaml.getString(initPath + "playerUUID")));
            BigDecimal price = BigDecimal.valueOf(yaml.getDouble(initPath + "price"));
            int amount = yaml.getInt(initPath + "amount");
            BigDecimal refunds = BigDecimal.valueOf(yaml.getDouble(initPath + "refunds"));
            int volume = yaml.getInt(initPath + "volume");
            
            List<Trade> trades = new ArrayList<>();
            for (String strTrade : yaml.getStringList(initPath + "trades")) {
                  trades.add(new Trade(strTrade));
            }
            
            return new Order(material, orderId, timestamp, orderType, playerUUID, price, amount, refunds, volume, trades);
      }
      
      public static void backupAll() {
            CACHE.values().forEach(orderBook -> orderBook.backup(false));
            INSTANCE.getLogger().info("All OrderBooks have been backed up.");
      }
      
      public static OrderBook get(Material material) {
            return CACHE.get(material.name());
      }
      
      public static List<OrderBook> getAll() {
            return CACHE.values().stream().toList();
      }
      
      public static void resetAll() {
            // Reset stats.yml
            INSTANCE.getStats().set("buyOrders", 0);
            INSTANCE.getStats().set("sellOrders", 0);
            INSTANCE.getStats().set("trades", 0);
            INSTANCE.getStats().set("moneyTraded", 0);
            INSTANCE.getStats().set("itemsTraded", 0);
            INSTANCE.getStats().save();
            
            // Clear all orders
            for (OrderBook book : getAll()) {
                  book.getBuyOrders().clear();
                  book.getSellOrders().clear();
                  book.save();
            }
            
            INSTANCE.getLogger().info("All OrderBooks (" + CACHE.values().size() + ") have been reset.");
      }
}