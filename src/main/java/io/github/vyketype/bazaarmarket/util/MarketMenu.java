package io.github.vyketype.bazaarmarket.util;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.vyketype.bazaarmarket.guis.MarketSelectionGUI;
import io.github.vyketype.bazaarmarket.guis.OrdersListGUI;
import org.bukkit.entity.Player;

public class MarketMenu {
      public static ChestGui getFinalizedGUI(Player player, ChestGui existingGUI, boolean hasPlacedOrders, boolean hasInfoBooklet) {
            int rows = existingGUI.getRows();
            StaticPane bottomPane = new StaticPane(0, rows - 1, 9, 1);
            
            GuiItem closeBarrier = new GuiItem(MenuItems.getCloseBarrier());
            closeBarrier.setAction(event -> player.closeInventory());
            bottomPane.addItem(closeBarrier, 4, 0);
            
            if (hasInfoBooklet) {
                  GuiItem infoBooklet = new GuiItem(MenuItems.getInfoBooklet());
                  bottomPane.addItem(infoBooklet, 5, 0);
            }
            
            if (hasPlacedOrders) {
                  GuiItem placedOrders = new GuiItem(MenuItems.getMyOrdersButton(player));
                  placedOrders.setAction(event -> OrdersListGUI.open(player.getUniqueId()));
                  bottomPane.addItem(placedOrders, 7, 0);
            }
            
            existingGUI.addPane(bottomPane);
            return existingGUI;
      }
      
      public static StaticPane getBottomPane(Player player, int rows, boolean backButton) {
            StaticPane bottomPane = new StaticPane(0, rows - 1, 9, 1);
            
            GuiItem myProfile = new GuiItem(MenuItems.getProfileStats(player));
            bottomPane.addItem(myProfile, 1, 0);
            
            GuiItem marketStats = new GuiItem(MenuItems.getMarketStats());
            bottomPane.addItem(marketStats, 2, 0);
            
            if (backButton) {
                  GuiItem goBack = new GuiItem(MenuItems.getBackArrow("§7Market"));
                  goBack.setAction(event -> MarketSelectionGUI.open(player.getUniqueId()));
                  bottomPane.addItem(goBack, 3, 0);
            }
            
            return bottomPane;
      }
}
