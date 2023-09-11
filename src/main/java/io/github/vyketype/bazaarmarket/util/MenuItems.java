package io.github.vyketype.bazaarmarket.util;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.market.Category;
import io.github.vyketype.bazaarmarket.market.Order;
import io.github.vyketype.bazaarmarket.market.OrderBook;
import io.github.vyketype.bazaarmarket.profile.Profile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ու տես որդիս, ուր էլ լինես,
 * Այս լուսնի տակ ուր էլ գնաս,
 * Թե մորդ անգամ մտքից հանես,
 * Քո Մայր լեզուն չմոռանա՛ս...
 *
 * — սիլվա կապուտիկեան
 * @author vyketype
 */
public class MenuItems {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      public static ItemStack getInfoBooklet() {
            // TODO: fill in lore lines
            return new ItemBuilder(new ItemStack(Material.KNOWLEDGE_BOOK))
                    .setDisplayName("§dInformation Booklet")
                    .addLoreLine("TODO")
                    .build();
      }
      
      public static ItemStack getBackArrow(String text) {
            return new ItemBuilder(new ItemStack(Material.ARROW))
                    .setDisplayName("§aGo Back")
                    .addLoreLine("§8[" + text + "§8]")
                    .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .build();
      }
      
      public static ItemStack getCloseBarrier() {
            return new ItemBuilder(new ItemStack(Material.BARRIER))
                    .setDisplayName("§cClose Menu")
                    .build();
      }
      
      public static ItemStack getBlackPane() {
            return new ItemBuilder(new ItemStack(Material.BLACK_STAINED_GLASS_PANE))
                    .setDisplayName(" ")
                    .build();
      }
      
