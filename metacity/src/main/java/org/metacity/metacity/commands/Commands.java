package org.metacity.metacity.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.metacity.commands.Command;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.player.MetaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class Commands {

    private static Commands commandUtil;

    private final List<Command<? extends CommandSender>> commands = new ArrayList<>();

    private Commands() {
        commands.clear();
        registerCommands();
    }

    public static void init() {
        if (commandUtil == null) commandUtil = new Commands();
    }

    private void registerCommands() {
        Stream.of(
                Command.builder(Player.class, "balance")
                        .withExecution((p, s) -> {

                        })
                        .build()
        ).forEach(commands::add);
    }

}
