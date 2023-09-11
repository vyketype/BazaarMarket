package io.github.vyketype.bazaarmarket.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.conversations.prompts.MarketResetConfirmationPrompt;
import io.github.vyketype.bazaarmarket.guis.MarketSelectionGUI;
import io.github.vyketype.bazaarmarket.profile.NotificationSetting;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.Messaging;
import io.github.vyketype.bazaarmarket.util.preconditions.CommandPreconditions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

/**
 * Market command.
 * Վե՜հ գաղափար Դաշնակցութեան։
 *
 * @author vyketype
 */
@CommandAlias("market|m|nasdaq|nyse|snp500|dowjones")
public class MarketCommand extends BaseCommand {
      /*
       * SOUNDS
       * - order fill notification: entity.experience_orb.pickup 2F
       * - claimed goods: entity.experience_orb.pickup 2F
       * - placed new order: block.amethyst_block.break 1F
       * - invalid/error: block.anvil.land 1F
       * - restricted/closed: block.anvil.land 0.5F
       * - unrestricted/open: entity.player.levelup 0.5F
       * - market reset: entity.experience_orb.pickup 0.5F
       * - cancelled order: block.netherite_block.place 1F
       */
      
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      @Default
      @Description("Opens the market menu")
      @CommandPermission("market.use")
      public void onMarket(Player player) {
            // If the market is not open
            if (!CommandPreconditions.isMarketOpen(player))
                  return;
            
            // If the player is restricted from the market
            if (CommandPreconditions.isPlayerMarketRestricted(player))
                  return;
            
            // Open the GUI
            MarketSelectionGUI.open(player.getUniqueId());
      }
      
      @Subcommand("open")
      @CommandPermission("market.open")
      @Description("Opens the market to the public")
      public void onMarketOpen(Player player) {
            // If the market was already open
            if (CommandPreconditions.isMarketAlreadyOpen(player))
                  return;
            
            // Open the market gates
            INSTANCE.getConfig().set("market_open", true);
            INSTANCE.getConfig().save();
            
            // Send messages and play sounds
            for (Player p : Bukkit.getOnlinePlayers()) {
                  Messaging.prefixedChat(p, "§7The market is now §aopen§7 for business!");
                  p.sendTitle(BazaarMarket.PREFIX + "§b§lOpen", "§7Use §a/market §7to access it.");
                  player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 0.5F);
            }
      }
      
      @Subcommand("close")
      @CommandPermission("market.close")
      @Description("Closes the market")
      public void onMarketClose(Player player) {
            // If the market was already closed
            if (CommandPreconditions.isMarketAlreadyClosed(player))
                  return;
            
            // Close the market gates
            INSTANCE.getConfig().set("market_open", false);
            INSTANCE.getConfig().save();
      
            // Send messages and play sounds
            for (Player p : Bukkit.getOnlinePlayers()) {
                  Messaging.prefixedChat(p, "§7The market is now §cclosed§7!");
                  p.sendTitle(BazaarMarket.PREFIX + "§c§lClosed", "§7It's just business.");
                  player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1F, 0.5F);
            }
      }
      
      @Subcommand("reset")
      @CommandPermission("market.reset")
      @Description("Resets the market (clears all active orders and resets global market statistics)")
      public void onMarketReset(Player player) {
            // Start a chat conversation to confirm this action
            ConversationFactory factory = new ConversationFactory(INSTANCE)
                    .withFirstPrompt(new MarketResetConfirmationPrompt())
                    .withModality(false)
                    .withTimeout(10)
                    .withLocalEcho(false)
                    .thatExcludesNonPlayersWithMessage("Go away evil console!");
            Conversation conversation = factory.buildConversation(player);
            conversation.getContext().setSessionData("uuid", player.getUniqueId());
            conversation.begin();
      }
      
      @Subcommand("notify")
      @Description("Mark the setting for your market notifications")
      @Syntax("<trades|completion|off>")
      public void onMarketNotify(Player player, String setting) {
            Profile profile = Profile.get(player.getUniqueId());
            
            switch (setting.toLowerCase()) {
                  case "trades" -> profile.setNotificationSetting(NotificationSetting.TRADES);
                  case "completion" -> profile.setNotificationSetting(NotificationSetting.COMPLETION);
                  case "off" -> profile.setNotificationSetting(NotificationSetting.OFF);
                  default -> {
                        Messaging.prefixedChat(player, "§cNo such setting exists! Try: TRADES, COMPLETION or OFF.");
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
                  }
            }
            
            Messaging.prefixedChat(player, "Your market notifications setting was set to §d" + setting.toUpperCase() + "§7.");
            profile.save();
      }
      
      @Subcommand("stats")
      @CommandPermission("market.stats")
      @Description("Check your market statistics")
      public void onMarketStats(Player player) {
            Profile profile = Profile.get(player.getUniqueId());
      
            Messaging.prefixedChat(player, "§7Here are your §dmarket statistics§7...");
            player.sendMessage(" §8» §7Buy orders made: §b" + profile.getBuyOrdersMade());
            player.sendMessage(" §8» §7Sell orders made: §b" + profile.getSellOrdersMade());
            player.sendMessage(" §8» §7Trades made: §a" + profile.getTradesMade());
            player.sendMessage(" §8» §7Money spent: §6" + profile.getMoneySpent() + "$");
            player.sendMessage(" §8» §7Money gained: §6" + profile.getMoneyGained() + "$");
            player.sendMessage(" §8» §7Items sold: §d" + profile.getItemsSold());
            player.sendMessage(" §8» §7Items bought: §d" + profile.getItemsBought());
      }
      
      @Subcommand("restrict")
      @CommandPermission("market.restrict")
      @Description("Restricts/unrestricts a player from accessing the market")
      @Syntax("<target>")
      public void onMarketRestrict(Player player, String targetName) {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            
            Profile profile = Profile.get(offlineTarget.getUniqueId());
            
            // Unrestrict/restrict
            if (profile.isRestricted()) {
                  handleUnrestrict(player, offlineTarget);
                  profile.setRestricted(false);
            } else {
                  handleRestrict(player, offlineTarget);
                  profile.setRestricted(true);
            }
            
            profile.save();
      }
      
      @HelpCommand
      public void onHelp(CommandHelp help) {
            help.showHelp();
      }
      
      private void handleUnrestrict(Player player, OfflinePlayer target) {
            Messaging.prefixedChat(player, "§a" + target.getName() + "§7is now §aallowed §7to access the market.");
            if (target.isOnline()) {
                  Player targetPlayer = target.getPlayer();
                  Messaging.prefixedChat(targetPlayer, "§7You are now allowed to §aaccess§7 the market!");
                  targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 0.5F);
            }
      }
      
      private void handleRestrict(Player player, OfflinePlayer target) {
            Messaging.prefixedChat(player, "§c" + target.getName() + " §7is now §crestricted §7from accessing the market.");
            if (target.isOnline()) {
                  Player targetPlayer = target.getPlayer();
                  Messaging.prefixedChat(targetPlayer, "§7You have been §crestricted§7 from accessing the market by §6" + 
                          player.getName() + "§7.");
                  targetPlayer.playSound(targetPlayer.getLocation(), Sound.BLOCK_ANVIL_LAND, 1F, 0.5F);
            }
      }
}
