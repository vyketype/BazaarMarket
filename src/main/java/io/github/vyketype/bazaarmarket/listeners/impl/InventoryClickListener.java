package io.github.vyketype.bazaarmarket.listeners.impl;

import io.github.vyketype.bazaarmarket.guis.OrderItemGUI;
import io.github.vyketype.bazaarmarket.market.Category;
import io.github.vyketype.bazaarmarket.market.OrderBook;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class InventoryClickListener implements Listener {
      public static List<UUID> VIEWING_MARKET = new ArrayList<>();
      
      @EventHandler
      public void onInventoryClick(InventoryClickEvent event) {
            HumanEntity entity = event.getWhoClicked();
            
            // If we're not dealing with a player
            if (!(entity instanceof Player player))
                  return;
            
            // If the player is not viewing the market
            if (!VIEWING_MARKET.contains(player.getUniqueId()))
                  return;
      
            // If the player clicks an item in the GUI
            if (!Objects.equals(event.getClickedInventory(), entity.getInventory()))
                  return;
            
            // If the player clicked on air
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack == null || itemStack.getType() == Material.AIR)
                  return;
            
            Material material = itemStack.getType();
      
            // If the player clicked on an item that is not sold on the market
            if (!OrderBook.getMaterials().contains(material))
                  return;
            
            Category category = Category.getCategoryOfItem(material);
            OrderItemGUI.open(player.getUniqueId(), category, material);
      }
}
