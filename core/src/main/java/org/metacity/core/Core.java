package org.metacity.core;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.metacity.commands.Command;
import org.metacity.commands.CommandNode;

import java.util.stream.Stream;

public class Core extends CorePlugin {

    private static Core core;

    @Override
    public void onEnable() {
        super.onEnable();
        core = this;
        registerCommands();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        CommandNode.unregisterAll();
    }

    private void registerCommands() {
        Stream.of(
                Command.builder(ConsoleCommandSender.class, "a")
                        .withExecution((s, w) ->
                                Bukkit.dispatchCommand(s, "plugman reload all")
                        ).build()
        ).forEach(c -> CommandNode.register(c));
    }

    public static Core getInstance() {
        return core;
    }

}
