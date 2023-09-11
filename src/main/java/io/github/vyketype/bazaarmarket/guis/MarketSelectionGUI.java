package io.github.vyketype.bazaarmarket.guis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.listeners.impl.InventoryClickListener;
import io.github.vyketype.bazaarmarket.market.Category;
import io.github.vyketype.bazaarmarket.util.MarketMenu;
import io.github.vyketype.bazaarmarket.util.MenuItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MarketSelectionGUI {
      public static void open(UUID uuid) {
            Player player = Bukkit.getPlayer(uuid);
            assert player != null;
            
            InventoryClickListener.VIEWING_MARKET.add(player.getUniqueId());
            
            // Create GUI
            ChestGui gui = new ChestGui(5, BazaarMarket.MARKET);
            
            // Cancel clicking
            gui.setOnGlobalClick(event -> event.setCancelled(true));
            
            // Background pane
            StaticPane selectionPane = new StaticPane(0, 0, 9, 5, Pane.Priority.LOWEST);
            selectionPane.fillWith(MenuItems.getBlackPane());
            
            // Populate pane
            int[][] coordinates = getCoordinates();
            Category.getCategories().forEach(category -> {
                  int index = category.getNumber() - 1;
                  int x = coordinates[index][0];
                  int y = coordinates[index][1];
                  GuiItem item = new GuiItem(MenuItems.getCategoryItem(category));
                  item.setAction(event -> MarketGUI.open(uuid, category));
                  selectionPane.addItem(item, x, y);
            });
      
            GuiItem myOrders = new GuiItem(MenuItems.getMyOrdersButton(player));
            myOrders.setAction(event -> OrdersListGUI.open(uuid));
            selectionPane.addItem(myOrders, 7, 5);
            
            // Bottom pane
            StaticPane bottomPane = MarketMenu.getBottomPane(player, 5, false);
            
            // Add panes
            gui.addPane(selectionPane);
            gui.addPane(bottomPane);
            
            // Show GUI
            ChestGui finalGUI = MarketMenu.getFinalizedGUI(player, gui, true, true);
            finalGUI.show(player);
      }
      
      private static int @NotNull [][] getCoordinates() {
            int[][] coordinates = new int[0][];
            switch (Category.getNumberOfCategories()) {
                  case 1 -> coordinates = new int[][] { {4, 1} };
                  case 2 -> coordinates = new int[][] { {3, 1}, {5, 1} };
                  case 3 -> coordinates = new int[][] { {3, 1}, {4, 1}, {5, 1} };
                  case 4 -> coordinates = new int[][] { {2, 1}, {3, 1}, {5, 1}, {6, 1} };
                  case 5 -> coordinates = new int[][] { {2, 1}, {3, 1}, {4, 1}, {5, 1}, {6, 1} };
                  case 6 -> coordinates = new int[][] { {1, 1}, {2, 1}, {3, 1}, {5, 1}, {6, 1}, {7, 1} };
                  case 7 -> coordinates = new int[][] { {1, 1}, {2, 1}, {3, 1}, {4, 1}, {5, 1}, {6, 1}, {7, 1} };
                  case 8 -> coordinates = new int[][] { {2, 1}, {3, 1}, {5, 1}, {6, 1}, {2, 2}, {3, 2}, {5, 2}, {6, 2} };
                  case 9 -> coordinates = new int[][] { {2, 1}, {3, 1}, {4, 1}, {5, 1}, {6, 1}, {2, 2}, {3, 2}, {5, 2}, {6, 2} };
            }
          return coordinates;
      }
}
