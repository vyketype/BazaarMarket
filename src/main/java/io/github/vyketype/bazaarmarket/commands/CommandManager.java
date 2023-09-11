package io.github.vyketype.bazaarmarket.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import com.google.common.collect.ImmutableList;
import io.github.vyketype.bazaarmarket.BazaarMarket;
import io.github.vyketype.bazaarmarket.commands.impl.*;

public class CommandManager {
      private static final BazaarMarket INSTANCE = BazaarMarket.getINSTANCE();
      private static final ImmutableList<BaseCommand> COMMANDS = ImmutableList.of(
              new MarketCommand(),
              new PriceCommand(),
              new BalanceCommand(),
              new BankCommand(),
              new TradeBlockCommand()
      );
      
      public CommandManager() {
            loadCommands();
      }
      
      public void loadCommands() {
            BukkitCommandManager bcm = new BukkitCommandManager(INSTANCE);
            
            bcm.enableUnstableAPI("help");
            bcm.enableUnstableAPI("brigadier");
      
            COMMANDS.forEach(bcm::registerCommand);
      }
}
