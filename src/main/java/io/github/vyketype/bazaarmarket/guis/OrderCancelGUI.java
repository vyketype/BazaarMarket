package io.github.vyketype.bazaarmarket.guis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.vyketype.bazaarmarket.market.MarketAction;
import io.github.vyketype.bazaarmarket.market.Order;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.MarketMenu;
import io.github.vyketype.bazaarmarket.util.MenuItems;
import io.github.vyketype.bazaarmarket.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

public class OrderCancelGUI {
      public static void open(UUID uuid, Order order) {
            Player player = Bukkit.getPlayer(uuid);
            assert player != null;
      
            // Create GUI
            ChestGui gui = new ChestGui(4, "Cancel Order");
      
            // Cancel clicking
            gui.setOnGlobalClick(event -> event.setCancelled(true));
      
            // Background pane
            StaticPane background = new StaticPane(0, 0, 9, 4, Pane.Priority.LOWEST);
            background.fillWith(MenuItems.getBlackPane());
      
            // Cancel pane
            StaticPane cancelPane = new StaticPane(4, 1, 1, 1);
      
            GuiItem cancel = new GuiItem(MenuItems.getCancelOrderButton(order));
            cancel.setAction(event -> {
                  order.kill();
                  player.closeInventory();
                  
                  player.playSound(player.getLocation(), Sound.BLOCK_NETHERITE_BLOCK_PLACE, 1F, 1F);
                  Messaging.prefixedChat(player, "§7Your order§r" + order.getChatString() + " §7was §ccancelled§7.");
      
                  Profile profile = Profile.get(uuid);
                  profile.refundGoodsOnCancellation(order);
            
                  String orderType = order.getType().name().toLowerCase();
                  String action = MarketAction.DELETE_ORDER.format(player.getName(), orderType, order.getOrderId());
                  MarketAction.updateLog(action);
            });
            cancelPane.addItem(cancel, 0, 0);
      
            // Bottom pane
            StaticPane bottomPane = new StaticPane(3, 3, 2, 1);
            
            GuiItem goBack = new GuiItem(MenuItems.getBackArrow("§7Placed Orders"));
            goBack.setAction(event -> OrdersListGUI.open(uuid));
            bottomPane.addItem(goBack, 0, 0);
      
            // Add panes
            gui.addPane(background);
            gui.addPane(cancelPane);
            gui.addPane(bottomPane);
      
            // Show GUI
            ChestGui finalGUI = MarketMenu.getFinalizedGUI(player, gui, false, false);
            finalGUI.show(player);
      }
}
