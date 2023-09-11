package io.github.vyketype.bazaarmarket.guis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.vyketype.bazaarmarket.listeners.impl.InventoryClickListener;
import io.github.vyketype.bazaarmarket.market.Order;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.MarketMenu;
import io.github.vyketype.bazaarmarket.util.MenuItems;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class OrdersListGUI {
      public static void open(UUID uuid) {
            Player player = Bukkit.getPlayer(uuid);
            assert player != null;
            
            InventoryClickListener.VIEWING_MARKET.add(player.getUniqueId());
            
            Profile profile = Profile.get(uuid);
            
            // Get all orders
            List<Order> orders = profile.getPlayerSellOrders();
            orders.addAll(profile.getPlayerBuyOrders());
            
            int rows = getNumberOfRows(profile);
            
            // Create GUI
            ChestGui gui = new ChestGui(rows, "Placed Orders");
            
            // Cancel clicking
            gui.setOnGlobalClick(event -> event.setCancelled(true));
            
            // Background pane
            StaticPane background = new StaticPane(0, 0, 9, rows, Pane.Priority.LOWEST);
            background.fillWith(MenuItems.getBlackPane());
            
            if (rows == 2) {
                  StaticPane infoPane = new StaticPane(4, 0, 1, 1);
                  
                  GuiItem noOrdersSign = new GuiItem(MenuItems.getNoOrdersSign());
                  infoPane.addItem(noOrdersSign, 0, 0);
                  
                  gui.addPane(infoPane);
            } else {
                  // Orders pane
                  OutlinePane ordersPane = new OutlinePane(1, 1, 7, rows - 2);
      
                  // Fill with the order items
                  for (Order order : orders) {
                        GuiItem item = new GuiItem(MenuItems.getOrderItem(player, order.getMaterial(), order));
                        item.setAction(event -> handleOrderClick(uuid, order));
                        ordersPane.addItem(item);
                  }
                  
                  int emptySlots = (rows * 7) - orders.size();
      
                  // Fill empty slots with air
                  for (int i = 0; i < emptySlots; i++) {
                        if (ordersPane.getItems().size() == (rows - 2) * 7)
                              break;
                        ordersPane.addItem(new GuiItem(MenuItems.getEmptyItem()));
                  }
      
                  gui.addPane(ordersPane);
            }
            
            // Bottom pane
            StaticPane bottomPane = new StaticPane(3, rows - 1, 2, 1);
            
            GuiItem goBack = new GuiItem(MenuItems.getBackArrow("§7Market"));
            goBack.setAction(event -> MarketSelectionGUI.open(uuid));
            bottomPane.addItem(goBack, 0, 0);
            
            // Add panes
            gui.addPane(background);
            gui.addPane(bottomPane);
      
            // Show GUI
            ChestGui finalGUI = MarketMenu.getFinalizedGUI(player, gui, false, false);
            finalGUI.show(player);
      }
      
      private static int getNumberOfRows(Profile profile) {
            int rows = 2;
      
            if (profile.getPlayerBuyOrders().isEmpty() && profile.getPlayerSellOrders().isEmpty())
                  return rows;
            
            int orders = profile.getPlayerBuyOrders().size() + profile.getPlayerSellOrders().size();
            return rows + (int) Math.ceil(orders / 7.0);
      }
      
      private static void handleOrderClick(UUID uuid, Order order) {
            Player player = Bukkit.getPlayer(uuid);
            
            if (order.isCancellable()) {
                  OrderCancelGUI.open(uuid, order);
            } else {
                  Profile.get(uuid).claimGoods(order);
                  player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 2F);
            
                  // Refresh the page
                  open(uuid);
            }
      }
}