      public static ItemStack getEmptyItem() {
            return new ItemBuilder(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE))
                    .setDisplayName(" ")
                    .build();
      }
      
      // --------------------------------------------------------------------------------------------
      
      public static ItemStack getNoOrdersSign() {
            return new ItemBuilder(new ItemStack(Material.BIRCH_HANGING_SIGN))
                    .setDisplayName("§eSorry!")
                    .addLoreLine("§7You do not have any orders.")
                    .build();
      }
      
      public static ItemStack getMarketItem(Category category, Material material) {
            OrderBook book = OrderBook.get(material);
            return new ItemBuilder(material)
                    .setDisplayName("§r§" + category.getColor() + ItemUtil.getStylizedName(material.name()))
                    .addLoreLine("§7Lowest buy offer: §6" + book.getLowestBuyPrice().doubleValue() + "$")
                    .addLoreLine("§7Highest sell offer: §6" + book.getHighestSellPrice().doubleValue() + "$")
                    .addLoreLine(" ")
                    .addLoreLine("§eClick to buy or sell!")
                    .build();
      }
      
      public static ItemStack getProfileStats(Player player) {
            Profile profile = Profile.get(player.getUniqueId());
            return new ItemBuilder(ItemUtil.getSkull(player.getUniqueId()))
                    .setDisplayName("§6" + player.getName() + "'s Statistics")
                    .addLoreLine(" ")
                    .addLoreLine("§7Buy orders made: §b" + profile.getBuyOrdersMade())
                    .addLoreLine("§7Sell orders made: §b" + profile.getSellOrdersMade())
                    .addLoreLine("§7Trades made: §a" + profile.getTradesMade())
                    .addLoreLine(" ")
                    .addLoreLine("§7Money spent: §6" + profile.getMoneySpent().doubleValue() + "$")
                    .addLoreLine("§7Money gained: §6" + profile.getMoneyGained().doubleValue() + "$")
                    .addLoreLine("§7Items sold: §d" + profile.getItemsSold() + " items")
                    .addLoreLine("§7Items bought: §d" + profile.getItemsBought() + " items")
                    .addLoreLine(" ")
                    /* TODO
                    .addLoreLine("§7Item most sold: §3" + a)
                    .addLoreLine("§7Item most bought: §3" + a)
                    .addLoreLine("§7Coins most spent on: §c" + a)
                    .addLoreLine("§7Coins most gained on: §c" + a)
                     */
                    .build();
      }
      
      public static ItemStack getMarketStats() {
            return new ItemBuilder(new ItemStack(Material.EMERALD))
                    .setDisplayName("§aMarket Statistics")
                    .addLoreLine(" ")
                    .addLoreLine("§7Buy orders made: §b" + INSTANCE.getStats().get("buyOrders", 0))
                    .addLoreLine("§7Sell orders made: §b" + INSTANCE.getStats().get("sellOrders", 0))
                    .addLoreLine("§7Trades made: §a" + INSTANCE.getStats().get("trades", 0))
                    .addLoreLine(" ")
                    .addLoreLine("§7Money traded: §6" + INSTANCE.getStats().get("moneyTraded", 0) + "$")
                    .addLoreLine("§7Items traded: §d" + INSTANCE.getStats().get("itemsTraded", 0) + " items")
                    .addLoreLine(" ")
                    /* TODO
                    .addLoreLine("§7Item most sold: §3" + a)
                    .addLoreLine("§7Item most bought: §3" + a)
                    .addLoreLine("§7Coins most spent on: §c" + a)
                    .addLoreLine("§7Coins most gained on: §c" + a)
                    .addLoreLine(" ")
                    .addLoreLine("§7Most buy orders: §b" + a)
                    .addLoreLine("§7Most sell orders: §b" + a)
                    .addLoreLine("§7Most orders: §a" + a)
                    */
                    .build();
      }
      
      public static ItemStack getMyOrdersButton(Player player) {
            Profile profile = Profile.get(player.getUniqueId());
            List<Order> orders = profile.getPlayerBuyOrders();
            orders.addAll(profile.getPlayerSellOrders());
            
            // Check if there are any goods to claim
            boolean goods = false;
            for (Order order : orders) {
                  if (!order.isCancellable()) {
                        goods = true;
                        break;
                  }
            }
            
            ItemBuilder builder = new ItemBuilder(new ItemStack(Material.BOOK))
                    .setDisplayName("§bPlaced Orders")
                    .addLoreLine(" ");
            
            if (goods) {
                  builder.addLoreLine("§aYou have goods to claim!");
            }
            
            return builder.addLoreLine("§eClick to view your orders!").build();
      }
      
      // --------------------------------------------------------------------------------------------
      
      public static ItemStack getCategoryItem(Category category) {
            return new ItemBuilder(new ItemStack(category.getMaterial()))
                    .setDisplayName(category.getName())
                    .addLoreLine(" ")
                    .addLoreLine("§eClick to view this category!")
                    .build();
      }
      
      // --------------------------------------------------------------------------------------------
      
      public static ItemStack getOrderItem(Player player, Material material, Order order) {
            String name = ItemUtil.getStylizedName(order.getMaterial().name());
            Order.Type type = order.getType();
            String prefix = type == Order.Type.BUY ? "§a§lBUY §r§a" : "§6§lSELL §r§6";
            String maxOrMin = type == Order.Type.BUY ? "max." : "min.";
            BigDecimal total = order.getPrice().multiply(BigDecimal.valueOf(order.getAmount()));
      
            // Top info and prices
            ItemBuilder builder = new ItemBuilder(new ItemStack(material))
                    .setDisplayName(prefix + order.getAmount() + "§8x §f" + name)
                    .addLoreLine(" ")
                    .addLoreLine("§7Price per unit: §8" + maxOrMin + " §6" + order.getPrice().doubleValue() + "$")
                    .addLoreLine("§3Total price: §8" + maxOrMin + " §6§l" + total.doubleValue() + "$")
                    .addLoreLine(" ");
            
            // Percentage of the order that is filled
            if (order.getVolume() != order.getAmount()) {
                  int percentage = 100 - (int) Math.round((double) order.getVolume() / (double) order.getAmount() * 100);
                  String strPercentage = "§a§lFILLED";
                  if (order.getVolume() != 0) {
                        strPercentage = "§8(§e" + percentage + "%§8)";
                  }
                  String amount = "§a" + (order.getAmount() - order.getVolume()) + "§7/" + order.getAmount();
                  builder.addLoreLine("§7Filled: " + amount + " " + strPercentage).addLoreLine(" ");
            }
            
            // If there are no trades, build the ItemStack and return
            if (order.getTrades().isEmpty()) {
                  return builder.addLoreLine("§8Order ID: " + order.getOrderId())
                          .addLoreLine("§eClick to view more options!")
                          .build();
            }
            
            // Trades list (10 most recent trades)
            builder.addLoreLine(type == Order.Type.BUY ? "§7Vendor(s):" : "§7Buyer(s):");
            
            boolean isViewerTheBuyer = player.getUniqueId() == order.getPlayerUUID() && type == Order.Type.BUY;
            order.getTenRecentTrades().forEach(trade -> builder.addLoreLine(trade.toString(isViewerTheBuyer)));
            
            if (order.getTrades().size() > 10) {
                  builder.addLoreLine(" §8§oand more...");
            }
            
            // Cancel order
            if (order.isCancellable()) {
                  return builder.addLoreLine(" ")
                          .addLoreLine("§8Order ID: " + order.getOrderId())
                          .addLoreLine("§cClick to cancel!")
                          .build();
            }
            
            // Claiming goods or coins
            if (order.anyTradesLeftToClaim()) {
                  builder.addLoreLine(" ");
                  if (type == Order.Type.BUY) {
                        builder.addLoreLine("§aYou have §2" + order.getItemsToClaim() + " items §ato claim!");
            
                  } else {
                        BigDecimal coinsToClaim = order.getCoinsToClaim();
                        builder.addLoreLine("§eYou have §6" + coinsToClaim.doubleValue() + "$ §eto claim!");
                  }
            }
            
            // Refunds
            double refundable = order.getRefundableCoins().doubleValue();
            if (refundable > 0) {
                  builder.addLoreLine(" ").addLoreLine("§7Pending refunds: §d" + refundable + "$");
            }
            
            return builder.addLoreLine(" ")
                    .addLoreLine("§8Order ID: " + order.getOrderId())
                    .addLoreLine("§eClick to claim!")
                    .build();
      }
      
      public static ItemStack getCancelOrderButton(Order order) {
            String name = ItemUtil.getStylizedName(order.getMaterial().name());
            
            ItemBuilder builder = new ItemBuilder(new ItemStack(Material.RED_TERRACOTTA))
                    .setDisplayName("§cCancel Order")
                    .addLoreLine(" ");
            
            // Give back non-accounted-for items
            switch (order.getType()) {
                  case BUY -> builder.addLoreLine("§7You will be refunded §6" +
                          (order.getPrice().multiply(BigDecimal.valueOf(order.getVolume()))).doubleValue() + "$§7.");
                  case SELL -> builder.addLoreLine("§7You will be refunded §a" + order.getVolume() + "§8x §f" + name + "§7.");
            }
            
            return builder.addLoreLine(" ")
                    .addLoreLine("§eClick to cancel!")
                    .build();
      }
      
      public static ItemStack getConfirmOrderButton(Order order) {
            Material material = order.getMaterial();
            String name = ItemUtil.getStylizedName(material.name());
            String maxOrMin = order.getType() == Order.Type.BUY ? "max." : "min.";
            double value = order.getPrice().multiply(BigDecimal.valueOf(order.getAmount())).doubleValue();
      
            return new ItemBuilder(new ItemStack(material))
                    .setDisplayName("§aConfirm Order")
                    .addLoreLine((order.getType() == Order.Type.BUY ? "§a§lBUY" : "§6§lSELL") + " §r§b" +
                            order.getAmount() + "§8x §f" + name)
                    .addLoreLine(" ")
                    .addLoreLine("§7Price per unit: §6" + order.getPrice().doubleValue() + "$")
                    .addLoreLine("§3Total price: §8" + maxOrMin + " §6§l" + value + "$")
                    .addLoreLine(" ")
                    .addLoreLine("§8ID: " + order.getOrderId())
                    .addLoreLine("§eClick to confirm order!")
                    .build();
      }
      
      public static ItemStack getBuyButton(Material material) {
            OrderBook book = OrderBook.get(material);
            ItemBuilder builder = new ItemBuilder(new ItemStack(Material.EMERALD))
                    .setDisplayName("§aBuy Order")
                    .addLoreLine("§7Best price per unit: §6" + book.getLowestBuyPrice().doubleValue() + "$")
                    .addLoreLine(" ");
            
            if (book.getRecentBuyOrders().isEmpty()) {
                  builder.addLoreLine("§8§oNo recent orders");
            }
            
            // Get 8 most recent orders
            for (String order : book.getRecentBuyOrders()) {
                  builder.addLoreLine(order);
            }
            
            return builder.addLoreLine(" ")
                    .addLoreLine("§8This is a limit order.")
                    .addLoreLine("§eClick to create!")
                    .build();
      }
      
      public static ItemStack getSellButton(Material material, int num) {
            OrderBook book = OrderBook.get(material);
            BigDecimal total = book.getHighestSellPrice().multiply(BigDecimal.valueOf(num));
            ItemBuilder builder = new ItemBuilder(new ItemStack(Material.GOLD_INGOT))
                    .setDisplayName("§6Sell Order")
                    .addLoreLine("§7Best price per unit: §6" + book.getHighestSellPrice().doubleValue() + "$")
                    .addLoreLine("§7Inventory: §a" + num + " items")
                    .addLoreLine("§7Best total price: §d" + total.doubleValue() + "$")
                    .addLoreLine(" ");
      
            if (book.getRecentSellOrders().isEmpty()) {
                  builder.addLoreLine("§8§oNo recent orders");
            }
            
            // Get 8 most recent orders
            for (String order : book.getRecentSellOrders()) {
                  builder.addLoreLine(order);
            }
            
            return builder.addLoreLine(" ")
                    .addLoreLine("§8This is a limit order.")
                    .addLoreLine("§eClick to create!")
                    .build();
      }
      
      public static ItemStack getBestPriceButton(Material material, Order.Type type, int amount) {
            OrderBook book = OrderBook.get(material);
            BigDecimal buyPrice = book.getLowestBuyPrice();
            BigDecimal sellPrice = book.getHighestSellPrice();
            
            return new ItemBuilder(new ItemStack(material))
                    .setDisplayName("§eBest current price")
                    .addLoreLine(type == Order.Type.BUY ?
                            "§7Price: §6" + buyPrice.doubleValue() + "$" :
                            "§7Price: §6" + sellPrice.doubleValue() + "$")
                    .addLoreLine(" ")
                    .addLoreLine(type == Order.Type.BUY ? "§7Buying: §a" + amount + "§8x" : "§7Selling: §a" + amount + "§8x")
                    .addLoreLine(type == Order.Type.BUY ?
                            "§3Total price: §6" + (buyPrice.multiply(BigDecimal.valueOf(amount))).doubleValue() + "$" :
                            "§3Total price: §6" + (sellPrice.multiply(BigDecimal.valueOf(amount))).doubleValue() + "$")
                    .addLoreLine(" ")
                    .addLoreLine("§eClick to set!")
                    .build();
      }
      
      public static ItemStack getChangedPriceButton(Material material, Order.Type type, int amount) {
            OrderBook book = OrderBook.get(material);
            BigDecimal buyPrice = book.getLowestBuyPrice().add(BigDecimal.valueOf(0.1));
            BigDecimal sellPrice = book.getHighestSellPrice().add(BigDecimal.valueOf(0.1));
            
            return new ItemBuilder(new ItemStack(Material.GOLD_NUGGET))
                    .setDisplayName(type == Order.Type.BUY ?
                            "§eBest current price +0.1" :
                            "§eBest current price -0.1")
                    .addLoreLine(type == Order.Type.BUY ?
                            "§7Price: §6" + (book.getLowestBuyPrice().add(BigDecimal.valueOf(0.1))).doubleValue() + "$" :
                            "§7Price: §6" + (book.getHighestSellPrice().subtract(BigDecimal.valueOf(0.1))).doubleValue() + "$")
                    .addLoreLine(" ")
                    .addLoreLine(type == Order.Type.BUY ? "§7Buying: §a" + amount + "§8x" : "§7Selling: §a" + amount + "§8x")
                    .addLoreLine(type == Order.Type.BUY ?
                            "§3Total price: §6" + buyPrice.multiply(BigDecimal.valueOf(amount)).doubleValue() + "$" :
                            "§3Total price: §6" + sellPrice.multiply(BigDecimal.valueOf(amount)).doubleValue() + "$")
                    .addLoreLine(" ")
                    .addLoreLine("§eClick to set!")
                    .build();
      }
      
      public static ItemStack getCustomPriceButton() {
            return new ItemBuilder(new ItemStack(Material.NAME_TAG))
                    .setDisplayName("§eCustom price")
                    .addLoreLine("§7Set a custom price for")
                    .addLoreLine("§7your needs.")
                    .addLoreLine(" ")
                    .addLoreLine("§eClick to set!")
                    .build();
      }
}