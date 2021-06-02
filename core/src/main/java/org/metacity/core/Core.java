package org.metacity.core;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.metacity.commands.Command;

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
        Command.unregisterAll();
    }

    private void registerCommands() {
        Stream.of(
                Command.builder(ConsoleCommandSender.class, "a")
                        .withExecution((s, w) ->
                                Bukkit.dispatchCommand(s, "plugman reload all")
                        ).build()
        ).forEach(Command::register);
    }

    public static Core getInstance() {
        return core;
    }

}
