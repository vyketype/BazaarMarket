package io.github.vyketype.bazaarmarket.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
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

@CommandAlias("economy|eco|bank")
public class BankCommand extends BaseCommand {
      @Subcommand("clear")
      @CommandPermission("market.bank.clear")
      @Description("Clear a player's bank account")
      @Syntax("<player>")
      public void onBankClear(Player player, String targetName) {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            
            if (!CommandPreconditions.hasPlayerEverJoined(player, offlineTarget.getUniqueId()))
                  return;
            
            Profile profile = Profile.get(offlineTarget.getUniqueId());
            profile.removeBalance(profile.getBalance());
            profile.save();
            
            Messaging.prefixedChat(player, "§7You §ccleared §7the balance of §d" + targetName + "§7's bank account.");
            
            if (offlineTarget.isOnline()) {
                  Player target = offlineTarget.getPlayer();
                  Messaging.prefixedChat(target, "§7Your bank account balance has been §ccleared §7(admin action).");
                  player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1F, 0.5F);
            }
      }
      
      @Subcommand("deposit|add|give")
      @CommandPermission("market.bank.give")
      @Description("Give a player some money")
      @Syntax("<player> <amount>")
      public void onBankDeposit(Player player, String strArgs) {
            if (!CommandPreconditions.economy(player, strArgs))
                  return;
            
            ArgumentInformation info = ArgumentInformation.get(strArgs);
            
            Profile profile = Profile.get(info.offlineTarget().getUniqueId());
            profile.addBalance(BigDecimal.valueOf(Double.parseDouble(info.strAmount())));
            profile.save();
            
            Messaging.prefixedChat(player, "§7You added §6" + info.strAmount() + "$ §7to §d" + info.targetName() + "§7's bank account.");
            
            if (info.offlineTarget().isOnline()) {
                  Player target = info.offlineTarget().getPlayer();
                  Messaging.prefixedChat(target, "§7You received §6" + info.strAmount() + "$ §7(admin action).");
                  player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.5F);
            }
      }
      
      @Subcommand("withdraw|remove|take")
      @CommandPermission("market.bank.remove")
      @Description("Remove some money from a player's bank account")
      @Syntax("<player> <amount>")
      public void onBankWithdraw(Player player, String strArgs) {
            if (!CommandPreconditions.economy(player, strArgs))
                  return;
            
            ArgumentInformation info = ArgumentInformation.get(strArgs);
            
            Profile profile = Profile.get(info.offlineTarget().getUniqueId());
            profile.removeBalance(BigDecimal.valueOf(Double.parseDouble(info.strAmount())));
            profile.save();
            
            Messaging.prefixedChat(player, "§7You removed §6" + info.strAmount() + "$ §7from §d" + info.targetName() + "§7's bank account.");
            
            if (info.offlineTarget().isOnline()) {
                  Player target = info.offlineTarget().getPlayer();
                  Messaging.prefixedChat(target, "§7You lost §6" + info.strAmount() + "$ §7(admin action).");
                  player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1F, 0.5F);
            }
      }
      
      @HelpCommand
      public void onHelp(CommandHelp help) {
            help.showHelp();
      }
      
      private record ArgumentInformation(String targetName, String strAmount, OfflinePlayer offlineTarget) {
            public static ArgumentInformation get(String strArgs) {
                  String[] args = StringUtils.split(strArgs, " ", -1);
                  String targetName = args[0];
                  String strAmount = args[1];
                  OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
                  return new ArgumentInformation(targetName, strAmount, offlineTarget);
            }
      }
}
