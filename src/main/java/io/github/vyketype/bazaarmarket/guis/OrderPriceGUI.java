package io.github.vyketype.bazaarmarket.guis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.conversations.AbandonListener;
import io.github.vyketype.bazaarmarket.conversations.prompts.CustomPricePrompt;
import io.github.vyketype.bazaarmarket.market.Order;
import io.github.vyketype.bazaarmarket.market.OrderBook;
import io.github.vyketype.bazaarmarket.util.ItemUtil;
import io.github.vyketype.bazaarmarket.util.MarketMenu;
import io.github.vyketype.bazaarmarket.util.MenuItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderPriceGUI {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      public static void open(UUID uuid, Order.Type type, Material material, int amount) {
            Player player = Bukkit.getPlayer(uuid);
            assert player != null;
      
            OrderBook book = OrderBook.get(material);
            String pageTitle = type.name() + " - " + ItemUtil.getStylizedName(material.name());
      
            // Create GUI
            ChestGui gui = new ChestGui(4, pageTitle);
      
            // Cancel clicking
            gui.setOnGlobalClick(event -> event.setCancelled(true));
      
            // Background pane
            StaticPane background = new StaticPane(0, 0, 9, 4, Pane.Priority.LOWEST);
            background.fillWith(MenuItems.getBlackPane());
      
            // Prices pane
            StaticPane pricesPane = new StaticPane(2, 1, 5, 1);
      
            boolean emptyBuyCondition = type == Order.Type.BUY && book.getBuyOrders().isEmpty();
            boolean emptySellCondition = type == Order.Type.SELL && book.getSellOrders().isEmpty();
      
            // Custom price item
            GuiItem customPrice = new GuiItem(MenuItems.getCustomPriceButton());
            customPrice.setAction(event -> {
                  player.closeInventory();
            
                  // Start a chat conversation for the transaction unit price
                  ConversationFactory factory = new ConversationFactory(INSTANCE)
                          .withFirstPrompt(new CustomPricePrompt())
                          .withModality(false)
                          .withEscapeSequence("quit")
                          .withTimeout(20)
                          .withLocalEcho(false)
                          .addConversationAbandonedListener(new AbandonListener())
                          .thatExcludesNonPlayersWithMessage("Go away evil console!");
                  Conversation conversation = factory.buildConversation(player);
                  conversation.getContext().setSessionData("uuid", player.getUniqueId());
                  conversation.getContext().setSessionData("type", type);
                  conversation.getContext().setSessionData("item", material.name());
                  conversation.getContext().setSessionData("amount", amount);
                  conversation.begin();
            });
      
            // Check if we should put market price options
            if (emptyBuyCondition || emptySellCondition) {
                  pricesPane.addItem(customPrice, 2, 0);
            } else {
                  // Market price item
                  GuiItem marketPrice = new GuiItem(MenuItems.getBestPriceButton(material, type, amount));
                  marketPrice.setAction(event -> {
                        BigDecimal price = BigDecimal.ZERO;
                        switch (type) {
                              case BUY -> price = book.getLowestBuyPrice();
                              case SELL -> price = book.getHighestSellPrice();
                        }
                        OrderConfirmGUI.open(uuid, type, material, amount, price);
                  });
                  pricesPane.addItem(marketPrice, 0, 0);
            
                  // Optimal price item
                  GuiItem changedPrice = new GuiItem(MenuItems.getChangedPriceButton(material, type, amount));
                  changedPrice.setAction(event -> {
                        BigDecimal price = BigDecimal.ZERO;
                        switch (type) {
                              case BUY -> price = book.getLowestBuyPrice().add(BigDecimal.valueOf(0.1));
                              case SELL -> price = book.getHighestSellPrice().subtract(BigDecimal.valueOf(0.1));
                        }
                        OrderConfirmGUI.open(uuid, type, material, amount, price);
                  });
                  pricesPane.addItem(changedPrice, 2, 0);
            
                  // Custom price item
                  pricesPane.addItem(customPrice, 4, 0);
            }
      
            // Add panes
            gui.addPane(background);
            gui.addPane(pricesPane);
      
            // Show GUI
            ChestGui finalGUI = MarketMenu.getFinalizedGUI(player, gui, false, true);
            finalGUI.show(player);
      }
}
