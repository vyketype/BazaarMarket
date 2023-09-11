package io.github.vyketype.bazaarmarket.market;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class Category {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      private final int number;
      private final Material material;
      private final char color;
      
      @Getter(AccessLevel.NONE)
      private final String name;
      
      public String getName() {
            return "§" + color + name;
      }
      
      public List<Material> getMaterials() {
            List<Material> materials = new ArrayList<>();
            INSTANCE.getConfig().getStringList("categories.category" + number + ".items").forEach(material -> {
                  materials.add(Material.valueOf(material.toUpperCase()));
            });
            return materials;
      }
      
      public static @Nullable Category getCategoryOfItem(Material material) {
            for (Category category : getCategories()) {
                  if (category.getMaterials().contains(material))
                        return category;
            }
            return null;
      }
      
      public static List<Category> getCategories() {
            List<Category> categories = new ArrayList<>();
            for (int i = 1; i <= getNumberOfCategories(); i++) {
                  String key = "categories.category" + i + ".";
                  String materialName = INSTANCE.getConfig().getString(key + "display_material").toUpperCase();
                  Material material = Material.getMaterial(materialName);
                  char color = INSTANCE.getConfig().get(key + "color").toString().toCharArray()[0];
                  String name = INSTANCE.getConfig().getString(key + "name");
                  categories.add(new Category(i, material, color, name));
            }
            return categories;
      }
      
      public static int getNumberOfCategories() {
            ConfigurationSection categories = INSTANCE.getConfig().getConfigurationSection("categories");
            return categories.getKeys(false).size();
      }
}
