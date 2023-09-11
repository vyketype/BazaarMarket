package io.github.vyketype.bazaarmarket.guis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.vyketype.bazaarmarket.market.MarketAction;
import io.github.vyketype.bazaarmarket.market.Order;
import io.github.vyketype.bazaarmarket.market.OrderBook;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderConfirmGUI {
      public static void open(UUID uuid, Order.Type type, Material material, int amount, BigDecimal price) {
            Player player = Bukkit.getPlayer(uuid);
            assert player != null;
      
            OrderBook book = OrderBook.get(material);

            // Create GUI
            ChestGui gui = new ChestGui(4, "Confirm Order");
      
            // Cancel clicking
            gui.setOnGlobalClick(event -> event.setCancelled(true));
      
            // Background pane
            StaticPane background = new StaticPane(0, 0, 9, 4, Pane.Priority.LOWEST);
            background.fillWith(MenuItems.getBlackPane());

            Order order = new Order(type, material, System.currentTimeMillis(), uuid, price, amount);
      
            // Confirm pane
            StaticPane confirmPane = new StaticPane(4, 1, 1, 1);
      
            GuiItem confirm = new GuiItem(MenuItems.getConfirmOrderButton(order));
            confirm.setAction(event -> {
                  // Message and sound
                  Messaging.prefixedChat(player, "§dNew order created§r" + order.getChatString());
                  player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1F, 1F);
                  
                  // Update log
                  String action = MarketAction.CREATE_ORDER.format(
                          player.getName(),
                          order.getType().name().toLowerCase(),
                          order.getOrderId(),
                          Integer.toString(order.getAmount()),
                          ItemUtil.getStylizedName(order.getMaterial().name()),
                          price.toString()
                  );
                  MarketAction.updateLog(action);
                  
                  // Item and money actions
                  switch (order.getType()) {
                        case BUY -> {
                              BigDecimal total = order.getPrice().multiply(BigDecimal.valueOf(order.getAmount()));
                              
                              Profile profile = Profile.get(uuid);
                              profile.removeBalance(total);
                              profile.save();
                              
                              book.buy(order);
                        }
                        case SELL -> {
                              InventoryUtil.removeItemsFromInventory(player, material, amount);
                              book.sell(order);
                        }
                  }
            
                  player.closeInventory();
            });
            confirmPane.addItem(confirm, 0, 0);
            
            // Add panes
            gui.addPane(background);
            gui.addPane(confirmPane);
      
            // Show GUI
            ChestGui finalGUI = MarketMenu.getFinalizedGUI(player, gui, false, true);
            finalGUI.show(player);
      }
}
