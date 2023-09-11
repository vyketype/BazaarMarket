package io.github.vyketype.bazaarmarket.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import io.github.vyketype.bazaarmarket.market.OrderBook;
import io.github.vyketype.bazaarmarket.util.ItemUtil;
import io.github.vyketype.bazaarmarket.util.Messaging;
import io.github.vyketype.bazaarmarket.util.preconditions.CommandPreconditions;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Price command.
 *
 * @author vyketype
 */
@CommandAlias("price|pr")
@CommandPermission("market.price")
@Description("Retrieve the best prices of the held item")
public class PriceCommand extends BaseCommand {
      @Default
      public void onPrice(Player player) {
            Material material = player.getInventory().getItemInMainHand().getType();
            
            // If the material is air
            if (CommandPreconditions.isPlayerHoldingAir(player, material))
                  return;
            
            // If the item is not on the market
            if (!CommandPreconditions.doesMarketItemExist(player, material))
                  return;
            
            OrderBook book = OrderBook.get(material);
            String name = ItemUtil.getStylizedName(material.name());
            
            // Send price summary
            Messaging.prefixedChat(player, "§7Here is the price summary for §d" + name + "§7.");
            Messaging.prefixedChat(player, "§7Best buy offer: §6" + book.getLowestBuyPrice().doubleValue() +
                    "$ §7(§3" + book.getBuyOrders().size() + " §7offers)");
            Messaging.prefixedChat(player, "§7Best sell offer: §6" + book.getHighestSellPrice().doubleValue() +
                    "$ §7(§c" + book.getBuyOrders().size() + " §7offers)");
      }
}