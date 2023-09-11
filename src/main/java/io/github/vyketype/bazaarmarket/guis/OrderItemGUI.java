package io.github.vyketype.bazaarmarket.guis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.conversations.AbandonListener;
import io.github.vyketype.bazaarmarket.conversations.prompts.AmountPrompt;
import io.github.vyketype.bazaarmarket.listeners.impl.InventoryClickListener;
import io.github.vyketype.bazaarmarket.market.Category;
import io.github.vyketype.bazaarmarket.market.Order;
import io.github.vyketype.bazaarmarket.util.InventoryUtil;
import io.github.vyketype.bazaarmarket.util.ItemUtil;
import io.github.vyketype.bazaarmarket.util.MarketMenu;
import io.github.vyketype.bazaarmarket.util.MenuItems;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class OrderItemGUI {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      public static void open(UUID uuid, Category category, Material material) {
            Player player = Bukkit.getPlayer(uuid);
            assert player != null;
            
            InventoryClickListener.VIEWING_MARKET.add(player.getUniqueId());
            
            // Create GUI
            ChestGui gui = new ChestGui(4, ItemUtil.getStylizedName(material.name()));
            
            // Cancel clicking
            gui.setOnGlobalClick(event -> event.setCancelled(true));
      
            // Background pane
            StaticPane background = new StaticPane(0, 0, 9, 4, Pane.Priority.LOWEST);
            background.fillWith(MenuItems.getBlackPane());
      
            // Item pane
            StaticPane itemPane = new StaticPane(4, 1, 1, 1);
            GuiItem orderItem = new GuiItem(new ItemStack(material));
            itemPane.addItem(orderItem, 0, 0);
      
            // Transactions pane
            StaticPane transactionsPane = new StaticPane(1, 1, 7, 1);
      
            for (Transactions transaction : Transactions.values()) {
                  GuiItem guiItem = transaction.getGuiItem(player, material);
                  guiItem.setAction(event -> {
                        player.closeInventory();
                  
                        // Start a chat conversation for the transaction item amount
                        ConversationFactory factory = new ConversationFactory(INSTANCE)
                                .withFirstPrompt(new AmountPrompt())
                                .withModality(false)
                                .withEscapeSequence("quit")
                                .withTimeout(20)
                                .withLocalEcho(false)
                                .addConversationAbandonedListener(new AbandonListener())
                                .thatExcludesNonPlayersWithMessage("Go away evil console!");
                        Conversation conversation = factory.buildConversation(player);
                        conversation.getContext().setSessionData("uuid", player.getUniqueId());
                        conversation.getContext().setSessionData("type", transaction.getType());
                        conversation.getContext().setSessionData("item", material.name());
                        conversation.begin();
                  });
                  transactionsPane.addItem(guiItem, transaction.getX(), transaction.getY());
            }
      
            // Bottom pane
            StaticPane bottomPane = new StaticPane(3, 3, 1, 1);
            
            GuiItem goBack = new GuiItem(MenuItems.getBackArrow(category.getName()));
            goBack.setAction(event -> MarketGUI.open(uuid, category));
            bottomPane.addItem(goBack, 0, 0);
      
            // Add panes
            gui.addPane(background);
            gui.addPane(itemPane);
            gui.addPane(transactionsPane);
            gui.addPane(bottomPane);
      
            // Show GUI
            ChestGui finalGUI = MarketMenu.getFinalizedGUI(player, gui, true, true);
            finalGUI.show(player);
      }
      
      @Getter
      @AllArgsConstructor
      private enum Transactions {
            BUY(Order.Type.BUY, 0, 0),
            SELL(Order.Type.SELL, 6, 0);
            
            private final Order.Type type;
            private final int x;
            private final int y;
            
            public GuiItem getGuiItem(Player player, Material material) {
                  if (this == BUY) {
                        return new GuiItem(MenuItems.getBuyButton(material));
                  } else {
                        int amount = InventoryUtil.getItemAmount(player.getUniqueId(), material);
                        return new GuiItem(MenuItems.getSellButton(material, amount));
                  }
            }
      }
}