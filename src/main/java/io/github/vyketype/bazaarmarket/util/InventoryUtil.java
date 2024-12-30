package io.github.vyketype.bazaarmarket.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility methods for inventory management.
 *
 * @author vyketype
 */
public class InventoryUtil {
      public static int getItemAmount(UUID uuid, Material material) {
            Player player = Bukkit.getPlayer(uuid);
            assert player != null;
            Inventory inv = player.getInventory();
            int num = 0;
            for (int i = 0; i < inv.getSize(); i++) {
                  if (inv.getItem(i) == null)
                        continue;
                  if (Objects.equals(Objects.requireNonNull(inv.getItem(i)).getType(), material)) {
                        num += Objects.requireNonNull(inv.getItem(i)).getAmount();
                  }
            }
            return num;
      }
      
      public static void addItemsToInventory(Player player, ItemStack itemStack) {
            Map<Integer, ItemStack> map = player.getInventory().addItem(itemStack);
            player.updateInventory();
            for (Map.Entry<Integer, ItemStack> entry : map.entrySet()) {
                  World world = player.getWorld();
                  Location location = player.getLocation();
                  world.dropItemNaturally(location, entry.getValue());
            }
      }
      
      public static void removeItemsFromInventory(Player player, Material material, int amount) {
            if (amount <= 0)
                  return;
            
            Inventory inventory = player.getInventory();
            int size = inventory.getSize();
            
            for (int slot = 0; slot < size; slot++) {
                  ItemStack itemStack = inventory.getItem(slot);
                  
                  if (itemStack == null)
                        continue;
                  
                  if (material != itemStack.getType())
                        continue;
                  
                  int newAmount = itemStack.getAmount() - amount;
                  if (newAmount > 0) {
                        itemStack.setAmount(newAmount);
                        break;
                  } else {
                        inventory.clear(slot);
                        amount = -newAmount;
                        if (amount == 0)
                              break;
                  }
            }
            
            player.updateInventory();
      }
}