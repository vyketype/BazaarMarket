package io.github.vyketype.bazaarmarket.conversations.prompts;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.guis.OrderConfirmGUI;
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

import java.math.BigDecimal;
import java.util.UUID;

public class CustomPricePrompt extends NumericPrompt {
      @Override
      protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull Number input) {
            UUID uuid = (UUID) context.getSessionData("uuid");
            Order.Type type = (Order.Type) context.getSessionData("type");
            Material material = Material.valueOf((String) context.getSessionData("item"));
            int amount = (int) context.getSessionData("amount");
            
            Player player = Bukkit.getPlayer(uuid);
            
            // If the input is not a positive double
            if (!ConversationPreconditions.isPositiveDouble(player, input))
                  return Prompt.END_OF_CONVERSATION;
            
            String strPrice = input.toString();
            BigDecimal unitPrice = new BigDecimal(strPrice);
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(amount));
            
            // If the player does not have enough money
            if (type == Order.Type.BUY && !ConversationPreconditions.playerHasEnoughMoney(player, totalPrice.doubleValue()))
                  return Prompt.END_OF_CONVERSATION;
            
            Messaging.prefixedChat(player, "§7You entered the following price: §6" + strPrice + "$§7.");
            OrderConfirmGUI.open(uuid, type, material, amount, unitPrice);
            return Prompt.END_OF_CONVERSATION;
      }
      
      @Override
      public @NotNull String getPromptText(@NotNull ConversationContext context) {
            return BazaarMarket.PREFIX + "§7Please enter a valid §dprice§7. §8(Type §9quit §8to cancel this operation)";
      }
}
