package io.github.vyketype.bazaarmarket.guis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.vyketype.bazaarmarket.listeners.impl.InventoryClickListener;
import io.github.vyketype.bazaarmarket.market.Category;
import io.github.vyketype.bazaarmarket.util.MarketMenu;
import io.github.vyketype.bazaarmarket.util.MenuItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Լաց վերջին լացըդ, սիրտ իմ մենավոր։
 *
 * — վահան դերյան
 * @author vyketype
 */
public class MarketGUI {
      public static void open(UUID uuid, Category category) {
            Player player = Bukkit.getPlayer(uuid);
            assert player != null;
            
            InventoryClickListener.VIEWING_MARKET.add(player.getUniqueId());
            
            // Create GUI
            ChestGui gui = new ChestGui(6, category.getName().substring(2));
            
            // Cancel clicking
            gui.setOnGlobalClick(event -> event.setCancelled(true));
      
            // Background pane
            StaticPane background = new StaticPane(0, 0, 9, 6, Pane.Priority.LOWEST);
            background.fillWith(MenuItems.getBlackPane());
            
            // Market pane
            OutlinePane marketPane = new OutlinePane(0, 0, 9, 5);
            for (Material material : category.getMaterials()) {
                  GuiItem guiItem = new GuiItem(MenuItems.getMarketItem(category, material));
                  guiItem.setAction(event -> OrderItemGUI.open(uuid, category, material));
                  marketPane.addItem(guiItem);
            }
            
            // Bottom pane
            StaticPane bottomPane = MarketMenu.getBottomPane(player, 6, true);
            
            // Add panes
            gui.addPane(background);
            gui.addPane(marketPane);
            gui.addPane(bottomPane);
            
            // Show GUI
            ChestGui finalGUI = MarketMenu.getFinalizedGUI(player, gui, true, true);
            finalGUI.show(player);
      }
}
