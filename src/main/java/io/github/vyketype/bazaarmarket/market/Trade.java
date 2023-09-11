package io.github.vyketype.bazaarmarket.market;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.ocpsoft.prettytime.PrettyTime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a trade in the market.
 *
 * @author vyketype
 * @since 0.1
 */
@Getter
@AllArgsConstructor
public class Trade {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      private String buyOrderId;
      private String sellOrderId;
      
      private long timestamp;
      private BigDecimal price;
      private int amount;
      
      @Setter
      private boolean claimed;
      
      private Order.Type belongingOrderType;
      
      public Trade(String serialized) {
            String[] parts = serialized.split(";");
            
            // Trade data
            buyOrderId = parts[0];
            sellOrderId = parts[1];
            timestamp = Long.parseLong(parts[2]);
            price = BigDecimal.valueOf(Double.parseDouble(parts[3]));
            amount = Integer.parseInt(parts[4]);
            claimed = Boolean.parseBoolean(parts[5]);
            belongingOrderType = Order.Type.valueOf(parts[6].toUpperCase());
      }
      
      public void claim() {
            claimed = true;
      }
     
      /**
       * Get a beautiful string to display the trade in a GUI.
       *
       * @param isViewerTheBuyer True if the viewer is the buyer, false if it is the seller.
       */
      public String toString(boolean isViewerTheBuyer) {
            // Format time
            long secondsTime = (System.currentTimeMillis() - timestamp) / 1000;
            PrettyTime prettyTime = new PrettyTime();
            String time = prettyTime.format(LocalDateTime.now().minusSeconds(secondsTime));
            
            // Name of buyer/seller
            String name;
            if (isViewerTheBuyer) {
                  @Nullable Order sellOrder = Order.get(sellOrderId);
                  if (sellOrder == null) {
                        sellOrder = Order.getDeadOrder(sellOrderId);
                  }
                  Player seller = Bukkit.getPlayer(sellOrder.getPlayerUUID());
                  name = seller.getName();
            } else {
                  @Nullable Order buyOrder = Order.get(buyOrderId);
                  if (buyOrder == null) {
                        buyOrder = Order.getDeadOrder(buyOrderId);
                  }
                  Player buyer = Bukkit.getPlayer(buyOrder.getPlayerUUID());
                  name = buyer.getName();
            }
            
            // Return completed string
            return " §8» §a" + amount + "§8x §7@ §6" + price.doubleValue() + "$ §8- §7" + name + " §8" + time;
      }
      
      /**
       * Saves the trade to both orders.
       */
      public void save() {
            Order[] orders = { Order.get(buyOrderId), Order.get(sellOrderId) };
      
            outerLoop:
            for (Order order : orders) {
                  // Update the trade in the trades list
                  for (Trade trade : order.getTrades()) {
                        if (trade.getTimestamp() != timestamp)
                              continue;
            
                        order.getTrades().set(order.getTrades().indexOf(trade), this);
                        order.save();
                        continue outerLoop;
                  }
      
                  // If the trade does not exist, add it to the list
                  order.getTrades().add(this);
                  order.save();
            }
      }
      
      /**
       * Get a serialized string of the Trade which can be saved to files.
       */
      public String getSerializedString() {
            return buyOrderId + ";" + sellOrderId + ";" + timestamp + ";" + price.doubleValue() + ";" +
                    amount + ";" + claimed + ";" + belongingOrderType.name();
      }
}
