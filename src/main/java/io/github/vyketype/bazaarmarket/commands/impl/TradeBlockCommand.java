package io.github.vyketype.bazaarmarket.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.Messaging;
import io.github.vyketype.bazaarmarket.util.preconditions.CommandPreconditions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@CommandAlias("tradeblock|tb")
@CommandPermission("pixlies.business.tradeblock")
public class TradeBlockCommand extends BaseCommand {
      @Subcommand("list")
      @Description("View who you have blocked")
      public void onTradeBlockList(Player player) {
            Profile profile = Profile.get(player.getUniqueId());
            List<UUID> list = profile.getBlockedPlayers();
            
            // If there are no trade-blocked players
            if (list.isEmpty()) {
                  Messaging.prefixedChat(player, "§7Your trade-block list is §cempty§7.");
                  return;
            }
            
            // Format names
            StringBuilder names = new StringBuilder();
            for (UUID uuid : list) {
                  names.append(Bukkit.getOfflinePlayer(uuid).getName());
                  if (list.indexOf(uuid) != list.size() - 1) {
                        names.append(",");
                  }
            }
            
            // Send message
            Messaging.prefixedChat(player, "§7Here are the players who you have §cblocked §7trading on the market with: §d" + names);
      }
      
      @Subcommand("add")
      @Description("Block a player from trading with you")
      @Syntax("<player>")
      public void onTradeBlockAdd(Player player, String name) {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(name);
            
            if (!CommandPreconditions.hasPlayerEverJoined(player, offlineTarget.getUniqueId()))
                  return;
            
            if (CommandPreconditions.isPlayerAlreadyTradeBlocked(player, name))
                  return;
            
            Profile profile = Profile.get(player.getUniqueId());
            profile.tradeBlockPlayer(offlineTarget.getUniqueId());
            profile.save();
            
            Messaging.prefixedChat(player, "§7You §ctrade-blocked §7the player §d" + name + "§7.");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.5F);
      }
      
      @Subcommand("remove")
      @Description("Unblock a player from trading with you")
      @Syntax("<player>")
      public void onTradeBlockRemove(Player player, String name) {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(name);
            
            if (!CommandPreconditions.hasPlayerEverJoined(player, offlineTarget.getUniqueId()))
                  return;
            
            if (CommandPreconditions.isPlayerAlreadyNotTradeBlocked(player, name))
                  return;
            
            Profile profile = Profile.get(player.getUniqueId());
            profile.unTradeBlockPlayer(offlineTarget.getUniqueId());
            profile.save();
            
            Messaging.prefixedChat(player, "§7You §atrade-unblocked §7the player §d" + name + "§7.");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 2F);
      }
      
      @Default
      @HelpCommand
      public void onHelp(CommandHelp help) {
            help.showHelp();
      }
}