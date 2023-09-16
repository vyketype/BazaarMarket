package io.github.vyketype.bazaarmarket.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.github.vyketype.bazaarmarket.profile.Profile;
import io.github.vyketype.bazaarmarket.util.Messaging;
import io.github.vyketype.bazaarmarket.util.preconditions.CommandPreconditions;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

@CommandAlias("pay")
@CommandPermission("market.pay")
@Description("Pay another player some money")
public class PayCommand extends BaseCommand {
      @Default
      @Syntax("<player> <amount>")
      @CommandCompletion("@players")
      public void onPay(Player player, String strArgs) {
            String[] args = StringUtils.split(strArgs, " ", -1);
            String targetName = args[0];
            String strAmount = args[1];
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            
            if (!CommandPreconditions.economy(player, strAmount, offlineTarget))
                  return;
            
            double amount = Double.parseDouble(strAmount);
            
            if (!CommandPreconditions.playerHasEnoughMoney(player, amount))
                  return;
            
            // Add money to target
            Profile targetProfile = Profile.get(offlineTarget.getUniqueId());
            targetProfile.addBalance(BigDecimal.valueOf(amount));
            targetProfile.save();
            
            // Remove money from target
            Profile playerProfile = Profile.get(player.getUniqueId());
            playerProfile.removeBalance(BigDecimal.valueOf(amount));
            playerProfile.save();
            
            Messaging.prefixedChat(player, "§7You gave §6" + strAmount + "$ §7to §d" +
                    targetName + " §7of your own money.");
            
            if (offlineTarget.isOnline()) {
                  Player target = offlineTarget.getPlayer();
                  Messaging.prefixedChat(target, "§7You received §6" + strAmount + "$ §7from §d" +
                          player.getName() + "§7.");
                  player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.5F);
            }
      }
}
