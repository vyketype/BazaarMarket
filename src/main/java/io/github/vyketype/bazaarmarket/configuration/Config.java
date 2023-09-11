package io.github.vyketype.bazaarmarket.configuration;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Better YAML config.
 *
 * @author dynmie
 * https://github.com/dynmie/
 */
public class Config extends YamlConfiguration {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      private final File file;
      private final String localDefaultsName;
      
      public Config(File file, String localDefaultsName) {
            this.file = file;
            this.localDefaultsName = localDefaultsName;
            try {
                  Reader stream = new InputStreamReader(Objects.requireNonNull(INSTANCE.getResource(localDefaultsName)),
                          StandardCharsets.UTF_8);
                  YamlConfiguration config = new YamlConfiguration();
                  config.load(stream);
                  this.setDefaults(config);
            } catch (IOException | InvalidConfigurationException e) {
                  e.printStackTrace();
                  INSTANCE.getLogger().warning("Failed to load defaults for " + file.getName() + ".");
            }
            createIfNotExists();
      }
      
      /**
       * Saves the updated YAML file.
       */
      public void save() {
            try {
                  super.save(file);
            } catch (IOException e) {
                  e.printStackTrace();
                  INSTANCE.getLogger().warning("Failed to save file " + file.getName() + ".");
            }
      }
      
      /**
       * Loads/reloads the existing YAML file.
       */
      public void reload() {
            try {
                  super.load(file);
            } catch (InvalidConfigurationException | IOException e) {
                  e.printStackTrace();
                  INSTANCE.getLogger().warning("Failed to load file " + file.getName() + ".");
            }
      }
      
      /**
       * Creates a YAML file if the specified file does not exist already.
       */
      public void createIfNotExists() {
            if (file.exists()) {
                  reload();
                  return;
            }
            
            if (localDefaultsName == null) {
                  save();
                  return;
            }
            
            try {
                  FileUtils.copyInputStreamToFile(Objects.requireNonNull(INSTANCE.getResource(localDefaultsName)), file);
            } catch (IOException e) {
                  e.printStackTrace();
                  INSTANCE.getLogger().warning("Failed to create " + file.getName() + ".");
            }
      }
}