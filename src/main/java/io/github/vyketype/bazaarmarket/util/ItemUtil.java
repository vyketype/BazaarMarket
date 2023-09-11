package io.github.vyketype.bazaarmarket.util;

import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class ItemUtil {
      /**
       * Get the stylized name of the item.
       *
       * @param itemName Name of the item, as provided by Minecraft.
       * @since 0.1
       */
      public static String getStylizedName(String itemName) {
            return WordUtils.capitalize(itemName.toLowerCase().replace("_", " "));
      }
      
      /**
       * Get the skull of a player.
       *
       * @param uuid UUID of the player.
       * @since 0.1
       */
      public static ItemStack getSkull(UUID uuid) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(Bukkit.getPlayer(uuid));
            skull.setItemMeta(meta);
            return skull;
      }
}
