package io.github.vyketype.bazaarmarket.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.Messaging;
import io.github.vyketype.bazaarmarket.util.preconditions.CommandPreconditions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("balance|bal")
@CommandPermission("market.balance")
@Description("Check your or another player's balance")
public class BalanceCommand extends BaseCommand {
      @Default
      public void onBalance(Player player, @Optional String targetName) {
            if (targetName == null || targetName.isEmpty()) {
                  Profile profile = Profile.get(player.getUniqueId());
                  Messaging.prefixedChat(player, "§d" + player.getName() + "§7's current balance is of §6" +
                          profile.getBalance().doubleValue() + "$§7.");
                  return;
            }
            
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            
            // If the player has not joined before
            if (!CommandPreconditions.hasPlayerEverJoined(player, offlineTarget.getUniqueId()))
                  return;
            
            Profile profile = Profile.get(offlineTarget.getUniqueId());
            Messaging.prefixedChat(player, "§d" + offlineTarget.getName() + "§7's current balance is of §6" +
                  profile.getBalance().doubleValue() + "$§7.");
      }
}
