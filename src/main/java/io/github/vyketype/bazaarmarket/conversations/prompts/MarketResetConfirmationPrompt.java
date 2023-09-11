package io.github.vyketype.bazaarmarket.conversations.prompts;

import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.market.MarketAction;
import io.github.vyketype.bazaarmarket.market.OrderBook;
import io.github.vyketype.bazaarmarket.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MarketResetConfirmationPrompt extends StringPrompt {
      @Nullable
      @Override
      public Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
            UUID uuid = (UUID) context.getSessionData("uuid");
            Player player = Bukkit.getPlayer(uuid);
            
            if (input.equals("confirm")) {
                  OrderBook.resetAll();
      
                  // Send messages and play sounds
                  for (Player p : Bukkit.getOnlinePlayers()) {
                        Messaging.prefixedChat(p, "§7Market orders and statistics have been §creset§7.");
                        p.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.5F);
                  }
      
                  // Log market action
                  String action = MarketAction.MARKET_RESET.format(player.getName());
                  MarketAction.updateLog(action);
            } else {
                  Messaging.prefixedChat(player, "§cYour action was cancelled!");
                  player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.5F);
            }
      
            return Prompt.END_OF_CONVERSATION;
      }
      
      @NotNull
      @Override
      public String getPromptText(@NotNull ConversationContext context) {
            return BazaarMarket.PREFIX + "§7Please type §a\"confirm\" §7to §creset §7the market in the next 10 seconds.";
      }
}
