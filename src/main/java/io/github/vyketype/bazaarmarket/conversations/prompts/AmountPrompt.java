package io.github.vyketype.bazaarmarket.conversations.prompts;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.guis.OrderPriceGUI;
import io.github.vyketype.bazaarmarket.listeners.impl.InventoryClickListener;
import io.github.vyketype.bazaarmarket.market.Order;
import io.github.vyketype.bazaarmarket.util.Messaging;
import io.github.vyketype.bazaarmarket.util.preconditions.ConversationPreconditions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AmountPrompt extends NumericPrompt {
      @Override
      protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull Number input) {
            UUID uuid = (UUID) context.getSessionData("uuid");
            Order.Type type = (Order.Type) context.getSessionData("type");
            Material material = Material.valueOf((String) context.getSessionData("item"));
            
            Player player = Bukkit.getPlayer(uuid);
            
            // If the input is not a positive double
            if (!ConversationPreconditions.isPositiveInteger(player, input))
                  return Prompt.END_OF_CONVERSATION;
            
            int amount = input.intValue();
            
            // If the player does not have the amount to sell
            if (type == Order.Type.SELL && !ConversationPreconditions.playerHasEnoughItems(player, material, amount))
                  return Prompt.END_OF_CONVERSATION;
      
            Messaging.prefixedChat(player, "§7You entered the following amount: §a" + amount + "§7.");
            OrderPriceGUI.open(uuid, type, material, amount);
            InventoryClickListener.VIEWING_MARKET.add(uuid);
            return Prompt.END_OF_CONVERSATION;
      }
      
      @Override
      public @NotNull String getPromptText(@NotNull ConversationContext context) {
            Order.Type type = (Order.Type) context.getSessionData("type");
            return BazaarMarket.PREFIX + "Please enter a valid §damount you want to " + type.name().toLowerCase() +
                    "§7. §8(Type §9quit §8to cancel this operation)";      }
}
