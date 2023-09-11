package io.github.vyketype.bazaarmarket.market;

import io.github.vyketype.bazaarmarket.BazaarMarket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Represents an action in the market.
 *
 * @author vyketype
 * @since 0.1
 */
public enum MarketAction {
      CREATE_ORDER,
      DELETE_ORDER,
      TRADE,
      MARKET_RESET;
      
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      
      /**
       * Get a string about the MarketAction instance which will be inserted into the log.
       *
       * @param args Arguments.
       *             <br>
       * CREATE_ORDER: [playerName] created a [type] order "[orderId]" on
       *             [amount]x [itemName] for [price] coins each
       *             <br>
       * DELETE_ORDER: [playerName] deleted a [type] order "[orderId]"
       *             <br>
       * TRADE: [playerName] ("[buyOrderId]") bought [amount]x [itemName]
       *             for [price] coins each from order "[sellOrderId]"
       *             <br>
       * MARKET_RESET: [playerName] reset the market
       */
      public String format(String... args) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String string = formatter.format(now) + " " + args[0] + " ";
            switch (this) {
                  case CREATE_ORDER -> {
                        return string + "created " + args[1] + " order \"" + args[2] + "\" on " + args[3] + "x " +
                                args[4] + " for " + args[5] + "$ each";
                  }
                  case DELETE_ORDER -> {
                        return string + "deleted " + args[1] + " order \"" + args[2] + "\"";
                  }
                  case TRADE -> {
                        return string + "(\"" + args[1] + "\") bought " + args[2] + "x " + args[3] + " for " +
                                args[4] + "$ each from order \"" + args[5] + "\"";
                  }
                  case MARKET_RESET -> {
                        return string + "reset the market";
                  }
                  default -> {
                        return string + "INVALID";
                  }
            }
      }
      
      /**
       * Add a log entry in log.yml and optionally send message to console in accordance with the configuration.
       *
       * @param action Log message.
       */
      public static void updateLog(String action) {
            List<String> log = INSTANCE.getLog().getStringList("log");
            log.add(action);
            INSTANCE.getLog().set("log", log);
            INSTANCE.getLog().save();
      
            // Send to console
            if (INSTANCE.getConfig().getBoolean("log_to_console"))
                  INSTANCE.getLogger().info(action);
      }
}
